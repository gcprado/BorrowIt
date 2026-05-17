package com.pigs.borrowit.data.model

import com.google.firebase.Timestamp

data class Community(
    val id: String = "",
    val name: String = "",
    val nameLowercase: String = "",
    val description: String = "",
    val bannerUrl: String? = null,
    val profileUrl: String? = null,
    val creatorId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val memberCount: Int = 0
)

data class CommunityMember(
    val userId: String = "",
    val userName: String = "",
    val role: String = "member", // "admin" or "member"
    val joinedAt: Timestamp = Timestamp.now()
)

data class CommunityItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val condition: String = "Good", // "New", "Like New", "Good", "Acceptable"
    val status: String = "available", // "available", "not available"
    val imageUrls: List<String> = emptyList(),
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
)
