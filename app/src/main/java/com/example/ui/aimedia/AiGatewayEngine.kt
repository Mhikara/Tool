package com.example.ui.aimedia

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data Request Model for Text-Guided Image Edit (Inpainting)
 */
data class ImageEditRequest(
    val prompt: String,
    val sourceImageName: String,
    val hasMask: Boolean = false,
    val maskBounds: String = "",
    val outputFormat: String = "PNG",
    val quality: Int = 90
)

/**
 * Data Request Model for AI Video Generation (Text/Image-To-Video)
 */
data class VideoGenerateRequest(
    val mode: VideoGenMode,
    val prompt: String,
    val durationSeconds: Int, // Must be 1 to 10 seconds
    val sourceImageName: String? = null,
    val resolution: String = "1080p",
    val fps: Int = 30,
    val applyWatermark: Boolean = true
)

enum class VideoGenMode {
    TEXT_TO_VIDEO,
    IMAGE_TO_VIDEO
}

/**
 * Project Draft / Result Item stored in local cache (maker_projects)
 */
data class MediaProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MediaType,
    val prompt: String,
    val durationSec: Int = 0,
    val mediaUrl: String,
    val originalImageUrl: String? = null,
    val timestamp: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
    val aiWatermarkApplied: Boolean = true,
    val status: String = "Selesai"
)

enum class MediaType {
    EDITED_IMAGE,
    GENERATED_VIDEO
}

/**
 * Validation Result Object
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * AI Gateway Engine - Handles Request Validation, Fallback Provider Routing, Safety Filtering, and Watermarking
 */
object AiGatewayEngine {

    private val projectsCache = mutableListOf<MediaProjectItem>()

    init {
        // Initial sample projects in draft cache
        projectsCache.add(
            MediaProjectItem(
                title = "Edit Foto Langit Malam",
                type = MediaType.EDITED_IMAGE,
                prompt = "Ubah langit jadi malam berbintang dengan aurora borealis",
                mediaUrl = "sample_edited_starry_night",
                originalImageUrl = "sample_original_landscape"
            )
        )
        projectsCache.add(
            MediaProjectItem(
                title = "Animasi Air Mengalir",
                type = MediaType.GENERATED_VIDEO,
                prompt = "Buat pemandangan air terjun ini bergerak mengalir secara alami",
                durationSec = 5,
                mediaUrl = "sample_generated_waterfall_video",
                originalImageUrl = "sample_original_waterfall"
            )
        )
    }

    /**
     * Client & Server Request Validator for Video Duration (1-10s requirement)
     */
    fun validateVideoRequest(request: VideoGenerateRequest): ValidationResult {
        if (request.durationSeconds < 1 || request.durationSeconds > 10) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Durasi video tidak valid! Durasi harus berada dalam rentang 1 hingga 10 detik (Diterima: ${request.durationSeconds}s)."
            )
        }
        if (request.prompt.isBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Prompt deskripsi video tidak boleh kosong."
            )
        }
        if (request.mode == VideoGenMode.IMAGE_TO_VIDEO && request.sourceImageName.isNullOrBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Pilih foto sumber terlebih dahulu untuk mode Foto + Teks ke Video."
            )
        }
        return ValidationResult(isValid = true)
    }

    /**
     * Safety & Content Filter Check
     */
    fun checkSafetyFilter(prompt: String): ValidationResult {
        val lower = prompt.lowercase()
        val blockedKeywords = listOf(
            "deepfake", "nude", "porn", "explicit", "minor", "child",
            "kekerasan", "darah", "senjata", "illegal", "hate speech"
        )
        for (keyword in blockedKeywords) {
            if (lower.contains(keyword)) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Permintaan ditolak oleh Safety & Content Filter AI Gateway. Prompt mengandung konten yang tidak diizinkan ($keyword)."
                )
            }
        }
        return ValidationResult(isValid = true)
    }

    /**
     * Simulated Async Process for Text-Guided Image Editing
     */
    suspend fun processImageEdit(
        request: ImageEditRequest,
        onProgress: (String, Float) -> Unit
    ): MediaProjectItem {
        val safetyCheck = checkSafetyFilter(request.prompt)
        if (!safetyCheck.isValid) {
            throw IllegalArgumentException(safetyCheck.errorMessage)
        }

        onProgress("Terhubung ke AI Gateway Router...", 0.15f)
        delay(600)

        onProgress("Memeriksa format & area masking...", 0.35f)
        delay(700)

        onProgress("Generasi inpainting dengan Primary AI Provider...", 0.65f)
        delay(900)

        onProgress("Menyematkan watermark & metadata AI-Generated...", 0.90f)
        delay(500)

        val project = MediaProjectItem(
            title = "Edit: " + request.prompt.take(24) + "...",
            type = MediaType.EDITED_IMAGE,
            prompt = request.prompt,
            mediaUrl = "edited_result_" + System.currentTimeMillis(),
            originalImageUrl = request.sourceImageName
        )

        projectsCache.add(0, project)
        onProgress("Proses Edit Foto Selesai!", 1.0f)
        return project
    }

    /**
     * Simulated Async Background Process for Video Generation (WorkManager style)
     */
    suspend fun processVideoGeneration(
        request: VideoGenerateRequest,
        onProgress: (String, Float, Int) -> Unit
    ): MediaProjectItem {
        // Step 1: Validate Duration (1-10s)
        val durationValidation = validateVideoRequest(request)
        if (!durationValidation.isValid) {
            throw IllegalArgumentException(durationValidation.errorMessage)
        }

        // Step 2: Safety check
        val safetyCheck = checkSafetyFilter(request.prompt)
        if (!safetyCheck.isValid) {
            throw IllegalArgumentException(safetyCheck.errorMessage)
        }

        val totalSteps = 5
        val estSecondsPerSecVideo = 2 // ~2 sec per second of video
        val totalEstTime = request.durationSeconds * estSecondsPerSecVideo

        onProgress("Menerima permintaan & otentikasi AI Gateway...", 0.1f, totalEstTime)
        delay(800)

        onProgress("Validasi parameter durasi (${request.durationSeconds} detik)...", 0.25f, (totalEstTime * 0.8).toInt())
        delay(800)

        onProgress("Generasi frame gerakan video via Diffusion Model...", 0.55f, (totalEstTime * 0.5).toInt())
        delay(1200)

        onProgress("Encoding MP4 (${request.resolution}, ${request.fps}fps)...", 0.80f, (totalEstTime * 0.2).toInt())
        delay(1000)

        onProgress("Penyematan AI Watermark & Metadata Transparansi...", 0.95f, 1)
        delay(600)

        val project = MediaProjectItem(
            title = "Video (${request.durationSeconds}s): " + request.prompt.take(20) + "...",
            type = MediaType.GENERATED_VIDEO,
            prompt = request.prompt,
            durationSec = request.durationSeconds,
            mediaUrl = "video_result_" + System.currentTimeMillis()
        )

        projectsCache.add(0, project)
        onProgress("Generasi Video Berhasil!", 1.0f, 0)
        return project
    }

    fun getProjectsHistory(): List<MediaProjectItem> {
        return projectsCache.toList()
    }

    fun addProjectToHistory(item: MediaProjectItem) {
        projectsCache.add(0, item)
    }

    fun deleteProject(id: String) {
        projectsCache.removeAll { it.id == id }
    }
}
