package com.pigs.borrowit.data.repositories

import android.util.Log
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

    suspend fun updateMemberRole(communityId: String, userId: String, newRole: String): Result<Unit> {
        return try {
            communitiesRef.document(communityId).collection("members").document(userId)
                .update("role", newRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeMemberFromCommunity(communityId: String, userId: String): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val commRef = communitiesRef.document(communityId)
                val memberRef = commRef.collection("members").document(userId)
                
                val snapshot = transaction.get(commRef)
                val currentCount = snapshot.getLong("memberCount") ?: 0
                
                transaction.update(commRef, "memberCount", if (currentCount > 0) currentCount - 1 else 0)
                transaction.delete(memberRef)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCommunity(communityId: String): Result<Unit> {
        return try {
            // Se elimina el documento raíz. En una implementación más robusta se deberían 
            // borrar también las subcolecciones, pero borrar el raíz lo quita de las listas principales.
            communitiesRef.document(communityId).delete().await()
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
        val result = mutableSetOf<Community>()
        try {
            // 1. Fetch communities where user is creator
            val createdSnapshot = communitiesRef.whereEqualTo("creatorId", userId).get().await()
            result.addAll(createdSnapshot.toObjects(Community::class.java))
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching created communities", e)
        }

        try {
            // 2. Fetch communities where user is a member
            val memberships = db.collectionGroup("members")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val communityIds = memberships.documents.mapNotNull { it.reference.parent.parent?.id }
            val existingIds = result.map { it.id }.toSet()
            val missingIds = communityIds.filter { it !in existingIds }
            
            if (missingIds.isNotEmpty()) {
                missingIds.chunked(10).forEach { chunk ->
                    val snapshot = communitiesRef.whereIn("__name__", chunk).get().await()
                    result.addAll(snapshot.toObjects(Community::class.java))
                }
            }
        } catch (e: Exception) {
            Log.e("CommunityRepository", "Error fetching membership communities", e)
        }
        
        return result.toList().sortedByDescending { it.updatedAt }
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
