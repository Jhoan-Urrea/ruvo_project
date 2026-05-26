package com.example.ruvo_app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.ruvo_app.BuildConfig
import com.example.ruvo_app.domain.model.ServicePost
import com.example.ruvo_app.domain.service.PostStatusUpdate
import com.example.ruvo_app.domain.service.TrustAnalysisResult
import com.example.ruvo_app.domain.service.TrustOptimizerService
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject

class TrustOptimizerServiceImpl @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : TrustOptimizerService {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val forbiddenPatterns = listOf(
        "(?i)webcam", "(?i)escort", "(?i)dinero rápido", "(?i)invierte y gana", 
        "(?i)bitcoins", "(?i)pago anticipado", "(?i)whatsapp:?\\s*\\d{7,}",
        "(?i)conviértete en tu propio jefe", "(?i)ganancias garantizadas"
    )

    override suspend fun analyzePost(post: ServicePost): Result<TrustAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val fullText = "${post.title} ${post.description}"
            if (forbiddenPatterns.any { Regex(it).containsMatchIn(fullText) }) {
                return@withContext Result.success(
                    TrustAnalysisResult(
                        score = 0.0,
                        textScore = 0,
                        imageScore = 0,
                        summary = "RECHAZO: Patrón de fraude o contenido no permitido detectado.",
                        status = PostStatusUpdate.RECHAZADO,
                        details = mapOf("alerta" to "Contenido prohibido detectado por filtro local."),
                        aiConfidence = 1.0,
                        riskHighlights = listOf(post.title, post.description).filter { text ->
                            forbiddenPatterns.any { Regex(it).containsMatchIn(text) }
                        }
                    )
                )
            }

            val imageUrl = post.images.find { it.isPrimary }?.url ?: post.images.firstOrNull()?.url
            val bitmap = imageUrl?.let { downloadBitmap(it) }

            val prompt = """
                ERES EL AUDITOR DE SEGURIDAD DE RUVO. Evalúa esta oferta de servicio para prevenir estafas y spam.
                
                DATOS:
                - Título: ${post.title}
                - Descripción: ${post.description}
                - Categoría: ${post.category.name}
                
                REGLAS CRÍTICAS:
                1. RECHAZA (score 0) si pide dinero por adelantado, promete ingresos mágicos o es contenido adulto.
                2. REVISIÓN MANUAL si el título no coincide con la descripción o si la descripción es muy vaga.
                3. VALORA POSITIVAMENTE el lenguaje profesional y la claridad en lo que se ofrece.
                
                RESPONDE EXCLUSIVAMENTE EN JSON:
                {
                  "score_texto": (0-100),
                  "score_imagen": (0-100, 100 si no hay imagen),
                  "resumen": "Máx 10 palabras sobre la veracidad del contenido",
                  "alerta_seguridad": "Breve nota si hay sospecha de fraude",
                  "evidencia_tecnica": "Nota para el moderador sobre por qué se asignó este puntaje",
                  "ai_confidence": (0.0 a 1.0, nivel de certeza de tu análisis),
                  "risk_highlights": ["lista", "de frases", "exactas", "del texto", "que te parecieron", "sospechosas"]
                }
            """.trimIndent()

            val response = if (bitmap != null) {
                val resizedBitmap = resizeBitmap(bitmap, 512)
                generativeModel.generateContent(
                    content {
                        image(resizedBitmap)
                        text(prompt)
                    }
                )
            } else {
                generativeModel.generateContent(prompt)
            }

            val responseText = response.text ?: throw Exception("Sin respuesta")
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            
            val json = JSONObject(cleanJson)
            val textScore = json.getInt("score_texto")
            val imageScore = if (bitmap != null) json.getInt("score_imagen") else 100
            val summary = json.getString("resumen")
            val alerta = json.optString("alerta_seguridad", "")
            val evidencia = json.optString("evidencia_tecnica", "")
            val confidence = json.optDouble("ai_confidence", 0.8)
            
            val highlightsJson = json.optJSONArray("risk_highlights")
            val highlights = mutableListOf<String>()
            if (highlightsJson != null) {
                for (i in 0 until highlightsJson.length()) {
                    highlights.add(highlightsJson.getString(i))
                }
            }

            val finalScore = (textScore * 0.7) + (imageScore * 0.3)

            val status = when {
                finalScore > 90 -> PostStatusUpdate.APROBADO
                finalScore >= 65 -> PostStatusUpdate.REVISION_MANUAL
                finalScore >= 35 -> PostStatusUpdate.REVISION_MANUAL_PRIORITARIA
                else -> PostStatusUpdate.RECHAZADO
            }

            val details = mutableMapOf<String, String>()
            if (alerta.isNotEmpty()) details["alerta"] = alerta
            if (evidencia.isNotEmpty()) details["evidencia"] = evidencia

            Result.success(
                TrustAnalysisResult(
                    score = finalScore,
                    textScore = textScore,
                    imageScore = imageScore,
                    summary = summary,
                    status = status,
                    details = details,
                    aiConfidence = confidence,
                    riskHighlights = highlights
                )
            )
        } catch (e: Exception) {
            Result.success(
                TrustAnalysisResult(
                    score = 50.0,
                    textScore = 50,
                    imageScore = 50,
                    summary = "Pendiente de validación por error en análisis automático.",
                    status = PostStatusUpdate.REVISION_MANUAL,
                    details = mapOf("error" to (e.message ?: "Error desconocido")),
                    aiConfidence = 0.0,
                    riskHighlights = emptyList()
                )
            )
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        val width = source.width
        val height = source.height
        val aspectRatio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxLength
            targetHeight = (maxLength / aspectRatio).toInt()
        } else {
            targetHeight = maxLength
            targetWidth = (maxLength * aspectRatio).toInt()
        }
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
