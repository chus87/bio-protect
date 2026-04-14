package com.bioprotect.fingerprint.util

object SessionController {
    @Volatile private var unlockedPackage: String? = null
    @Volatile private var allowedPackage: String? = null
    @Volatile private var allowedUntil: Long = 0L
    @Volatile private var ignorePackage: String? = null
    @Volatile private var ignoreUntil: Long = 0L
    @Volatile private var lastLockedPackage: String? = null
    @Volatile private var lockActivityShowing = false
    @Volatile private var authInProgress = false
    @Volatile private var pendingRelock = false
    @Volatile private var appPanelUnlocked = false

    @Synchronized
    fun isUnlocked(packageName: String): Boolean = unlockedPackage == packageName

    @Synchronized
    fun unlock(packageName: String) {
        unlockedPackage = packageName
        lockActivityShowing = false
        authInProgress = false
        lastLockedPackage = null
    }

    @Synchronized
    fun clearIfDifferent(packageName: String?) {
        if (unlockedPackage != null && unlockedPackage != packageName) {
            unlockedPackage = null
        }
        if (packageName == null) {
            lockActivityShowing = false
        }
    }

    @Synchronized
    fun canLaunchLockFor(packageName: String): Boolean {
        return !lockActivityShowing && lastLockedPackage != packageName
    }

    @Synchronized
    fun markLockShown(packageName: String) {
        lockActivityShowing = true
        lastLockedPackage = packageName
    }

    @Synchronized
    fun markLockDismissed() {
        lockActivityShowing = false
        authInProgress = false
    }

    @Synchronized
    fun clearLastLockedPackage() {
        lastLockedPackage = null
    }

    @Synchronized
    fun setAuthInProgress(inProgress: Boolean) {
        authInProgress = inProgress
    }

    @Synchronized
    fun isAuthInProgress(): Boolean = authInProgress

    @Synchronized
    fun markPendingRelock() {
        pendingRelock = true
    }

    @Synchronized
    fun hasPendingRelock(): Boolean = pendingRelock

    @Synchronized
    fun clearPendingRelock() {
        pendingRelock = false
    }

    @Synchronized
    fun allowPackage(packageName: String, millis: Long = 2500L) {
        val until = System.currentTimeMillis() + millis
        allowedPackage = packageName
        allowedUntil = until
        ignorePackage = packageName
        ignoreUntil = until
    }

    @Synchronized
    fun canPass(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        val ok = allowedPackage == packageName && now <= allowedUntil
        if (ok || now > allowedUntil) {
            allowedPackage = null
            allowedUntil = 0L
        }
        return ok
    }

    @Synchronized
    fun shouldIgnoreLoop(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        val ok = ignorePackage == packageName && now <= ignoreUntil
        if (!ok && now > ignoreUntil) {
            ignorePackage = null
            ignoreUntil = 0L
        }
        return ok
    }

    @Synchronized
    fun clearTempAccess() {
        allowedPackage = null
        allowedUntil = 0L
        ignorePackage = null
        ignoreUntil = 0L
    }

    @Synchronized
    fun setAppPanelUnlocked(unlocked: Boolean) {
        appPanelUnlocked = unlocked
    }

    @Synchronized
    fun isAppPanelUnlocked(): Boolean = appPanelUnlocked
}
