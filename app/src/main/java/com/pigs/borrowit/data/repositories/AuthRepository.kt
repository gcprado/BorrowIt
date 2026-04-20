package com.pigs.borrowit.data.repositories

import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun register(
        email: String,
        password: String,
        username: String,
        profilePictureUrl: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userData = mapOf(
                            "username" to username,
                            "profilepicture" to profilePictureUrl
                        )
                        usersCollection.document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("AuthRepository", "Error al crear perfil", e)
                                onResult(true, "Registro exitoso, pero falló la creación del perfil")
                            }
                    } else {
                        onResult(false, "Usuario autenticado pero no encontrado")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    suspend fun signInWithGoogle(credential: AuthCredential): Result<Unit> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null && result.additionalUserInfo?.isNewUser == true) {
                // If new user, create profile in Firestore
                val userData = mapOf(
                    "username" to (user.displayName ?: "User"),
                    "profilepicture" to (user.photoUrl?.toString() ?: "")
                )
                usersCollection.document(user.uid).set(userData).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}