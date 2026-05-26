package com.example.ruvo_app.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ruvo_app.domain.model.ImageResource
import com.example.ruvo_app.domain.repository.ImageStorageService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume

class CloudinaryStorageServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageStorageService {

    override suspend fun uploadImage(uri: Uri): Result<ImageResource> = suspendCancellableCoroutine { continuation ->
        val requestId = MediaManager.get().upload(uri)
            .option("folder", "ruvo_services")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    val publicId = resultData["public_id"] as? String ?: ""
                    
                    if (continuation.isActive) {
                        continuation.resume(Result.success(ImageResource(url, publicId)))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception(error.description)))
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }
}
