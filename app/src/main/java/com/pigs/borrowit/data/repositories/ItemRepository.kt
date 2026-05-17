package com.pigs.borrowit.data.repositories

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.pigs.borrowit.data.model.Availability
import com.pigs.borrowit.data.model.Item
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source

import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val itemsCollection = firestore.collection("items")

    suspend fun updateItemSuspend(itemId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            itemsCollection.document(itemId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItemSuspend(itemId: String): Result<Unit> {
        return try {
            itemsCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(imageData: ByteArray): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child("item_images/$fileName")
        imageRef.putBytes(imageData).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun getItemById(itemId: String): Item? {
        return try {
            val doc = itemsCollection.document(itemId).get().await()
            if (doc.exists()) {
                mapDocumentToItem(doc)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error getting item $itemId", e)
            null
        }
    }

    suspend fun migrateAvailabilityField() {
        val snapshot: QuerySnapshot = itemsCollection.get(Source.SERVER).await()
        snapshot.documents.forEach { doc ->
            val availabilityField = doc.get("availability")
            if (availabilityField is List<*>) {
                val list = availabilityField.filterIsInstance<Timestamp>()
                if (list.size >= 2) {
                    val newAvailability = mapOf(
                        "start" to list[0],
                        "end" to list[1]
                    )
                    doc.reference.update("availability", newAvailability).await()
                }
            }
        }
    }

    fun addItem(
        item: Item,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val document = itemsCollection.document()
        document.set(item.toMap())
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    suspend fun addItemSuspend(item: Item): Result<String> {
        return try {
            val document = itemsCollection.document()
            document.set(item.toMap()).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getItemsFlow(): Flow<List<Item>> = callbackFlow {
        val snapshotListener = itemsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents?.mapNotNull { doc ->
                mapDocumentToItem(doc)
            } ?: emptyList()

            trySend(items)
        }
        awaitClose { snapshotListener.remove() }
    }

    fun getItemsByOwnerFlow(ownerId: String): Flow<List<Item>> = callbackFlow {
        val userRef = firestore.collection("users").document(ownerId)
        val snapshotListener = itemsCollection
            .whereIn("owner", listOf(ownerId, userRef, "users/$ownerId", "$ownerId ", " $ownerId"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToItem(doc)
                } ?: emptyList()

                trySend(items)
            }
        awaitClose { snapshotListener.remove() }
    }

    fun getItemsByCurrentUserFlow(currentUserId: String): Flow<List<Item>> = callbackFlow {
        val userRef = firestore.collection("users").document(currentUserId)
        val snapshotListener = itemsCollection
            .whereIn("currentUser", listOf(currentUserId, userRef, "users/$currentUserId", "$currentUserId ", " $currentUserId"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToItem(doc)
                }?.filter { it.owner != currentUserId } ?: emptyList()

                trySend(items)
            }
        awaitClose { snapshotListener.remove() }
    }

    fun getItemsByCommunityFlow(communityId: String): Flow<List<Item>> = callbackFlow {
        val snapshotListener = itemsCollection
            .whereEqualTo("communityId", communityId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToItem(doc)
                } ?: emptyList()

                trySend(items)
            }
        awaitClose { snapshotListener.remove() }
    }

    private fun mapDocumentToItem(doc: com.google.firebase.firestore.DocumentSnapshot): Item? {
        return try {
            val name = doc.getString("name") ?: ""
            val description = doc.getString("description") ?: ""
            
            val ownerRaw = doc.get("owner")
            val owner = when (ownerRaw) {
                is com.google.firebase.firestore.DocumentReference -> ownerRaw.id
                is String -> {
                    var str = ownerRaw.trim()
                    if (str.startsWith("users/")) str = str.substringAfter("users/")
                    str
                }
                else -> ""
            }
            
            val condition = doc.getString("condition") ?: ""
            val picture = doc.getString("picture") ?: ""
            val pictures = doc.get("pictures") as? List<String> ?: emptyList()
            val availability = parseAvailability(doc.get("availability"))
            val communityId = doc.getString("communityId") ?: ""
            val status = doc.getString("status") ?: "AVAILABLE"
            
            val currentUserRaw = doc.get("currentUser")
            val currentUser = when (currentUserRaw) {
                is com.google.firebase.firestore.DocumentReference -> currentUserRaw.id
                is String -> {
                    var str = currentUserRaw.trim()
                    if (str.startsWith("users/")) str = str.substringAfter("users/")
                    str
                }
                else -> ""
            }

            Item(
                id = doc.id,
                name = name,
                description = description,
                owner = owner,
                condition = condition,
                picture = picture,
                pictures = pictures,
                availability = availability,
                communityId = communityId,
                status = status,
                currentUser = currentUser
            )
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error parseando documento ${doc.id}", e)
            null
        }
    }

    private fun parseAvailability(field: Any?): Availability {
        return when (field) {
            is List<*> -> {
                val timestamps = field.filterIsInstance<Timestamp>()
                if (timestamps.size >= 2) {
                    Availability(timestamps[0], timestamps[1])
                } else {
                    Availability()
                }
            }
            is Map<*, *> -> {
                val start = (field["start"] as? Timestamp) ?: Timestamp.now()
                val end = (field["end"] as? Timestamp) ?: Timestamp.now()
                Availability(start, end)
            }
            else -> Availability()
        }
    }
}