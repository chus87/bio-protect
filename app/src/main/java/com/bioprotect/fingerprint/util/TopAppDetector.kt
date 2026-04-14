package com.bioprotect.fingerprint.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

object TopAppDetector {
    fun getTopPackage(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 15_000
        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        var result: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && !event.packageName.isNullOrBlank()) {
                result = event.packageName
            }
        }
        return result
    }
}
