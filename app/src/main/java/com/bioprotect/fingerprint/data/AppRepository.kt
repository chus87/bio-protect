package com.bioprotect.fingerprint.data

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.bioprotect.fingerprint.model.AppInfo

class AppRepository(private val packageManager: PackageManager) {

    fun loadLaunchableApps(): List<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)

        return activities
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                val appInfo = activityInfo.applicationInfo
                AppInfo(
                    packageName = activityInfo.packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString(),
                    icon = resolveInfo.loadIcon(packageManager),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }
            .distinctBy { it.packageName }
    }
}
