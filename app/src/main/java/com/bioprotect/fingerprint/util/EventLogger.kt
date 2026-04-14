package com.bioprotect.fingerprint.util

import android.content.Context

object EventLogger {
    fun init(context: Context) = Unit

    fun log(message: String) = Unit

    fun export(context: Context, header: String = ""): String = ""
}
