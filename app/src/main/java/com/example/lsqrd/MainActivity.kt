package com.example.lsqrd

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.lsqrd.ui.AppNavGraph
import com.example.lsqrd.ui.theme.LsqrdTheme
import androidx.fragment.app.FragmentActivity
import com.example.lsqrd.ui.LockScreen

class MainActivity : FragmentActivity() {

    private var isUnlocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LsqrdTheme {
                if (isUnlocked) {
                    AppNavGraph()
                } else {
                    LockScreen(onUnlockRequest = { authenticate() })
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isUnlocked) {
            authenticate()
        }
    }

    override fun onStop() {
        super.onStop()
        isUnlocked = false
    }

    private fun authenticate() {
        val canAuthenticate = BiometricManager.from(this).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val prompt = BiometricPrompt(
                this,
                ContextCompat.getMainExecutor(this),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        isUnlocked = true
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder().setTitle("Unlock Lsqrd")
                .setSubtitle("Use your biometric or device PIN").setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                ).build()

            prompt.authenticate(promptInfo)
        } else {
            isUnlocked = true
        }
    }
}