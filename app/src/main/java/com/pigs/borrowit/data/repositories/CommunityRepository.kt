package com.pigs.borrowit.data.repositories

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

    // Fetches communities where the user is the creator
    // To fetch all communities the user belongs to, we would use a Collection Group query
    // or store a list of community IDs in the user's document.
    suspend fun getUserCommunities(userId: String): List<Community> {
        return try {
            communitiesRef.whereEqualTo("creatorId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Community::class.java)
        } catch (e: Exception) {
            // In case of index missing error, fallback to simple query
            try {
                communitiesRef.whereEqualTo("creatorId", userId)
                    .get()
                    .await()
                    .toObjects(Community::class.java)
            } catch (e2: Exception) {
                emptyList()
            }
        }
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
