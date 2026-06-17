package com.example.ruvo_app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ruvo_app.domain.model.ImageResource
import com.example.ruvo_app.domain.repository.ImageStorageService
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class CloudinaryImageService @Inject constructor(
    private val context: Context
) : ImageStorageService {

    override suspend fun uploadImage(uri: Uri): Result<ImageResource> = suspendCancellableCoroutine { continuation ->
        Log.d("CLOUDINARY_DEBUG", "Iniciando subida para URI: $uri")
        
        try {
            MediaManager.get().upload(uri)
                .unsigned("ruvo_app")
                .option("folder", "services_posts")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("CLOUDINARY_DEBUG", "Upload iniciado. RequestId: $requestId")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        val progress = if (totalBytes > 0) (bytes.toDouble() / totalBytes * 100).toInt() else 0
                        Log.d("CLOUDINARY_DEBUG", "Progreso: $progress% ($bytes/$totalBytes)")
                    }

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String ?: ""
                        val publicId = resultData["public_id"] as? String ?: ""
                        
                        Log.d("CLOUDINARY_DEBUG", "Upload EXITOSO: URL=$url, PublicId=$publicId")
                        
                        if (continuation.isActive) {
                            continuation.resume(Result.success(ImageResource(url, publicId)))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val errorMessage = error?.description ?: "Error desconocido"
                        val errorCode = error?.code ?: -1
                        Log.e("CLOUDINARY_DEBUG", "Error en upload ($errorCode): $errorMessage")
                        
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Cloudinary Error $errorCode: $errorMessage")))
                        }
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.w("CLOUDINARY_DEBUG", "Upload reprogramado: ${error?.description}")
                        // Note: Depending on logic, you might want to wait or fail here.
                        // For now, we allow it to continue or fail on timeout.
                    }
                })
                .dispatch(context)
        } catch (e: Exception) {
            Log.e("CLOUDINARY_DEBUG", "Excepción al intentar llamar al SDK: ${e.message}")
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }
}
