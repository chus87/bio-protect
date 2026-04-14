package com.bioprotect.fingerprint.model

data class PermissionItem(
    val key: String,
    val title: String,
    val stateText: String,
    val stateColorRes: Int,
    val openLabel: String,
    val openAction: () -> Unit,
    val secondaryLabel: String? = null,
    val secondaryAction: (() -> Unit)? = null
)
