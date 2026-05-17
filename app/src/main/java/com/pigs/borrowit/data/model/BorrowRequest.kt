package com.pigs.borrowit.data.model

import com.google.firebase.Timestamp

data class BorrowRequest(
    val id: String = "",
    val communityId: String = "",
    val itemId: String = "",
    val itemName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val status: String = "pending", // "pending", "accepted", "rejected", "finished"
    val requestDate: Timestamp = Timestamp.now(),
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "communityId" to communityId,
            "itemId" to itemId,
            "itemName" to itemName,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "requesterId" to requesterId,
            "requesterName" to requesterName,
            "status" to status,
            "requestDate" to requestDate,
            "startDate" to startDate,
            "endDate" to endDate
        )
    }
}
