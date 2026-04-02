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

        val allFiles = scanAll(context)
        if (allFiles.isEmpty()) return emptyList()

        val seenIds = prefs.getStringSet(KEY_SEEN, mutableSetOf())
            ?.toMutableSet() ?: mutableSetOf()

        var unseen = allFiles.filter { it.uri?.toString() !in seenIds }

        if (unseen.isEmpty()) {
            prefs.edit().remove(KEY_SEEN).apply()
            unseen = allFiles.shuffled()
            Log.d("QueueManager", "Full cycle done — resetting")
        }

        // ── GET TARGET ────────────────────────────────────────────
        val targetMB = TargetManager.getCurrentTarget(context)
        val targetBytes = (targetMB * 1_000_000).toLong()

        // parse size string to bytes
        fun sizeToBytes(size: String): Long {
            val num = size
                .replace(" GB","").replace(" MB","")
                .replace(" KB","").replace(" B","")
                .toDoubleOrNull() ?: 0.0
            return when {
                size.contains("GB") -> (num * 1_000_000_000).toLong()
                size.contains("MB") -> (num * 1_000_000).toLong()
                size.contains("KB") -> (num * 1_000).toLong()
                else                -> num.toLong()
            }
        }

        // ── SMART BUCKET PICKING BASED ON TARGET ──────────────────
        val ultraLarge = unseen.filter { sizeToBytes(it.size) >= 50_000_000 }.shuffled()  // 50MB+
        val large      = unseen.filter { sizeToBytes(it.size) in 10_000_000..49_999_999 }.shuffled() // 10-50MB
        val medium     = unseen.filter { sizeToBytes(it.size) in 1_000_000..9_999_999 }.shuffled()   // 1-10MB
        val small      = unseen.filter { sizeToBytes(it.size) < 1_000_000 }.shuffled()    // under 1MB

        val batch = mutableListOf<ReviewFile>()

        when {
            // aggressive target 75MB+ — prioritise ultra large and large
            targetMB >= 75f -> {
                batch.addAll(ultraLarge.take(1))
                batch.addAll(large.take(2))
                batch.addAll(medium.take(1))
                batch.addAll(small.take(1))
            }
            // active target 50-75MB
            targetMB >= 50f -> {
                batch.addAll(ultraLarge.take(1))
                batch.addAll(large.take(1))
                batch.addAll(medium.take(2))
                batch.addAll(small.take(1))
            }
            // moderate target 25-50MB
            targetMB >= 25f -> {
                batch.addAll(large.take(1))
                batch.addAll(medium.take(2))
                batch.addAll(small.take(2))
            }
            // light target under 25MB
            else -> {
                batch.addAll(medium.take(2))
                batch.addAll(small.take(3))
            }
        }

        // fill remaining slots if buckets were empty
        if (batch.size < batchSize) {
            val picked = batch.map { it.uri?.toString() }.toSet()
            val remaining = unseen
                .filter { it.uri?.toString() !in picked }
                .shuffled()
                .take(batchSize - batch.size)
            batch.addAll(remaining)
        }

        val finalBatch = batch.shuffled()

        // mark as seen
        val newSeen = seenIds.toMutableSet()
        finalBatch.forEach { it.uri?.toString()?.let { id -> newSeen.add(id) } }
        prefs.edit().putStringSet(KEY_SEEN, newSeen).apply()

        // record actual MB shown for auto adjustment
        val actualMB = finalBatch.sumOf { sizeToBytes(it.size) } / 1_000_000f
        TargetManager.recordSession(context, actualMB)

        Log.d("QueueManager", "Target: $targetMB MB — Actual: $actualMB MB — Batch: ${finalBatch.size}")
        return finalBatch
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