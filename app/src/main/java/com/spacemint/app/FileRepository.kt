package com.spacemint.app

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object FileRepository {

    // ── READ PHOTOS ───────────────────────────────────────────
    fun loadPhotos(context: Context, limit: Int = 10): List<ReviewFile> {
        val files = mutableListOf<ReviewFile>()

        // columns we want from MediaStore — like SELECT these columns
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        // oldest first — most likely to be deletable
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val name      = c.getString(nameCol) ?: "Unknown"
                    val sizeBytes = c.getLong(sizeCol)
                    val dateSecs  = c.getLong(dateCol)

                    files.add(
                        ReviewFile(
                            name = name,
                            date = formatDate(dateSecs),
                            size = formatSize(sizeBytes),
                            type = if (name.lowercase().contains("screenshot"))
                                "Screenshot" else "Photo",
                            hint = buildHint(name, sizeBytes)
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error loading photos: ${e.message}")
        }

        return files
    }

    // ── READ VIDEOS ───────────────────────────────────────────
    fun loadVideos(context: Context, limit: Int = 5): List<ReviewFile> {
        val files = mutableListOf<ReviewFile>()

        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} ASC"
            )

            cursor?.use { c ->
                val nameCol     = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol     = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol     = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val name      = c.getString(nameCol) ?: "Unknown"
                    val sizeBytes = c.getLong(sizeCol)
                    val dateSecs  = c.getLong(dateCol)
                    val durationMs= c.getLong(durationCol)

                    val durationSec = durationMs / 1000
                    val hint = when {
                        durationSec < 5  -> "Very short clip — probably safe to delete"
                        sizeBytes > 500_000_000 -> "Large video — freeing this saves significant space"
                        else -> "Old video — review if still needed"
                    }

                    files.add(
                        ReviewFile(
                            name = name,
                            date = formatDate(dateSecs),
                            size = formatSize(sizeBytes),
                            type = "Video",
                            hint = hint
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error loading videos: ${e.message}")
        }

        return files
    }

    // ── READ DOWNLOADS ────────────────────────────────────────
    fun loadDownloads(context: Context, limit: Int = 5): List<ReviewFile> {
        val files = mutableListOf<ReviewFile>()

        val projection = arrayOf(
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.DATE_ADDED
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Downloads.DATE_ADDED} ASC"
            )

            cursor?.use { c ->
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED)

                var count = 0
                while (c.moveToNext() && count < limit) {
                    val name      = c.getString(nameCol) ?: "Unknown"
                    val sizeBytes = c.getLong(sizeCol)
                    val dateSecs  = c.getLong(dateCol)

                    val ext = name.substringAfterLast('.', "").lowercase()
                    val type = when(ext) {
                        "pdf"  -> "PDF"
                        "apk"  -> "APK"
                        "mp3","wav","m4a" -> "Audio"
                        "zip","rar" -> "Archive"
                        else   -> "Download"
                    }

                    files.add(
                        ReviewFile(
                            name = name,
                            date = formatDate(dateSecs),
                            size = formatSize(sizeBytes),
                            type = type,
                            hint = buildDownloadHint(ext, sizeBytes)
                        )
                    )
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("FileRepository", "Error loading downloads: ${e.message}")
        }

        return files
    }

    // ── COMBINE ALL — mix of photos, videos, downloads ────────
    fun loadMixed(context: Context, total: Int = 10): List<ReviewFile> {
        val photos    = loadPhotos(context, limit = total / 2)
        val videos    = loadVideos(context, limit = 2)
        val downloads = loadDownloads(context, limit = 3)

        return (photos + videos + downloads)
            .shuffled()
            .take(total)
            .ifEmpty {
                // fallback if no permission yet — return empty
                emptyList()
            }
    }

    // ── HELPERS ───────────────────────────────────────────────
    private fun formatSize(bytes: Long): String = when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000     -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000         -> "%.0f KB".format(bytes / 1_000.0)
        else                   -> "$bytes B"
    }

    private fun formatDate(seconds: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return sdf.format(Date(seconds * 1000))
    }

    private fun buildHint(name: String, sizeBytes: Long): String {
        val lower = name.lowercase()
        return when {
            lower.contains("screenshot") ->
                "Screenshot — safe to delete if no longer needed"
            lower.contains("whatsapp") ->
                "WhatsApp media — check if already backed up"
            lower.contains("img_") || lower.contains("dsc") ->
                "Camera photo — check if similar ones exist"
            lower.contains("received") ->
                "Received file — may be a forward you no longer need"
            sizeBytes > 10_000_000 ->
                "Large file — deleting will free significant space"
            sizeBytes < 100_000 ->
                "Small file — probably a thumbnail or temp file"
            else ->
                "Old photo — consider if you still need this"
        }
    }

    private fun buildDownloadHint(ext: String, sizeBytes: Long): String = when(ext) {
        "apk"  -> "APK installer — safe to delete after installing the app"
        "pdf"  -> "PDF document — check if still needed"
        "zip","rar" -> "Archive file — likely safe to delete after extracting"
        "mp3","m4a" -> "Audio file — check if backed up elsewhere"
        else   -> if (sizeBytes > 50_000_000)
            "Large download — freeing this saves significant space"
        else
            "Old download — review if still needed"
    }
}