package com.example.ruvo_app.domain.service

import com.example.ruvo_app.domain.model.ServicePost

interface TrustOptimizerService {
    suspend fun analyzePost(post: ServicePost): Result<TrustAnalysisResult>
}

data class TrustAnalysisResult(
    val score: Double,
    val textScore: Int,
    val imageScore: Int,
    val summary: String,
    val status: PostStatusUpdate,
    val details: Map<String, String> = emptyMap(),
    val aiConfidence: Double = 1.0, // Nivel de certeza de la IA (0.0 a 1.0)
    val riskHighlights: List<String> = emptyList() // Frases o palabras sospechosas detectadas
)

enum class PostStatusUpdate {
    APROBADO,
    REVISION_MANUAL,
    REVISION_MANUAL_PRIORITARIA,
    RECHAZADO
}
