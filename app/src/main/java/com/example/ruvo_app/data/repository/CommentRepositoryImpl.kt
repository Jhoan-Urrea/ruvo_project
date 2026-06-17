package com.example.ruvo_app.data.repository

import com.example.ruvo_app.domain.model.Comment
import com.example.ruvo_app.domain.repository.CommentRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRepository {

    override suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            val docRef = firestore.collection("comments").document()
            val commentWithId = comment.copy(id = docRef.id)
            docRef.set(commentWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCommentsForPost(postId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { it.toObject(Comment::class.java) } ?: emptyList()
                trySend(comments)
            }
        awaitClose { subscription.remove() }
    }
}
