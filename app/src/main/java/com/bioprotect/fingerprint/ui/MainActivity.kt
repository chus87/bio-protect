package com.bioprotect.fingerprint.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bioprotect.fingerprint.R
import com.bioprotect.fingerprint.data.AppPreferences
import com.bioprotect.fingerprint.data.AppRepository
import com.bioprotect.fingerprint.databinding.ActivityMainBinding
import com.bioprotect.fingerprint.databinding.RowPermissionBinding
import com.bioprotect.fingerprint.model.AppInfo
import com.bioprotect.fingerprint.model.PermissionItem
import com.bioprotect.fingerprint.util.PermissionChecker
import com.bioprotect.fingerprint.util.ProtectionManager
import com.bioprotect.fingerprint.util.SessionController
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: AppRepository
    private lateinit var adapter: AppListAdapter

    private var allApps: List<AppInfo> = emptyList()
    private var currentFilter: Filter = Filter.ALL
    private var appEntryPromptShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.init(this)
        SessionController.setAppPanelUnlocked(false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = AppRepository(packageManager)
        adapter = AppListAdapter { appInfo, isProtected ->
            AppPreferences.setProtected(appInfo.packageName, isProtected)
            refreshAppList()
            refreshUiState()
        }

        binding.appsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appsRecyclerView.adapter = adapter

        binding.permissionsSectionButton.setOnClickListener {
            val show = binding.permissionContainer.visibility != View.VISIBLE
            binding.permissionContainer.visibility = if (show) View.VISIBLE else View.GONE
            binding.permissionsSectionButton.text = getString(
                if (show) R.string.ocultar_configuracion_necesaria else R.string.ver_configuracion_necesaria
            )
        }

        binding.dismissOnboardingButton.setOnClickListener {
            AppPreferences.setOnboardingDismissed(true)
            renderOnboardingCard()
        }

        binding.filterToggleGroup.check(binding.filterAllButton.id)
        binding.filterToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            currentFilter = when (checkedId) {
                binding.filterProtectedButton.id -> Filter.PROTECTED
                binding.filterUnprotectedButton.id -> Filter.UNPROTECTED
                else -> Filter.ALL
            }
            refreshAppList()
        }

        binding.hideSystemSwitch.isChecked = AppPreferences.hideSystemApps()
        binding.hideSystemSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setHideSystemApps(isChecked)
            refreshAppList()
        }

        binding.recommendedAppsButton.setOnClickListener {
            toggleRecommendedApps()
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = refreshAppList()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.protectionSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!binding.protectionSwitch.isPressed) return@setOnCheckedChangeListener
            if (isChecked && !PermissionChecker.hasAllEssentialPermissions(this)) {
                Snackbar.make(binding.root, getString(R.string.main_status_missing_permissions), Snackbar.LENGTH_SHORT).show()
                binding.protectionSwitch.isChecked = false
                return@setOnCheckedChangeListener
            }
            AppPreferences.setProtectionEnabled(isChecked)
            if (!isChecked) {
                SessionController.clearTempAccess()
                SessionController.setAppPanelUnlocked(false)
            }
            refreshUiState()
            ProtectionManager.syncForegroundService(this)
        }

        maybeRequestNotificationPermission()
        renderPermissionRows()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        refreshUiState()
        refreshAppList()
        renderPermissionRows()
        renderOnboardingCard()
        ProtectionManager.syncForegroundService(this)
        maybeRequireAppAuthentication()
    }

    override fun onStop() {
        super.onStop()
        SessionController.setAppPanelUnlocked(false)
        appEntryPromptShown = false
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 40)
        }
    }

    private fun loadApps() {
        allApps = repository.loadLaunchableApps()
        refreshUiState()
        refreshAppList()
        refreshRecommendedButtonState()
        renderOnboardingCard()
    }

    private fun renderOnboardingCard() {
        val show = !AppPreferences.isOnboardingDismissed() && !PermissionChecker.hasAllEssentialPermissions(this)
        binding.onboardingCard.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun maybeRequireAppAuthentication() {
        if (!AppPreferences.isProtectionEnabled()) return
        if (!PermissionChecker.hasAllEssentialPermissions(this)) return
        if (SessionController.isAppPanelUnlocked()) return
        if (appEntryPromptShown) return
        requestAppEntryAuthentication(force = false)
    }

    private fun requestAppEntryAuthentication(force: Boolean) {
        appEntryPromptShown = true
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        val canAuth = BiometricManager.from(this).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

        if (!canAuth) {
            if (!force) finish()
            return
        }

        SessionController.setAuthInProgress(true)

        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                SessionController.setAuthInProgress(false)
                SessionController.setAppPanelUnlocked(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                SessionController.setAuthInProgress(false)
                if (!force) finish()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bloqueo_titulo))
            .setSubtitle(getString(R.string.bloqueo_subtitulo))
            .setNegativeButtonText(getString(R.string.cancelar))
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun refreshUiState() {
        val hasPerms = PermissionChecker.hasAllEssentialPermissions(this)
        val enabled = AppPreferences.isProtectionEnabled()

        binding.protectionSwitch.isEnabled = hasPerms
        binding.protectionSwitch.isChecked = enabled && hasPerms

        when {
            !hasPerms -> {
                binding.statusText.text = getString(R.string.main_status_missing_permissions)
                binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.bp_gray))
                tintSwitch(R.color.bp_gray)
            }
            enabled -> {
                binding.statusText.text = getString(R.string.main_status_protecting)
                binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.bp_green))
                tintSwitch(R.color.bp_green)
            }
            else -> {
                binding.statusText.text = getString(R.string.main_status_inactive)
                binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.bp_red))
                tintSwitch(R.color.bp_red)
            }
        }

        binding.summaryProtectionValue.text = getString(if (enabled && hasPerms) R.string.resumen_activa else R.string.resumen_inactiva)
        binding.summaryAppsValue.text = AppPreferences.getProtectedPackages().size.toString()
        binding.summaryPendingValue.text = countPendingRequirements().toString()
    }

    private fun countPendingRequirements(): Int {
        var pending = 0
        if (!PermissionChecker.hasDeviceAdmin(this)) pending++
        if (!AppPreferences.isRestrictedSettingsConfirmed()) pending++
        if (!PermissionChecker.hasAccessibilityPermission(this)) pending++
        if (!PermissionChecker.hasUsageAccessPermission(this)) pending++
        if (!PermissionChecker.hasOverlayPermission(this)) pending++
        if (!PermissionChecker.hasIgnoreBatteryOptimization(this)) pending++
        if (!AppPreferences.isAutostartConfirmed()) pending++
        return pending
    }

    private fun tintSwitch(colorRes: Int) {
        val color = ContextCompat.getColorStateList(this, colorRes)
        binding.protectionSwitch.thumbTintList = color
        binding.protectionSwitch.trackTintList = color
    }

    private fun renderPermissionRows() {
        val container = binding.permissionsRowsContainer
        container.removeAllViews()

        val items = listOf(
            PermissionItem(
                key = "admin",
                title = getString(R.string.perm_admin),
                stateText = permissionStateText(PermissionChecker.hasDeviceAdmin(this)),
                stateColorRes = permissionStateColor(PermissionChecker.hasDeviceAdmin(this)),
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openDeviceAdminSettings(this) }
            ),
            PermissionItem(
                key = "restricted_settings",
                title = getString(R.string.perm_restricted_settings),
                stateText = if (AppPreferences.isRestrictedSettingsConfirmed()) getString(R.string.perm_ok) else getString(R.string.perm_revision_manual),
                stateColorRes = if (AppPreferences.isRestrictedSettingsConfirmed()) R.color.bp_green else R.color.bp_gray,
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openRestrictedSettings(this) },
                secondaryLabel = getString(R.string.confirmar_ajustes_restringidos),
                secondaryAction = {
                    AppPreferences.setRestrictedSettingsConfirmed(true)
                    renderPermissionRows()
                    refreshUiState()
                }
            ),
            PermissionItem(
                key = "accessibility",
                title = getString(R.string.perm_accesibilidad),
                stateText = permissionStateText(PermissionChecker.hasAccessibilityPermission(this)),
                stateColorRes = permissionStateColor(PermissionChecker.hasAccessibilityPermission(this)),
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openAccessibilitySettings(this) }
            ),
            PermissionItem(
                key = "usage",
                title = getString(R.string.perm_uso),
                stateText = permissionStateText(PermissionChecker.hasUsageAccessPermission(this)),
                stateColorRes = permissionStateColor(PermissionChecker.hasUsageAccessPermission(this)),
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openUsageAccessSettings(this) }
            ),
            PermissionItem(
                key = "overlay",
                title = getString(R.string.perm_overlay),
                stateText = permissionStateText(PermissionChecker.hasOverlayPermission(this)),
                stateColorRes = permissionStateColor(PermissionChecker.hasOverlayPermission(this)),
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openOverlaySettings(this) }
            ),
            PermissionItem(
                key = "battery",
                title = getString(R.string.perm_bateria),
                stateText = permissionStateText(PermissionChecker.hasIgnoreBatteryOptimization(this)),
                stateColorRes = permissionStateColor(PermissionChecker.hasIgnoreBatteryOptimization(this)),
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openBatterySettings(this) }
            ),
            PermissionItem(
                key = "autostart",
                title = getString(R.string.perm_autoinicio),
                stateText = if (AppPreferences.isAutostartConfirmed()) getString(R.string.perm_ok) else getString(R.string.perm_revision_manual),
                stateColorRes = if (AppPreferences.isAutostartConfirmed()) R.color.bp_green else R.color.bp_gray,
                openLabel = getString(R.string.abrir_ajustes),
                openAction = { PermissionChecker.openAutoStartSettings(this) },
                secondaryLabel = getString(R.string.confirmar_autoinicio),
                secondaryAction = {
                    AppPreferences.setAutostartConfirmed(true)
                    renderPermissionRows()
                    refreshUiState()
                }
            )
        )

        items.forEach { item ->
            val row = RowPermissionBinding.inflate(layoutInflater, container, false)
            row.permissionTitleText.text = item.title
            row.permissionStateText.text = item.stateText
            row.permissionStateText.setTextColor(ContextCompat.getColor(this, item.stateColorRes))
            row.openPermissionButton.text = item.openLabel
            row.openPermissionButton.setOnClickListener { item.openAction.invoke() }

            if (item.secondaryAction != null && item.secondaryLabel != null) {
                row.secondaryPermissionButton.visibility = View.VISIBLE
                row.secondaryPermissionButton.text = item.secondaryLabel
                row.secondaryPermissionButton.setOnClickListener { item.secondaryAction.invoke() }
            } else {
                row.secondaryPermissionButton.visibility = View.GONE
            }

            container.addView(row.root, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun permissionStateText(granted: Boolean): String =
        if (granted) getString(R.string.perm_ok) else getString(R.string.perm_pendiente)

    private fun permissionStateColor(granted: Boolean): Int =
        if (granted) R.color.bp_green else R.color.bp_red

    private fun refreshAppList() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty()
        val protectedPackages = AppPreferences.getProtectedPackages()
        val recommendedPackages = getRecommendedPackagesPresent()

        val filtered = allApps
            .asSequence()
            .filter { app ->
                if (!AppPreferences.hideSystemApps()) {
                    true
                } else {
                    !app.isSystemApp || (recommendedPackages.contains(app.packageName) && protectedPackages.contains(app.packageName))
                }
            }
            .filter {
                when (currentFilter) {
                    Filter.ALL -> true
                    Filter.PROTECTED -> protectedPackages.contains(it.packageName)
                    Filter.UNPROTECTED -> !protectedPackages.contains(it.packageName)
                }
            }
            .filter {
                query.isBlank() ||
                    it.appName.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<AppInfo> { protectedPackages.contains(it.packageName) }
                    .thenBy { it.appName.lowercase(Locale.ROOT) }
            )
            .toList()

        adapter.submitList(filtered.map { AppRowModel(appInfo = it, protected = protectedPackages.contains(it.packageName)) })
        binding.emptyText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        refreshRecommendedButtonState()
        refreshUiState()
    }

    private fun toggleRecommendedApps() {
        val recommendedPackages = getRecommendedPackagesPresent()
        if (recommendedPackages.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.recomendadas_no_encontradas), Snackbar.LENGTH_SHORT).show()
            return
        }

        val protectedPackages = AppPreferences.getProtectedPackages()
        val allRecommendedProtected = recommendedPackages.all { protectedPackages.contains(it) }
        recommendedPackages.forEach { packageName ->
            AppPreferences.setProtected(packageName, !allRecommendedProtected)
        }
        refreshAppList()
        val count = recommendedPackages.size
        val msg = getString(
            if (allRecommendedProtected) R.string.recomendadas_quitadas_count else R.string.recomendadas_marcadas_count,
            count
        )
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun refreshRecommendedButtonState() {
        val recommendedPackages = getRecommendedPackagesPresent()
        val protectedPackages = AppPreferences.getProtectedPackages()
        val allRecommendedProtected = recommendedPackages.isNotEmpty() && recommendedPackages.all { protectedPackages.contains(it) }
        binding.recommendedAppsButton.text = getString(
            if (allRecommendedProtected) R.string.desmarcar_recomendadas else R.string.marcar_recomendadas
        )
    }

    private fun getRecommendedPackagesPresent(): Set<String> {
        val recommendedPackages = setOf(
            "com.miui.gallery",
            "com.google.android.apps.photos",
            "com.google.android.gm",
            "com.whatsapp",
            "org.telegram.messenger",
            "com.google.android.apps.docs",
            "com.google.android.apps.messaging",
            "com.google.android.apps.nbu.files"
        )
        val recommendedNames = setOf(
            "galería",
            "galeria",
            "google fotos",
            "photos",
            "gmail",
            "whatsapp",
            "telegram",
            "drive",
            "mensajes",
            "messages",
            "archivos",
            "files"
        )

        return allApps
            .filter { app ->
                recommendedPackages.contains(app.packageName) || recommendedNames.contains(app.appName.lowercase(Locale.ROOT))
            }
            .map { it.packageName }
            .toSet()
    }

    private enum class Filter {
        ALL,
        PROTECTED,
        UNPROTECTED
    }
}
