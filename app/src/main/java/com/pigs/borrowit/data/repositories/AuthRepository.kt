package com.pigs.borrowit.data.repositories

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pigs.borrowit.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersCollection = firestore.collection("users")

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    fun register(email: String, password: String, username: String, profilePictureUrl: String = "", onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val newUser = User(
                            username = username,
                            profilePicture = profilePictureUrl
                        )
                        usersCollection.document(firebaseUser.uid).set(newUser)
                            .addOnSuccessListener { onResult(true, null) }
                            .addOnFailureListener { e -> onResult(true, "Profile creation failed") }
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
            if (user != null) {
                val userRef = usersCollection.document(user.uid)
                val userDoc = userRef.get().await()
                
                // Obtenemos la URL de la foto de Google
                val googlePhoto = user.photoUrl?.toString() ?: ""
                
                // Si el usuario es nuevo, o si no tiene foto pero Google sí la tiene, actualizamos
                if (!userDoc.exists() || (userDoc.getString("profilePicture").isNullOrEmpty() && googlePhoto.isNotEmpty())) {
                    val newUser = User(
                        username = user.displayName ?: "User",
                        profilePicture = googlePhoto
                    )
                    userRef.set(newUser).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getUsername(userId: String): String {
        return try {
            val doc = usersCollection.document(userId).get().await()
            doc.getString("username") ?: "Unknown User"
        } catch (e: Exception) { "User" }
    }

    fun logout(context: Context) {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut()
    }
}
