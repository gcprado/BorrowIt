package com.pigs.borrowit.data.models

data class HistoryTransaction(
    val id: Int,
    val itemName: String,
    val interactionType: InteractionType,
    val date: String,
    val userName: String
)

enum class InteractionType {
    LENT,
    BORROWED
}

fun InteractionType.toDisplayString(): String {
    return when (this) {
        InteractionType.LENT -> "Prestado a"
        InteractionType.BORROWED -> "Tomado de"
    }
}
