package com.pigs.borrowit.data.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        profilePictureUrl: String = "", // Opcional, puede actualizarse después
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Crear documento en la colección users
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
                                // Aún así el registro fue exitoso, pero informamos del error secundario
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