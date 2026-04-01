package com.spacemint.app

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log

object QueueManager {

    private const val PREFS_NAME = "spacemint_queue"
    private const val KEY_SEEN   = "seen_ids"

    // ── GET NEXT BATCH ────────────────────────────────────────
    fun getNextBatch(context: Context, batchSize: Int = 5): List<ReviewFile> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // scan everything on phone
        val allFiles = scanAll(context)
        if (allFiles.isEmpty()) return emptyList()

        // get IDs already seen
        val seenIds = prefs.getStringSet(KEY_SEEN, mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()

        // unseen files
        var unseen = allFiles.filter { it.uri?.toString() !in seenIds }

        // if everything seen — reset and start fresh
        if (unseen.isEmpty()) {
            prefs.edit().remove(KEY_SEEN).apply()
            unseen = allFiles.shuffled()
            Log.d("QueueManager", "Full cycle done — resetting")
        }

        // take next batch randomly
        val batch = unseen.shuffled().take(batchSize)

        // mark batch as seen
        val newSeen = seenIds.toMutableSet()
        batch.forEach { file ->
            file.uri?.toString()?.let { id -> newSeen.add(id) }
        }
        prefs.edit().putStringSet(KEY_SEEN, newSeen).apply()

        Log.d("QueueManager", "${newSeen.size} seen of ${allFiles.size} total")
        return batch
    }

    // ── SCAN PHOTOS + VIDEOS ──────────────────────────────────
    private fun scanAll(context: Context): List<ReviewFile> {
        val all = mutableListOf<ReviewFile>()
        all.addAll(scanPhotos(context))
        all.addAll(scanVideos(context))
        return all
    }

    // ── PHOTOS ────────────────────────────────────────────────
    private fun scanPhotos(context: Context): List<ReviewFile> {
        val files = mutableListOf<ReviewFile>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${MediaStore.Images.Media.DATE_ADDED} ASC"
            )?.use { c ->
                val idCol   = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (c.moveToNext()) {
                    val id        = c.getLong(idCol)
                    val name      = c.getString(nameCol) ?: "Unknown"
                    val sizeBytes = c.getLong(sizeCol)
                    val dateSecs  = c.getLong(dateCol)

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )

                    files.add(ReviewFile(
                        name = name,
                        date = fmt(dateSecs),
                        size = fmtSize(sizeBytes),
                        type = if (name.lowercase().contains("screenshot"))
                            "Screenshot" else "Photo",
                        hint = hint(name, sizeBytes),
                        uri  = uri
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("QueueManager", "Photo scan error: ${e.message}")
        }

        return files
    }

    // ── VIDEOS ────────────────────────────────────────────────
    private fun scanVideos(context: Context): List<ReviewFile> {
        val files = mutableListOf<ReviewFile>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION
        )

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${MediaStore.Video.Media.DATE_ADDED} ASC"
            )?.use { c ->
                val idCol   = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durCol  = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                while (c.moveToNext()) {
                    val id        = c.getLong(idCol)
                    val name      = c.getString(nameCol) ?: "Unknown"
                    val sizeBytes = c.getLong(sizeCol)
                    val dateSecs  = c.getLong(dateCol)
                    val durSec    = c.getLong(durCol) / 1000

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )

                    files.add(ReviewFile(
                        name = name,
                        date = fmt(dateSecs),
                        size = fmtSize(sizeBytes),
                        type = "Video",
                        hint = when {
                            durSec < 5          -> "Very short clip — probably safe to delete"
                            sizeBytes > 500_000_000 -> "Large video — freeing this saves significant space"
                            else                -> "Old video — review if still needed"
                        },
                        uri  = uri
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("QueueManager", "Video scan error: ${e.message}")
        }

        return files
    }

    // ── HELPERS ───────────────────────────────────────────────
    private fun fmt(seconds: Long): String {
        val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(seconds * 1000))
    }

    private fun fmtSize(bytes: Long): String = when {
        bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000     -> "%.1f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000         -> "%.0f KB".format(bytes / 1_000.0)
        else                   -> "$bytes B"
    }

    private fun hint(name: String, size: Long): String {
        val l = name.lowercase()
        return when {
            l.contains("screenshot") -> "Screenshot — safe to delete if no longer needed"
            l.contains("whatsapp")   -> "WhatsApp media — check if already backed up"
            l.contains("img_")       -> "Camera photo — check if similar ones exist"
            l.contains("received")   -> "Received file — may be a forward you no longer need"
            size > 10_000_000        -> "Large file — deleting frees significant space"
            else                     -> "Old file — consider if you still need this"
        }
    }
}