package com.pigs.borrowit.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.pigs.borrowit.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection("users")

    fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val user = snapshot?.let { doc ->
                User(
                    username = doc.getString("username") ?: "",
                    profilePicture = doc.getString("profilepicture") ?: ""
                )
            }
            trySend(user)
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUser(uid: String): User? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc?.let {
                User(
                    username = doc.getString("username") ?: "",
                    profilePicture = doc.getString("profilepicture") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al obtener usuario $uid", e)
            null
        }
    }

    fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                User(
                    username = doc.getString("username") ?: "",
                    profilePicture = doc.getString("profilepicture") ?: ""
                )
            } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(uid: String, username: String, profilePicture: String): Result<Unit> {
        return try {
            val userMap = mapOf(
                "username" to username,
                "profilepicture" to profilePicture
            )
            usersCollection.document(uid).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsername(uid: String, newUsername: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update("username", newUsername).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfilePicture(uid: String, pictureUrl: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update("profilepicture", pictureUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}