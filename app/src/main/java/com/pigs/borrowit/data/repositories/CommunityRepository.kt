package com.pigs.borrowit.data.repositories

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pigs.borrowit.data.model.Community
import com.pigs.borrowit.data.model.CommunityItem
import com.pigs.borrowit.data.model.CommunityMember
import kotlinx.coroutines.tasks.await

import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CommunityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val communitiesRef = db.collection("communities")

    suspend fun uploadImage(imageData: ByteArray, path: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child("$path/$fileName")
        imageRef.putBytes(imageData).await()
        return imageRef.downloadUrl.await().toString()
    }

    suspend fun createCommunity(community: Community, creatorName: String): Result<String> {
        return try {
            val docRef = communitiesRef.document() // Auto-generated ID
            val newCommunity = community.copy(
                id = docRef.id,
                nameLowercase = community.name.lowercase()
            )
            
            db.runBatch { batch ->
                batch.set(docRef, newCommunity)
                val memberRef = docRef.collection("members").document(community.creatorId)
                batch.set(memberRef, CommunityMember(
                    userId = community.creatorId,
                    userName = creatorName,
                    role = "admin"
                ))
            }.await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addMemberToCommunity(communityId: String, userId: String, userName: String): Result<Unit> {
        return try {
            val memberRef = communitiesRef.document(communityId).collection("members").document(userId)
            val memberDoc = memberRef.get().await()
            
            if (memberDoc.exists()) {
                return Result.success(Unit) // Already a member
            }

            db.runTransaction { transaction ->
                val commRef = communitiesRef.document(communityId)
                val snapshot = transaction.get(commRef)
                val currentCount = snapshot.getLong("memberCount") ?: 0
                
                transaction.update(commRef, "memberCount", currentCount + 1)
                transaction.set(memberRef, CommunityMember(
                    userId = userId,
                    userName = userName,
                    role = "member"
                ))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCommunityItem(communityId: String, item: CommunityItem): Result<String> {
        return try {
            val docRef = communitiesRef.document(communityId).collection("communityItems").document()
            val newItem = item.copy(id = docRef.id)
            docRef.set(newItem).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCommunityMembers(communityId: String): List<CommunityMember> {
        return try {
            communitiesRef.document(communityId).collection("members")
                .get().await().toObjects(CommunityMember::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserCommunities(userId: String): List<Community> {
        val communitiesList = mutableListOf<Community>()
        val communityIds = mutableSetOf<String>()
        
        try {
            // 1. Intentar obtener IDs mediante la subcolección members (requiere el índice de tu captura)
            val memberships = db.collectionGroup("members")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            for (doc in memberships.documents) {
                // El padre de 'members' es el documento de la comunidad
                doc.reference.parent.parent?.id?.let { communityIds.add(it) }
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error en collectionGroup (índice en proceso o ausente)", e)
        }
        
        // 2. Por si acaso el índice falla o el usuario es el creador y aún no se indexó el miembro
        try {
            val created = communitiesRef.whereEqualTo("creatorId", userId).get().await()
            for (doc in created.documents) {
                communityIds.add(doc.id)
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error obteniendo comunidades creadas", e)
        }

        // 3. Obtener los objetos Community finales
        if (communityIds.isNotEmpty()) {
            try {
                // Firestore limita 'whereIn' a 10 elementos por consulta
                val idChunks = communityIds.toList().chunked(10)
                for (chunk in idChunks) {
                    val snapshot = communitiesRef.whereIn(FieldPath.documentId(), chunk).get().await()
                    communitiesList.addAll(snapshot.toObjects(Community::class.java))
                }
            } catch (e: Exception) {
                Log.e("CommunityRepository", "Error recuperando objetos comunidad", e)
            }
        }
        
        return communitiesList.distinctBy { it.id }.sortedByDescending { it.updatedAt }
    }

    suspend fun getCommunity(communityId: String): Community? {
        return try {
            communitiesRef.document(communityId).get().await().toObject(Community::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateCommunity(
        communityId: String,
        name: String,
        description: String,
        bannerUrl: String?,
        profileUrl: String?
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name,
                "nameLowercase" to name.lowercase(),
                "description" to description,
                "bannerUrl" to bannerUrl,
                "profileUrl" to profileUrl,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            communitiesRef.document(communityId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
