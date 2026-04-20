package com.pigs.borrowit.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.pigs.borrowit.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showErrorDialog = MutableStateFlow(false)
    val showErrorDialog: StateFlow<Boolean> = _showErrorDialog.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
        _showErrorDialog.value = false
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Fields cannot be empty"
            _showErrorDialog.value = true
            return
        }

        authRepository.login(email, password) { success, error ->
            if (success) {
                _isSuccess.value = true
            } else {
                _errorMessage.value = error ?: "Login failed"
                _showErrorDialog.value = true
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "All fields are required"
            _showErrorDialog.value = true
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            _showErrorDialog.value = true
            return
        }

        authRepository.register(email, password, username) { success, error ->
            if (success) {
                _isSuccess.value = true
            } else {
                _errorMessage.value = error ?: "Register failed"
                _showErrorDialog.value = true
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(credential)
                .onSuccess {
                    _isSuccess.value = true
                }
                .onFailure { e ->
                    _errorMessage.value = e.localizedMessage ?: "Google Sign-In failed"
                    _showErrorDialog.value = true
                }
        }
    }

    fun onNavigationHandled() {
        _isSuccess.value = false
    }
}