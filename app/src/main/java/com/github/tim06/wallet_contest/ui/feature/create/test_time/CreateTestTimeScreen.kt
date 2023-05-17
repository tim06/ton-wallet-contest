package com.github.tim06.wallet_contest.ui.feature.create.test_time

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.ScrollableTitleContainer
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.dialog.TonDialog
import com.github.tim06.wallet_contest.ui.components.textField.TonTextField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateTestTimeScreen(
    randomWords: List<Pair<Int, String>>,
    onContinueClick: () -> Unit,
    backClickListener: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var isWordsCorrect by remember { mutableStateOf(false) }

    ScrollableTitleContainer(
        title = R.string.create_test_time_title,
        description = stringResource(
            id = R.string.create_test_time_description,
            randomWords[0].first,
            randomWords[1].first,
            randomWords[2].first
        ),
        icon = R.raw.test_time,
        backClick = backClickListener
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        TestRows(
            modifier = Modifier.fillMaxWidth(),
            randomWords = randomWords,
            isLoading = isLoading,
            isWordsCorrect = { isWordsCorrect = it }
        )
        Spacer(modifier = Modifier.height(24.dp))
        TonButton(
            modifier = Modifier.widthIn(min = 200.dp),
            text = stringResource(id = R.string.create_test_time_continue),
            enabled = isWordsCorrect,
            click = {
                keyboard?.hide()
                onContinueClick.invoke()
            }
        )
    }
    if (showDialog) {
        SureDoneDialog {
            showDialog = false
        }
    }
}

@Composable
private fun TestRows(
    modifier: Modifier = Modifier,
    randomWords: List<Pair<Int, String>>,
    isLoading: Boolean,
    isWordsCorrect: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var value1 by remember { mutableStateOf("") }
    var value2 by remember { mutableStateOf("") }
    var value3 by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        randomWords.forEachIndexed { index, item ->
            TonTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (index == 0) {
                            Modifier.focusRequester(focusRequester)
                        } else {
                            Modifier
                        }
                    ),
                number = item.first,
                text = when (index) {
                    0 -> value1
                    1 -> value2
                    2 -> value3
                    else -> ""
                },
                enabled = !isLoading,
                onNextClick = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ) {
                when (index) {
                    0 -> value1 = it
                    1 -> value2 = it
                    2 -> value3 = it
                }
                isWordsCorrect.invoke(
                    value1 == randomWords[0].second && value2 == randomWords[1].second && value3 == randomWords[2].second
                )
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SureDoneDialog(
    dismiss: () -> Unit
) {
    TonDialog(
        title = R.string.create_test_time_dialog_title,
        description = R.string.create_test_time_dialog_description,
        leftButtonText = R.string.create_test_time_dialog_see_words,
        leftButtonClick = {},
        rightButtonText = R.string.create_test_time_dialog_try_again,
        rightButtonClick = dismiss,
        dismissRequest = dismiss
    )
}