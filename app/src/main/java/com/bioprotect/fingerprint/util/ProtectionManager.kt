package com.bioprotect.fingerprint.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.service.ProtectionForegroundService

object ProtectionManager {
    fun syncForegroundService(context: Context) {
        if (AppPreferences.isProtectionEnabled() && PermissionChecker.hasAllEssentialPermissions(context)) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ProtectionForegroundService::class.java)
            )
        } else {
            context.stopService(Intent(context, ProtectionForegroundService::class.java))
        }
    }
}
