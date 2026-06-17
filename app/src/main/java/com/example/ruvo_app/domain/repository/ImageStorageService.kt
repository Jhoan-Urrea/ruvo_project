package com.example.ruvo_app.domain.repository

import android.net.Uri
import com.example.ruvo_app.domain.model.ImageResource

interface ImageStorageService {
    suspend fun uploadImage(uri: Uri): Result<ImageResource>
}
