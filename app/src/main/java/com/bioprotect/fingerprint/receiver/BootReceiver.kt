package com.bioprotect.fingerprint.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.util.EventLogger
import com.bioprotect.fingerprint.util.PermissionChecker
import com.bioprotect.fingerprint.util.ProtectionManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        EventLogger.init(context)
        AppPreferences.init(context)
        EventLogger.log("Broadcast recibido: ${intent?.action}")
        if (AppPreferences.isProtectionEnabled() && PermissionChecker.hasAllEssentialPermissions(context)) {
            ProtectionManager.syncForegroundService(context)
        }
    }
}
