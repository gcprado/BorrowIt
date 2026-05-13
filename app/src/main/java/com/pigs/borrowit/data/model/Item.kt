package com.pigs.borrowit.data.model

import com.google.firebase.Timestamp
import java.util.Date

data class Availability(
    val start: Date = Date(),
    val end: Date = Date()
) {
    // Constructor para Firestore (necesita Timestamp)
    constructor(startTimestamp: Timestamp, endTimestamp: Timestamp) : this(
        startTimestamp.toDate(),
        endTimestamp.toDate()
    )
}

data class Item(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val owner: String = "",
    val condition: String = "",
    val picture: String = "",
    val pictures: List<String> = emptyList(),
    val availability: Availability = Availability(),
    val communityId: String = ""
) {
    // Convertir a mapa para Firestore
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "description" to description,
        "owner" to owner,
        "condition" to condition,
        "picture" to picture,
        "pictures" to pictures,
        "availability" to mapOf(
            "start" to Timestamp(availability.start),
            "end" to Timestamp(availability.end)
        ),
        "communityId" to communityId
    )
}