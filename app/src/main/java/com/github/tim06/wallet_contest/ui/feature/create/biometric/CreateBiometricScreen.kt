package com.github.tim06.wallet_contest.ui.feature.create.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import javax.crypto.Cipher

@Composable
fun CreateBiometricScreen() {
    InfoScreen(
        icon = R.raw.loading,
        title = stringResource(id = R.string.create_biometric_title),
        description = stringResource(id = R.string.create_biometric_description),
        firstButtonText = stringResource(id = R.string.create_biometric_title),
        firstButtonClick = { /*TODO*/ },
        secondButtonText = stringResource(id = R.string.create_biometric_skip),
        secondButtonClick = { /*TODO*/ }
    )
}

fun buildBiometricPromptInfo(): BiometricPrompt.PromptInfo {
    return BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Please authenticate to continue")
        .setNegativeButtonText("Cancel")
        .build()
}

fun createBiometricPrompt(
    context: Context,
    onAuthenticationSuccess: (Cipher?) -> Unit,
    onAuthenticationError: () -> Unit
): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(context)
    val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthenticationSuccess.invoke(result.cryptoObject?.cipher)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            onAuthenticationError.invoke()
        }
    }

    return BiometricPrompt(context as FragmentActivity, executor, authenticationCallback)
}