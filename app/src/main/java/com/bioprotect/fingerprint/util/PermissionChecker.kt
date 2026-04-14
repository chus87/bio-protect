package com.bioprotect.fingerprint.util

import android.app.Activity
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import com.bioprotect.fingerprint.R
import com.bioprotect.fingerprint.receiver.BioProtectDeviceAdminReceiver

object PermissionChecker {

    fun hasAccessibilityPermission(context: Context): Boolean {
        val expected = ComponentName(context, com.bioprotect.fingerprint.service.BioProtectAccessibilityService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasIgnoreBatteryOptimization(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun hasDeviceAdmin(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(context, BioProtectDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(component)
    }

    fun hasAllEssentialPermissions(context: Context): Boolean {
        return hasAccessibilityPermission(context) &&
            hasUsageAccessPermission(context) &&
            hasOverlayPermission(context) &&
            hasIgnoreBatteryOptimization(context) &&
            hasDeviceAdmin(context)
    }

    fun openAccessibilitySettings(context: Context) {
        launch(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openUsageAccessSettings(context: Context) {
        launch(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun openOverlaySettings(context: Context) {
        launch(
            context,
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
        )
    }

    fun openBatterySettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}")),
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        )
        launchFirstResolvable(context, intents, R.string.battery_unavailable)
    }

    fun openDeviceAdminSettings(context: Context) {
        val component = ComponentName(context, BioProtectDeviceAdminReceiver::class.java)
        val directIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                context.getString(R.string.device_admin_description)
            )
        }

        val fallbacks = listOf(
            directIntent,
            Intent(Settings.ACTION_SECURITY_SETTINGS),
            Intent(Settings.ACTION_PRIVACY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        launchFirstResolvable(context, fallbacks, R.string.admin_unavailable, preferDirect = true)
    }

    fun openRestrictedSettings(context: Context) {
        launch(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    fun openAutoStartSettings(context: Context) {
        val intents = buildList {
            add(Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            })
            add(Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity"
                )
                putExtra("extra_pkgname", context.packageName)
            })
            add(Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.appmanager.ApplicationsDetailsActivity"
                )
                putExtra("package_name", context.packageName)
                putExtra("pkg_name", context.packageName)
            })
            add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
        }
        launchFirstResolvable(context, intents, R.string.autostart_unavailable)
    }

    fun openSecuritySettings(context: Context) {
        launch(context, Intent(Settings.ACTION_SECURITY_SETTINGS))
    }

    fun openAppDetails(context: Context) {
        launch(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
    }

    private fun launch(context: Context, intent: Intent) {
        val finalIntent = prepareIntent(context, intent)
        context.startActivity(finalIntent)
    }

    private fun launchFirstResolvable(
        context: Context,
        intents: List<Intent>,
        errorRes: Int,
        preferDirect: Boolean = false
    ) {
        val pm = context.packageManager
        val chosen = if (preferDirect) {
            intents.firstOrNull { canLaunch(pm, prepareIntent(context, it), allowMissingResolveForDeviceAdmin = true) }
        } else {
            intents.firstOrNull { canLaunch(pm, prepareIntent(context, it), allowMissingResolveForDeviceAdmin = false) }
        }
        if (chosen == null) {
            Toast.makeText(context, context.getString(errorRes), Toast.LENGTH_SHORT).show()
            return
        }
        try {
            context.startActivity(prepareIntent(context, chosen))
        } catch (_: Exception) {
            Toast.makeText(context, context.getString(errorRes), Toast.LENGTH_SHORT).show()
        }
    }

    private fun canLaunch(
        packageManager: PackageManager,
        intent: Intent,
        allowMissingResolveForDeviceAdmin: Boolean
    ): Boolean {
        if (allowMissingResolveForDeviceAdmin && intent.action == DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN) {
            return true
        }
        return intent.resolveActivity(packageManager) != null
    }

    private fun prepareIntent(context: Context, intent: Intent): Intent {
        return Intent(intent).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
