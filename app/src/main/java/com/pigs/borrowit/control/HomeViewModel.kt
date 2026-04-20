package com.pigs.borrowit.control

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pigs.borrowit.data.model.BorrowRequest
import com.pigs.borrowit.data.model.User
import com.pigs.borrowit.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    val pendingRequests = mutableStateListOf(
        BorrowRequest(
            id = "req1",
            requesterName = "Dustin Henderson",
            itemName = "Nintendo Switch",
            status = "pending"
        ),
        BorrowRequest(
            id = "req2",
            requesterName = "Lucas Sinclair",
            itemName = "Hydraulic Jack",
            status = "pending"
        )
    )

    init {
        loadUser()
    }

    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserFlow(uid).collect {
                _userState.value = it
            }
        }
    }

    fun setShowNotifications(show: Boolean) {
        _showNotifications.value = show
    }

    fun acceptRequest(request: BorrowRequest) {
        pendingRequests.remove(request)
        // Add additional logic here if needed (e.g. updating Firestore)
    }

    fun declineRequest(request: BorrowRequest) {
        pendingRequests.remove(request)
        // Add additional logic here if needed
    }
}