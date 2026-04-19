package com.pigs.borrowit.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.pigs.borrowit.data.model.Community
import com.pigs.borrowit.data.model.CommunityItem
import com.pigs.borrowit.data.model.CommunityMember
import kotlinx.coroutines.tasks.await

class CommunityRepository {
    private val db = FirebaseFirestore.getInstance()
    private val communitiesRef = db.collection("communities")

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
}
