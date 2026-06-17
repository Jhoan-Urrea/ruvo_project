package com.example.ruvo_app.domain.repository

import com.example.ruvo_app.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    suspend fun addComment(comment: Comment): Result<Unit>
    fun getCommentsForPost(postId: String): Flow<List<Comment>>
}
