package com.pigs.borrowit.data.models

data class BorrowItem(
    val id: Int,
    val name: String,
    val description: String,
    val status: ItemStatus,
    val condition: String,
    val imageUrl: String = "",
    val availableFrom: String = "",
    val availableUntil: String = "",
    val category: String = "",
    val ownerId: Int = 0,
    val ownerName: String = "",
    val borrowerId: Int? = null,
    val borrowerName: String? = null
)

enum class ItemStatus {
    AVAILABLE,
    BORROWED,
    IN_USE,
    UNAVAILABLE
}

fun ItemStatus.toDisplayString(): String {
    return when (this) {
        ItemStatus.AVAILABLE -> "Disponible"
        ItemStatus.BORROWED -> "Prestado"
        ItemStatus.IN_USE -> "En uso"
        ItemStatus.UNAVAILABLE -> "No disponible"
    }
}
