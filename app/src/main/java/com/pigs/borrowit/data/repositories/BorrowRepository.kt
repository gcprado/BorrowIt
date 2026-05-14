package com.pigs.borrowit.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.Timestamp
import com.pigs.borrowit.data.model.BorrowRequest
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BorrowRepository {
    private val db = FirebaseFirestore.getInstance()
    private val requestsRef = db.collection("borrow_requests")

    private fun mapToBorrowRequest(doc: DocumentSnapshot): BorrowRequest? {
        return try {
            val ownerIdRaw = doc.get("ownerId")
            val ownerId = when (ownerIdRaw) {
                is com.google.firebase.firestore.DocumentReference -> ownerIdRaw.id
                is String -> {
                    var str = ownerIdRaw.trim()
                    if (str.startsWith("users/")) str = str.substringAfter("users/")
                    str
                }
                else -> ""
            }

            val requesterIdRaw = doc.get("requesterId")
            val requesterId = when (requesterIdRaw) {
                is com.google.firebase.firestore.DocumentReference -> requesterIdRaw.id
                is String -> {
                    var str = requesterIdRaw.trim()
                    if (str.startsWith("users/")) str = str.substringAfter("users/")
                    str
                }
                else -> ""
            }
            
            BorrowRequest(
                id = doc.id,
                communityId = doc.getString("communityId") ?: "",
                itemId = doc.getString("itemId") ?: "",
                itemName = doc.getString("itemName") ?: "",
                ownerId = ownerId,
                ownerName = doc.getString("ownerName") ?: "",
                requesterId = requesterId,
                requesterName = doc.getString("requesterName") ?: "",
                status = doc.getString("status") ?: "pending",
                requestDate = doc.getTimestamp("requestDate") ?: Timestamp.now(),
                startDate = doc.getTimestamp("startDate") ?: Timestamp.now(),
                endDate = doc.getTimestamp("endDate") ?: Timestamp.now()
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createBorrowRequest(request: BorrowRequest): Result<String> {
        return try {
            val docRef = requestsRef.document() // Auto-generated ID
            val newRequest = request.copy(id = docRef.id)
            docRef.set(newRequest).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserBorrowHistory(userId: String): List<BorrowRequest> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("status", "finished")
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserLendingHistory(userId: String): List<BorrowRequest> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("status", "finished")
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserPastBorrows(userId: String): List<BorrowRequest> {
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = requestsRef
                .whereIn("requesterId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }.filter { it.status == "finished" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserPastLends(userId: String): List<BorrowRequest> {
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = requestsRef
                .whereIn("ownerId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }.filter { it.status == "finished" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserActiveBorrows(userId: String): List<BorrowRequest> {
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = requestsRef
                .whereIn("requesterId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }.filter { it.status == "pending" || it.status == "accepted" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserActiveLends(userId: String): List<BorrowRequest> {
        return try {
            val userRef = db.collection("users").document(userId)
            val snapshot = requestsRef
                .whereIn("ownerId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }.filter { it.status == "pending" || it.status == "accepted" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveBorrowsFlow(userId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val userRef = db.collection("users").document(userId)
        val listener = requestsRef
            .whereIn("requesterId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val items = snapshot?.documents?.mapNotNull { mapToBorrowRequest(it) }
                    ?.filter { it.status != "finished" && it.status != "rejected" } 
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun getActiveLendsFlow(userId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val userRef = db.collection("users").document(userId)
        val listener = requestsRef
            .whereIn("ownerId", listOf(userId, userRef, "users/$userId", "$userId ", " $userId"))
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finishRequestsForItem(itemId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val itemRef = db.collection("items").document(itemId)
            
            val queries = listOf(
                db.collection("borrow_requests").whereEqualTo("itemId", itemId).whereEqualTo("status", "accepted"),
                db.collection("borrow_requests").whereEqualTo("itemId", itemRef).whereEqualTo("status", "accepted")
            )
            
            for (query in queries) {
                val snapshot = query.get().await()
                for (doc in snapshot.documents) {
                    doc.reference.update(
                        "status", "finished",
                        "endDate", Timestamp.now()
                    ).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
