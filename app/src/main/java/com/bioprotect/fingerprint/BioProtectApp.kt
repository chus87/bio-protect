package com.bioprotect.fingerprint

import android.app.Application
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.util.EventLogger
import com.bioprotect.fingerprint.util.ProtectionManager

class BioProtectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this)
        EventLogger.init(this)
        EventLogger.log("App iniciada")
        ProtectionManager.syncForegroundService(this)
    }
}
