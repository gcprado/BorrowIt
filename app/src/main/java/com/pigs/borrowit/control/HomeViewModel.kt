package com.pigs.borrowit.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pigs.borrowit.R
import com.pigs.borrowit.data.model.BorrowRequest
import com.pigs.borrowit.data.model.Item
import com.pigs.borrowit.data.model.User
import com.pigs.borrowit.data.repositories.BorrowRepository
import com.pigs.borrowit.data.repositories.ItemRepository
import com.pigs.borrowit.data.repositories.UserRepository
import com.pigs.borrowit.screens.SponsoredAd
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val borrowRepository: BorrowRepository = BorrowRepository(),
    private val itemRepository: ItemRepository = ItemRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<BorrowRequest>>(emptyList())
    val pendingRequests: StateFlow<List<BorrowRequest>> = _pendingRequests.asStateFlow()

    private val _recommendedItem = MutableStateFlow<Item?>(null)
    val recommendedItem: StateFlow<Item?> = _recommendedItem.asStateFlow()

    private val _sponsoredAds = MutableStateFlow<List<SponsoredAd>>(emptyList())
    val sponsoredAds: StateFlow<List<SponsoredAd>> = _sponsoredAds.asStateFlow()

    init {
        loadUser()
        observePendingRequests()
        loadRecommendedItem()
        loadSponsoredAds()
    }

    private fun loadUser() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserFlow(uid).collect {
                _userState.value = it
            }
        }
    }

    private fun observePendingRequests() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            borrowRepository.getActiveLendsFlow(uid).collect { requests ->
                _pendingRequests.value = requests.filter { it.status == "pending" }
            }
        }
    }

    private fun loadRecommendedItem() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val history = borrowRepository.getUserPastBorrows(uid)
            if (history.isNotEmpty()) {
                val randomRequest = history.random()
                val item = itemRepository.getItemById(randomRequest.itemId)
                _recommendedItem.value = item
            }
        }
    }

    private fun loadSponsoredAds() {
        val adPool = listOf(
            SponsoredAd(1, "Professional Hair Dryer", "Dyson", "Fast drying • Ionic technology", R.drawable.hairdryer),
            SponsoredAd(2, "Electric Drill Pro", "Bosch", "Cordless • 20V Max", R.drawable.electricdrill),
            SponsoredAd(3, "Mountain Bike", "Trek", "All-terrain • 21 speeds", R.drawable.mountainbike),
            SponsoredAd(4, "Coffee Maker", "Nespresso", "One-touch brewing", R.drawable.coffeemaker),
            SponsoredAd(5, "Camping Tent", "Quechua", "Waterproof • 3 persons", R.drawable.campingtent),
            SponsoredAd(6, "Pressure Washer", "Kärcher", "High pressure • Compact", R.drawable.pressurewasher),
            SponsoredAd(7, "Lawn Mower", "Honda", "Gas powered • Self-propelled", R.drawable.lawnmower),
            SponsoredAd(8, "Standing Mixer", "KitchenAid", "Professional grade • 5qt", R.drawable.standingmixer)
        )
        // Shuffle and take 3 random ads from the pool
        _sponsoredAds.value = adPool.shuffled().take(3)
    }

    fun setShowNotifications(show: Boolean) {
        _showNotifications.value = show
    }

    fun acceptRequest(request: BorrowRequest) {
        viewModelScope.launch {
            borrowRepository.updateRequestStatus(request.id, "accepted")
        }
    }

    fun declineRequest(request: BorrowRequest) {
        viewModelScope.launch {
            borrowRepository.updateRequestStatus(request.id, "rejected")
        }
    }

    fun borrowItem(item: Item) {
        val uid = auth.currentUser?.uid ?: return
        val currentUser = _userState.value
        
        viewModelScope.launch {
            try {
                val ownerUser = userRepository.getUser(item.owner)
                
                val request = BorrowRequest(
                    communityId = item.communityId,
                    itemId = item.id,
                    itemName = item.name,
                    ownerId = item.owner,
                    ownerName = ownerUser?.username ?: "Owner",
                    requesterId = uid,
                    requesterName = currentUser?.username ?: "User",
                    status = "pending"
                )
                
                val result = borrowRepository.createBorrowRequest(request)
                if (result.isSuccess) {
                    Log.d("HomeViewModel", "Borrow request created: ${result.getOrNull()}")
                } else {
                    Log.e("HomeViewModel", "Failed to create borrow request", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error in borrowItem", e)
            }
        }
    }
}
