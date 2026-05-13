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
            BorrowRequest(
                id = doc.id,
                communityId = doc.getString("communityId") ?: "",
                itemId = doc.getString("itemId") ?: "",
                itemName = doc.getString("itemName") ?: "",
                ownerId = doc.getString("ownerId") ?: "",
                ownerName = doc.getString("ownerName") ?: "",
                requesterId = doc.getString("requesterId") ?: "",
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

    suspend fun getUserActiveBorrows(userId: String): List<BorrowRequest> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("requesterId", userId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserActiveLends(userId: String): List<BorrowRequest> {
        return try {
            val snapshot = requestsRef
                .whereEqualTo("ownerId", userId)
                .whereIn("status", listOf("pending", "accepted"))
                .get()
                .await()
            snapshot.documents.mapNotNull { mapToBorrowRequest(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getActiveBorrowsFlow(userId: String): Flow<List<BorrowRequest>> = callbackFlow {
        val listener = requestsRef
            .whereEqualTo("requesterId", userId)
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
        val listener = requestsRef
            .whereEqualTo("ownerId", userId)
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
            requestsRef.document(requestId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
