package com.github.tim06.wallet_contest.ui.feature.import_wallet.words

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.ScrollableTitleContainer
import com.github.tim06.wallet_contest.ui.components.button.TonLoadingButton
import com.github.tim06.wallet_contest.ui.components.dialog.TonDialog
import com.github.tim06.wallet_contest.ui.components.textField.TonTextField
import com.github.tim06.wallet_contest.ui.feature.import_wallet.ImportViewModel
import com.github.tim06.wallet_contest.ui.feature.import_wallet.ImportViewModelFactory
import com.github.tim06.wallet_contest.ui.theme.TonBlue2
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImportSecretWordsScreen(
    tonWalletClient: TonWalletClient,
    onBackClick: () -> Unit,
    onNoWordsClick: () -> Unit,
    onContinueClick: (String) -> Unit,
    viewModel: ImportViewModel = viewModel(
        factory = ImportViewModelFactory(tonWalletClient)
    )
) {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val isSuccessImport by viewModel.isSuccess.collectAsState()
    val isShowDialog by viewModel.errorDialog.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState(emptyArray())
    val rowsValues = remember { viewModel.words }

    LaunchedEffect(key1 = isSuccessImport) {
        if (isSuccessImport) {
            onContinueClick.invoke(Json.encodeToString(viewModel.inputKey))
        }
    }

    ScrollableTitleContainer(
        title = R.string.import_secret_words_title,
        description = stringResource(id = R.string.import_secret_words_description),
        icon = R.raw.recovery_phrase,
        backClick = {
            keyboard?.hide()
            onBackClick.invoke()
        }
    ) {
        Column(
            modifier = Modifier.padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.clickable {
                    keyboard?.hide()
                    onNoWordsClick.invoke()
                },
                text = stringResource(id = R.string.import_secret_words_no_words),
                style = MaterialTheme.typography.body2.copy(
                    color = TonBlue2
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            SecretRows(
                rowIndexes = rowsValues,
                suggestions = suggestions,
                isLoading = isLoading,
                onNextKeyboardClick = {
                    if (it != rowsValues.size) {
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                },
                onRowQueryChanged = { key, newValue ->
                    viewModel.onRowChanged(key, newValue)
                },
                onFocusChanged = {
                    viewModel.clearSuggestions()
                }
            )
            Spacer(modifier = Modifier.height(28.dp))
            TonLoadingButton(
                modifier = Modifier.widthIn(min = 200.dp),
                text = stringResource(id = R.string.import_secret_words_continue),
                loading = isLoading,
                click = {
                    keyboard?.hide()
                    viewModel.importWallet()
                }
            )
            /*TonButton(
                modifier = Modifier.widthIn(min = 200.dp),
                text = stringResource(id = R.string.import_secret_words_continue),
                click = {
                    keyboard?.hide()
                    viewModel.importWallet()
                }
            )*/
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
    if (isShowDialog) {
        IncorrectWordsDialog(dismiss = viewModel::dismissErrorDialog)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SecretRows(
    modifier: Modifier = Modifier,
    rowIndexes: Map<Int, String>,
    suggestions: Array<String>,
    isLoading: Boolean,
    onNextKeyboardClick: (Int) -> Unit,
    onRowQueryChanged: (Int, String) -> Unit,
    onFocusChanged: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isFocusedLast by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .padding(horizontal = 40.dp)
            .alpha(if (isLoading) 0.8f else 1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rowIndexes.forEach { entry ->
            var isFocused by remember { mutableStateOf(false) }
            var savedLayoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
            Box {
                TonTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            when (entry.key) {
                                1 -> {
                                    Modifier.focusRequester(focusRequester)
                                }

                                24 -> {
                                    Modifier.onFocusEvent {
                                        isFocusedLast = it.isFocused
                                    }
                                }

                                else -> {
                                    Modifier
                                }
                            }
                        )
                        .onFocusEvent { focusState ->
                            if (!isFocused && focusState.isFocused) {
                                onFocusChanged.invoke()
                            }
                            isFocused = focusState.isFocused
                        }
                        .onGloballyPositioned { layoutCoordinates ->
                            if (isFocused) {
                                savedLayoutCoordinates = layoutCoordinates
                            }
                        },
                    number = entry.key,
                    text = entry.value,
                    enabled = !isLoading,
                    onNextClick = {
                        if (isFocusedLast) {
                            keyboard?.hide()
                        } else {
                            onNextKeyboardClick.invoke(entry.key)
                        }
                    },
                    onTextChanged = { newQuery -> onRowQueryChanged.invoke(entry.key, newQuery) }
                )
                androidx.compose.animation.AnimatedVisibility(visible = isFocused && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .wrapContentHeight()
                            .offset(y = (-50).dp),
                        backgroundColor = MaterialTheme.colors.background
                    ) {
                        LazyRow {
                            suggestions.forEach { suggestion ->
                                item {
                                    TextButton(
                                        onClick = {
                                            onFocusChanged.invoke()
                                            onRowQueryChanged.invoke(entry.key, suggestion)
                                            onNextKeyboardClick.invoke(entry.key)
                                        }
                                    ) {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun IncorrectWordsDialog(
    dismiss: () -> Unit
) {
    TonDialog(
        title = R.string.import_secret_words_dialog_title,
        description = R.string.import_secret_words_dialog_description,
        rightButtonText = R.string.import_secret_words_dialog_ok,
        rightButtonClick = dismiss,
        dismissRequest = dismiss
    )
}