package com.spacemint.app

data class ReviewFile(
    val name: String,
    val date: String,
    val size: String,
    val type: String,
    val hint: String,
    val uri: android.net.Uri? = null
)