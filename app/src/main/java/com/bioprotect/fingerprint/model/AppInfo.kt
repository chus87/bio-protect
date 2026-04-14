package com.bioprotect.fingerprint.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isSystemApp: Boolean
)
