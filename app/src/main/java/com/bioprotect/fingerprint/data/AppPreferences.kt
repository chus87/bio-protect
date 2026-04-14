package com.bioprotect.fingerprint.data

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "bioprotect_prefs"
    private const val KEY_PROTECTED_PACKAGES = "protected_packages"
    private const val KEY_PROTECTION_ENABLED = "protection_enabled"
    private const val KEY_HIDE_SYSTEM_APPS = "hide_system_apps"
    private const val KEY_AUTOSTART_CONFIRMED = "autostart_confirmed"
    private const val KEY_RESTRICTED_SETTINGS_CONFIRMED = "restricted_settings_confirmed"
    private const val KEY_ONBOARDING_DISMISSED = "onboarding_dismissed"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getProtectedPackages(): Set<String> = prefs.getStringSet(KEY_PROTECTED_PACKAGES, emptySet()) ?: emptySet()

    fun isProtected(packageName: String): Boolean = getProtectedPackages().contains(packageName)

    fun setProtected(packageName: String, protected: Boolean) {
        val updated = getProtectedPackages().toMutableSet()
        if (protected) updated.add(packageName) else updated.remove(packageName)
        prefs.edit().putStringSet(KEY_PROTECTED_PACKAGES, updated).apply()
    }

    fun isProtectionEnabled(): Boolean = prefs.getBoolean(KEY_PROTECTION_ENABLED, false)

    fun setProtectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }

    fun hideSystemApps(): Boolean = prefs.getBoolean(KEY_HIDE_SYSTEM_APPS, true)

    fun setHideSystemApps(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_SYSTEM_APPS, hide).apply()
    }

    fun isAutostartConfirmed(): Boolean = prefs.getBoolean(KEY_AUTOSTART_CONFIRMED, false)

    fun setAutostartConfirmed(confirmed: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOSTART_CONFIRMED, confirmed).apply()
    }

    fun isRestrictedSettingsConfirmed(): Boolean = prefs.getBoolean(KEY_RESTRICTED_SETTINGS_CONFIRMED, false)

    fun setRestrictedSettingsConfirmed(confirmed: Boolean) {
        prefs.edit().putBoolean(KEY_RESTRICTED_SETTINGS_CONFIRMED, confirmed).apply()
    }

    fun isOnboardingDismissed(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DISMISSED, false)

    fun setOnboardingDismissed(dismissed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DISMISSED, dismissed).apply()
    }

}
