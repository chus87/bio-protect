package com.bioprotect.fingerprint.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.bioprotect.fingerprint.util.EventLogger

class BioProtectDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        EventLogger.init(context)
        EventLogger.log("Administrador del dispositivo activado")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        EventLogger.init(context)
        EventLogger.log("Administrador del dispositivo desactivado")
    }
}
