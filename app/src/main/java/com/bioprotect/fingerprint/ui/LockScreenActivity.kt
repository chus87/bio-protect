package com.bioprotect.fingerprint.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bioprotect.fingerprint.R
import com.bioprotect.fingerprint.databinding.ActivityLockBinding
import com.bioprotect.fingerprint.util.EventLogger
import com.bioprotect.fingerprint.util.SessionController

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private var targetPackage: String = ""
    private var promptedBiometric = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SECURE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE).orEmpty()
        binding.targetAppText.text = targetPackage

        binding.cancelButton.setOnClickListener {
            SessionController.setAuthInProgress(false)
            EventLogger.log("Autenticación cancelada para $targetPackage")
            goHomeAndFinish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!promptedBiometric) {
            promptedBiometric = true
            tryBiometric()
        }
    }

    private fun tryBiometric() {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        val biometricManager = BiometricManager.from(this)
        val availability = biometricManager.canAuthenticate(authenticators)
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            SessionController.setAuthInProgress(false)
            EventLogger.log("Huella no disponible para $targetPackage")
            goHomeAndFinish()
            return
        }

        SessionController.setAuthInProgress(true)
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                SessionController.setAuthInProgress(false)
                EventLogger.log("Desbloqueo biométrico correcto para $targetPackage")
                unlockAndFinish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                SessionController.setAuthInProgress(false)
                EventLogger.log("Error biométrico para $targetPackage: $errorCode $errString")
                goHomeAndFinish()
            }

            override fun onAuthenticationFailed() {
                EventLogger.log("Intento biométrico fallido para $targetPackage")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bloqueo_titulo))
            .setSubtitle(getString(R.string.bloqueo_subtitulo))
            .setNegativeButtonText(getString(R.string.cancelar))
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun unlockAndFinish() {
        SessionController.allowPackage(targetPackage)
        SessionController.unlock(targetPackage)
        finish()
    }

    private fun goHomeAndFinish() {
        SessionController.markLockDismissed()
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && !SessionController.isUnlocked(targetPackage)) {
            SessionController.markLockDismissed()
        }
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "extra_target_package"
    }
}
