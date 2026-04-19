package com.pigs.borrowit.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.pigs.borrowit.data.model.BorrowRequest
import kotlinx.coroutines.tasks.await

class BorrowRepository {
    private val db = FirebaseFirestore.getInstance()
    private val requestsRef = db.collection("borrow_requests")

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
            requestsRef
                .whereEqualTo("requesterId", userId)
                .whereEqualTo("status", "finished")
                .get()
                .await()
                .toObjects(BorrowRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserLendingHistory(userId: String): List<BorrowRequest> {
        return try {
            requestsRef
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("status", "finished")
                .get()
                .await()
                .toObjects(BorrowRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
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
