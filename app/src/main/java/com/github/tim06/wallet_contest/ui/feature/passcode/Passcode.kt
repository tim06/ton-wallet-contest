package com.github.tim06.wallet_contest.ui.feature.passcode

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import com.github.tim06.wallet_contest.ui.components.keyboard.CustomPhoneKeyboardView
import com.github.tim06.wallet_contest.ui.components.shake.ShakeAnimationControlled
import com.github.tim06.wallet_contest.ui.components.text.IconWithText
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import com.github.tim06.wallet_contest.ui.theme.PrimaryTextColor

@Composable
fun Passcode(
    modifier: Modifier = Modifier,
    title: String? = null,
    descriptionColor: Color = PrimaryTextColor,
    dotsCount: Int = 4,
    filledCount: Int = 0,
    darkMode: Boolean = false,
    showBackIcon: Boolean = false,
    withBiometric: Boolean = false,
    showPasscodeOptions: Boolean = true,
    showSuccessAnimation: Boolean = false,
    showErrorAnimation: Boolean = false,
    onPasscodeOptionsChanged: ((Int) -> Unit)? = null,
    onNewDigitClick: (String) -> Unit,
    onDeleteDigitClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    onErrorAnimationEnd: (() -> Unit)? = null,
    onSuccessAnimationEnd: (() -> Unit)? = null
) {
    val inputEnabled by remember(showSuccessAnimation) {
        derivedStateOf {
            showSuccessAnimation.not()
        }
    }

    val keyboardListener = remember {
        object : CustomPhoneKeyboardView.OnKeyboardQueryListener {
            override fun onClick(number: String) {
                onNewDigitClick.invoke(number)
            }
            override fun onDeleteClick() {
                onDeleteDigitClick.invoke()
            }

            override fun onBiometricClick() {
                onBiometricClick?.invoke()
            }
        }
    }

    InfoScreen(
        modifier = modifier,
        top = {
            if (showBackIcon) {
                TonTopAppBar(
                    title = "",
                    backClick = {

                    }
                )
            }
        },
        center = {
            Column(
                modifier = Modifier.padding(bottom = CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconWithText(
                    icon = R.raw.password,
                    title = title,
                    description = stringResource(
                        id = R.string.create_passcode_description,
                        dotsCount
                    ),
                    descriptionColor = descriptionColor
                )
                Spacer(modifier = Modifier.height(28.dp))
                Box(contentAlignment = Alignment.Center) {
                    ShakeAnimationControlled(
                        startAnimation = showErrorAnimation,
                        onShakeAnimationFinish = onErrorAnimationEnd
                    ) {
                        DigitsDots(
                            modifier = Modifier,
                            digitsCount = dotsCount,
                            filledCount = filledCount,
                            scaleAnimation = showSuccessAnimation,
                            darkMode = darkMode,
                            onSuccessAnimationEnd = onSuccessAnimationEnd
                        )
                    }
                }
            }
        },
        bottom = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showPasscodeOptions) {
                    PasscodeOptions(
                        clickEnabled = inputEnabled,
                        onDigitsCountChanged = onPasscodeOptionsChanged
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CustomPhoneKeyboardView.KEYBOARD_HEIGHT_DP.dp),
                    factory = { context ->
                        CustomPhoneKeyboardView(
                            context,
                            keyboardListener,
                            darkMode,
                            false,
                            // TODO biometric check
                            withBiometric
                        )
                    },
                    update = {
                        it.setInputEnabled(inputEnabled)
                    }
                )
            }
        }
    )
}