package com.spacemint.app

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import android.util.Log

data class BinItem(
    val name: String,
    val size: String,
    val type: String,
    val deletedDate: String,
    val daysRemaining: Int = 7,
    val uri: android.net.Uri? = null
)

object BinManager {
    var isComingFromReview = false

    private val _items = mutableListOf<BinItem>()
    val items: List<BinItem> get() = _items.toList()

    fun addToBin(file: ReviewFile) {
        val today = java.text.SimpleDateFormat(
            "d MMM yyyy", java.util.Locale.getDefault()
        ).format(java.util.Date())

        _items.add(
            BinItem(
                name         = file.name,
                size         = file.size,
                type         = file.type,
                deletedDate  = today,
                daysRemaining= 7,
                uri          = file.uri
            )
        )
    }

    fun restore(item: BinItem) {
        _items.remove(item)
    }

    fun emptyBin() {
        _items.clear()
    }

    fun count(): Int = _items.size

    fun totalSize(): String {
        var totalMB = 0.0
        _items.forEach { item ->
            val num = item.size
                .replace(" MB", "").replace(" GB", "").replace(" KB", "")
                .toDoubleOrNull() ?: 0.0
            totalMB += when {
                item.size.contains("GB") -> num * 1000
                item.size.contains("KB") -> num / 1000
                else                     -> num
            }
        }
        return "%.1f MB".format(totalMB)
    }

    // ── ACTUALLY DELETE FROM PHONE STORAGE ────────────────────
    fun deleteFromStorage(
        context: Context,
        item: BinItem,
        onSuccess: () -> Unit,
        onNeedPermission: (IntentSender) -> Unit
    ) {
        if (item.uri == null) {
            _items.remove(item)
            onSuccess()
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ — need to request delete permission via system popup
                val uris = listOf(item.uri)
                val pendingIntent = MediaStore.createDeleteRequest(
                    context.contentResolver, uris
                )
                // this triggers the system "Allow delete?" popup
                onNeedPermission(pendingIntent.intentSender)
            } else {
                // Android 10 and below — direct delete
                val deleted = context.contentResolver.delete(
                    item.uri, null, null
                )
                if (deleted > 0) {
                    _items.remove(item)
                    onSuccess()
                    Log.d("BinManager", "Deleted: ${item.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("BinManager", "Delete error: ${e.message}")
        }
    }

    // ── DELETE ALL IN BIN FROM STORAGE ────────────────────────
    fun deleteAllFromStorage(
        context: Context,
        onNeedPermission: (IntentSender) -> Unit
    ) {
        val uris = _items.mapNotNull { it.uri }
        if (uris.isEmpty()) {
            _items.clear()
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createDeleteRequest(
                    context.contentResolver, uris
                )
                onNeedPermission(pendingIntent.intentSender)
            } else {
                uris.forEach { uri ->
                    context.contentResolver.delete(uri, null, null)
                }
                _items.clear()
            }
        } catch (e: Exception) {
            Log.e("BinManager", "Delete all error: ${e.message}")
        }
    }
}