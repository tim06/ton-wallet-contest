package com.github.tim06.wallet_contest.ui.feature.lock

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.create.biometric.buildBiometricPromptInfo
import com.github.tim06.wallet_contest.ui.feature.create.biometric.createBiometricPrompt
import com.github.tim06.wallet_contest.ui.feature.passcode.Passcode
import com.github.tim06.wallet_contest.util.SystemBarIconsDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.crypto.Cipher

@Composable
fun LockScreen(
    tonWalletClient: TonWalletClient,
    onSuccess: (String) -> Unit
) {
    SystemBarIconsDark(false)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var digitsCount by remember { mutableStateOf(4) }
    var currentPasscode by remember { mutableStateOf("") }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var showErrorAnimation by remember { mutableStateOf(false) }

    var inputKey by remember { mutableStateOf<String>("") }

    var isSuccessTriggered by remember { mutableStateOf(false) }

    val onAuthenticationSuccess = remember {
        { cipher: Cipher? ->
            //val cipher = tonWalletClient.getCipher()
            if (cipher != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val key = tonWalletClient.getInputKeyWithCipher(cipher)
                    if (key == null) {
                        showErrorAnimation = true
                    } else {
                        inputKey = Json.encodeToString(key)
                        showSuccessAnimation = true
                    }
                }
            }
        }
    }

    val onAuthenticationError = remember {
        {
            Toast.makeText(context, "Error:!", Toast.LENGTH_SHORT).show()
        }
    }

    Passcode(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.surface)
            .navigationBarsPadding(),
        darkMode = true,
        withBiometric = tonWalletClient.isDeviceBiometricAllowed,
        descriptionColor = Color.White,
        dotsCount = digitsCount,
        filledCount = currentPasscode.count(),
        showSuccessAnimation = showSuccessAnimation,
        showErrorAnimation = showErrorAnimation,
        onPasscodeOptionsChanged = { newDigitsCount ->
            digitsCount = newDigitsCount
            currentPasscode = ""
        },
        onNewDigitClick = { number ->
            if (currentPasscode.count() < digitsCount) {
                currentPasscode += number
            }

            if (currentPasscode.count() == digitsCount) {
                coroutineScope.launch(Dispatchers.IO) {
                    val key = tonWalletClient.getInputKeyWithCipher(currentPasscode)
                    if (key == null) {
                        showErrorAnimation = true
                    } else {
                        inputKey = Json.encodeToString(key)
                        showSuccessAnimation = true
                    }
                }
            }
        },
        onDeleteDigitClick = {
            currentPasscode = currentPasscode.dropLast(1)
        },
        onBiometricClick = {
            createBiometricPrompt(
                context,
                onAuthenticationSuccess,
                onAuthenticationError
            ).authenticate(
                buildBiometricPromptInfo(),
                BiometricPrompt.CryptoObject(tonWalletClient.getCipher()!!)
            )
        },
        onErrorAnimationEnd = {
            showErrorAnimation = false
        },
        onSuccessAnimationEnd = {
            if (isSuccessTriggered.not()) {
                onSuccess.invoke(inputKey)
                isSuccessTriggered = true
            }
            showSuccessAnimation = false
        }
    )
}