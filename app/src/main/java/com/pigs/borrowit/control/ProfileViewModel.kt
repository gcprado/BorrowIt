package com.pigs.borrowit.control

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pigs.borrowit.data.model.User
import com.pigs.borrowit.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLDecoder
import java.util.UUID

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _navigateToLogin = MutableStateFlow(false)
    val navigateToLogin: StateFlow<Boolean> = _navigateToLogin.asStateFlow()

    val currentUser = auth.currentUser
    val uid = currentUser?.uid ?: ""
    val email = currentUser?.email ?: ""

    init {
        loadUser()
    }

    private fun loadUser() {
        if (uid.isNotEmpty()) {
            viewModelScope.launch {
                userRepository.getUserFlow(uid).collect {
                    _userState.value = it
                }
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun onLogout(context: Context) {
        // 1. Firebase Sign out
        auth.signOut()
        
        // 2. Google Sign out (para permitir cambiar de cuenta)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            _navigateToLogin.value = true
        }
    }

    fun onDeleteAccount() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                try {
                    // 1. Delete Firestore data
                    userRepository.deleteUserData(user.uid)
                    
                    // 2. Delete Auth user
                    user.delete().await()
                    
                    _navigateToLogin.value = true
                } catch (e: Exception) {
                    _snackbarMessage.value = "Error deleting account: ${e.localizedMessage}"
                    Log.e("ProfileViewModel", "Error deleting account", e)
                }
            }
        }
    }

    fun saveUsername(username: String) {
        if (uid.isNotEmpty() && username.isNotBlank()) {
            viewModelScope.launch {
                _isSaving.value = true
                userRepository.updateUsername(uid, username)
                    .onSuccess {
                        _snackbarMessage.value = "Username updated"
                    }
                    .onFailure {
                        _snackbarMessage.value = "Error updating username"
                    }
                _isSaving.value = false
            }
        }
    }

    fun uploadImage(context: Context, imageUri: Uri, compressImage: suspend (Context, Uri) -> ByteArray?) {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            _isUploadingImage.value = true
            try {
                val compressedData = compressImage(context, imageUri)
                if (compressedData == null) {
                    _snackbarMessage.value = "Error compressing image"
                    return@launch
                }

                // Delete old image if exists
                val oldUrl = _userState.value?.profilePicture
                if (!oldUrl.isNullOrEmpty()) {
                    val regex = Regex("/o/(.+?)\\?")
                    val matchResult = regex.find(oldUrl)
                    val oldPath = matchResult?.groupValues?.get(1)?.let {
                        try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { null }
                    }
                    if (oldPath != null) {
                        try {
                            FirebaseStorage.getInstance().reference.child(oldPath).delete().await()
                        } catch (e: Exception) {
                            Log.w("ProfileViewModel", "Could not delete old image: ${e.message}")
                        }
                    }
                }

                // Upload new image
                val storageRef = FirebaseStorage.getInstance().reference
                val fileName = "${UUID.randomUUID()}.jpg"
                val imageRef = storageRef.child("profile_pictures/${uid}/${fileName}")

                imageRef.putBytes(compressedData).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // Update Firestore
                userRepository.updateProfilePicture(uid, downloadUrl)
                    .onSuccess {
                        _snackbarMessage.value = "Profile picture updated"
                    }
                    .onFailure {
                        _snackbarMessage.value = "Error saving URL to Firestore"
                    }
            } catch (e: Exception) {
                _snackbarMessage.value = "Error: ${e.localizedMessage}"
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }
}
