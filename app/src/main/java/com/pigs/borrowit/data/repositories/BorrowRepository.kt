package com.pigs.borrowit.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import com.pigs.borrowit.data.model.BorrowRequest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.util.Log

class BorrowRepository {
    private val db = FirebaseFirestore.getInstance()
    private val requestsRef = db.collection("borrow_requests")

    /**
     * Limpia un ID que pueda venir como ruta (/users/ID), referencia o con espacios.
     */
    private fun cleanId(idRaw: Any?): String {
        return when (idRaw) {
            is com.google.firebase.firestore.DocumentReference -> idRaw.id
            is String -> {
                var str = idRaw.trim()
                if (str.startsWith("/")) str = str.substring(1)
                if (str.startsWith("users/")) str = str.substringAfter("users/")
                if (str.startsWith("items/")) str = str.substringAfter("items/")
                if (str.startsWith("communities/")) str = str.substringAfter("communities/")
                str
            }
            else -> idRaw?.toString() ?: ""
        }
    }

    /**
     * Genera todas las variaciones posibles de un ID de usuario para búsquedas (ID, Path, Ref).
     */
    private fun getUserIdVariations(userId: String): List<Any> {
        val userRef = db.collection("users").document(userId)
        return listOf(
            userId,
            userRef,
            "users/$userId",
            "/users/$userId",
            "users/$userId ",
            "/users/$userId "
        )
    }

    private fun mapToBorrowRequest(doc: DocumentSnapshot): BorrowRequest? {
        return try {
            val request = BorrowRequest(
                id = doc.id,
                communityId = cleanId(doc.get("communityId")),
                itemId = cleanId(doc.get("itemId")),
                itemName = doc.getString("itemName") ?: "Objeto",
                ownerId = cleanId(doc.get("ownerId")),
                ownerName = doc.getString("ownerName") ?: "Dueño",
                requesterId = cleanId(doc.get("requesterId")),
                requesterName = doc.getString("requesterName") ?: "Usuario",
                status = doc.getString("status") ?: "pending",
                requestDate = doc.getTimestamp("requestDate") ?: Timestamp.now(),
                startDate = doc.getTimestamp("startDate") ?: Timestamp.now(),
                endDate = doc.getTimestamp("endDate") ?: Timestamp.now()
            )
            Log.d("BorrowRepoDebug", "Mapped doc ${doc.id}: owner=${request.ownerId}, status=${request.status}")
            request
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error mapping doc ${doc.id}", e)
            null
        }
    }

    suspend fun createBorrowRequest(request: BorrowRequest): Result<String> {
        return try {
            val docRef = requestsRef.document()
            val newRequest = request.copy(id = docRef.id)
            docRef.set(newRequest).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPastBorrows(userId: String): List<BorrowRequest> {
        return try {
            // Buscamos finalizadas y filtramos por ID limpio localmente para asegurar match con cualquier formato en DB
            val snapshot = requestsRef
                .whereEqualTo("status", "finished")
                .get()
                .await()
            val items = snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
                .filter { it.requesterId == userId }
            
            Log.d("BorrowRepoDebug", "Found ${items.size} past borrows for user $userId")
            items
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error in getUserPastBorrows", e)
            emptyList()
        }
    }

    fun getActiveLendsFlow(userId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val variations = getUserIdVariations(userId)
        Log.d("BorrowRepoDebug", "Observing lends for $userId with variations: $variations")
        
        val listener = requestsRef
            .whereIn("ownerId", variations)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { 
                    Log.e("BorrowRepoDebug", "Snapshot error", e)
                    close(e); return@addSnapshotListener 
                }
                
                val items = snapshot?.documents?.mapNotNull { mapToBorrowRequest(it) }
                    ?.filter { it.status == "pending" } 
                    ?: emptyList()
                
                Log.d("BorrowRepoDebug", "Emitting ${items.size} pending notifications")
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun getActiveBorrowsFlow(userId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val variations = getUserIdVariations(userId)
        val listener = requestsRef
            .whereIn("requesterId", variations)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val items = snapshot?.documents?.mapNotNull { mapToBorrowRequest(it) }
                    ?.filter { it.status != "finished" && it.status != "rejected" } 
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateRequestStatus(requestId: String, newStatus: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to newStatus)
            if (newStatus == "finished") {
                updates["endDate"] = Timestamp.now()
            }
            requestsRef.document(requestId).update(updates).await()
            Log.d("BorrowRepoDebug", "Status updated to $newStatus for $requestId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finishRequestsForItem(itemId: String): Result<Unit> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("itemId", itemId)
                .get()
                .await()
            
            val batch = db.batch()
            var count = 0
            snapshot.documents.forEach { doc ->
                val status = doc.getString("status")
                if (status != "finished" && status != "rejected") {
                    batch.update(doc.reference, mapOf(
                        "status" to "finished",
                        "endDate" to Timestamp.now()
                    ))
                    count++
                }
            }
            if (count > 0) batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error in finishRequestsForItem", e)
            Result.failure(e)
        }
    }

    suspend fun getUserPastLends(userId: String): List<BorrowRequest> {
        return try {
            val variations = getUserIdVariations(userId)
            val snapshot = requestsRef
                .whereIn("ownerId", variations)
                .whereEqualTo("status", "finished")
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error in getUserPastLends", e)
            emptyList()
        }
    }

    suspend fun getUserActiveBorrows(userId: String): List<BorrowRequest> {
        return try {
            val variations = getUserIdVariations(userId)
            val snapshot = requestsRef
                .whereIn("requesterId", variations)
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
                .filter { it.status != "finished" && it.status != "rejected" }
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error in getUserActiveBorrows", e)
            emptyList()
        }
    }

    suspend fun getUserActiveLends(userId: String): List<BorrowRequest> {
        return try {
            val variations = getUserIdVariations(userId)
            val snapshot = requestsRef
                .whereIn("ownerId", variations)
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
                .filter { it.status != "finished" && it.status != "rejected" }
        } catch (e: Exception) {
            Log.e("BorrowRepoDebug", "Error in getUserActiveLends", e)
            emptyList()
        }
    }
}
