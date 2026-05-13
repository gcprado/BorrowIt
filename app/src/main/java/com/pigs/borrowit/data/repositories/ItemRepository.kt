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

    suspend fun migrateAvailabilityField() {
        val snapshot: QuerySnapshot = itemsCollection.get(Source.SERVER).await()
        snapshot.documents.forEach { doc ->
            val availabilityField = doc.get("availability")
            if (availabilityField is List<*>) {
                val list = availabilityField.filterIsInstance<Timestamp>()  // Ahora sí es el Timestamp correcto
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
        val document = itemsCollection.document() // ID autogenerado
        document.set(item.toMap())
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    // Versión con corutinas (suspend)
    suspend fun addItemSuspend(item: Item): Result<String> {
        return try {
            val document = itemsCollection.document()
            document.set(item.toMap()).await()
            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Flow para observar la lista de ítems (opcional)
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
        val snapshotListener = itemsCollection
            .whereEqualTo("owner", ownerId)
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
            val owner = doc.getString("owner") ?: ""
            val condition = doc.getString("condition") ?: ""
            val picture = doc.getString("picture") ?: ""
            val availability = parseAvailability(doc.get("availability"))
            val communityId = doc.getString("communityId") ?: ""

            Item(
                id = doc.id,
                name = name,
                description = description,
                owner = owner,
                condition = condition,
                picture = picture,
                availability = availability,
                communityId = communityId
            )
        } catch (e: Exception) {
            Log.e("ItemRepository", "Error parseando documento ${doc.id}", e)
            null
        }
    }

    // Función auxiliar que acepta tanto Array como Map
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
