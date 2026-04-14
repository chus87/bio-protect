package com.bioprotect.fingerprint.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.ui.LockScreenActivity
import com.bioprotect.fingerprint.util.EventLogger
import com.bioprotect.fingerprint.util.PermissionChecker
import com.bioprotect.fingerprint.util.SessionController

class BioProtectAccessibilityService : AccessibilityService() {

    private val excludedPackages = setOf(
        "com.android.systemui",
        "android",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.miui.securitycenter",
        "com.miui.home",
        "com.miui.systemAdSolution",
        "com.mi.android.globallauncher"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        EventLogger.log("Servicio de accesibilidad conectado")
        if (AppPreferences.isProtectionEnabled()) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, ProtectionForegroundService::class.java)
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return

        if (pkg == "android" || pkg == "com.android.systemui") {
            SessionController.markPendingRelock()
        }

        if (!AppPreferences.isProtectionEnabled()) return
        if (!PermissionChecker.hasAllEssentialPermissions(this)) return
        if (pkg == packageName || pkg in excludedPackages) return
        if (SessionController.isAuthInProgress()) return
        if (!AppPreferences.isProtected(pkg)) return
        if (SessionController.shouldIgnoreLoop(pkg)) return

        if (SessionController.hasPendingRelock()) {
            SessionController.clearPendingRelock()
            SessionController.clearTempAccess()
        }

        if (SessionController.canPass(pkg)) {
            SessionController.clearLastLockedPackage()
            return
        }

        if (!SessionController.canLaunchLockFor(pkg)) return

        SessionController.markLockShown(pkg)
        EventLogger.log("Bloqueo lanzado para $pkg")

        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, pkg)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        EventLogger.log("Servicio de accesibilidad interrumpido")
    }
}
