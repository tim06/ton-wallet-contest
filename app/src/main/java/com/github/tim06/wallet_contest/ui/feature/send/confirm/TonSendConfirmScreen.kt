package com.github.tim06.wallet_contest.ui.feature.send.confirm

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import com.github.tim06.wallet_contest.ui.feature.passcode_setup.CreatePasscodeViewModelFactory
import com.github.tim06.wallet_contest.ui.feature.send.TonSendContainer
import com.github.tim06.wallet_contest.ui.feature.send.TonSendRecipientModel
import com.github.tim06.wallet_contest.ui.feature.send.TonSendTextField
import com.github.tim06.wallet_contest.ui.theme.Error2Color
import com.github.tim06.wallet_contest.ui.theme.Error2Color12
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.ui.theme.WarningColor
import com.github.tim06.wallet_contest.util.formatCurrency
import com.github.tim06.wallet_contest.util.toTonLong
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TonSendConfirmScreen(
    tonWalletClient: TonWalletClient,
    address: String? = null,
    amount: Long? = null,
    commentExtra: String? = null,
    viewModel: TonSendConfirmViewModel = viewModel(
        factory = TonSendConfirmViewModelFactory(tonWalletClient, address, amount, commentExtra)
    ),
    onBackClick: () -> Unit,
    onConfirmClick: (String) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val fee by viewModel.fee.collectAsState()
    var comment by remember { mutableStateOf(TextFieldValue(commentExtra.orEmpty())) }
    val commentCharactersCount by remember(comment) {
        derivedStateOf { comment.text.count() }
    }
    val commentCharactersLengthState by remember(commentCharactersCount) {
        derivedStateOf {
            if (commentCharactersCount > COMMENT_CHARACTERS_SIZE) {
                CommentCharactersLengthState.Exceeded(commentCharactersCount - COMMENT_CHARACTERS_SIZE)
            } else if (commentCharactersCount >= COMMENT_CHARACTERS_SIZE - COMMENT_CHARACTERS_WARNING_SIZE) {
                CommentCharactersLengthState.Warning(COMMENT_CHARACTERS_SIZE - commentCharactersCount)
            } else {
                CommentCharactersLengthState.Normal
            }
        }
    }
    TonSendContainer {
        Column {
            TonTopAppBar(title = stringResource(id = R.string.send_title), backClick = onBackClick)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(id = R.string.send_comment_field_title),
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TonSendTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = comment,
                hint = stringResource(id = R.string.send_comment_field_hint),
                onTextChanged = {
                    comment = it.copy(
                        annotatedString = it.annotatedString.toAnnotatedString()
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = stringResource(id = R.string.send_comment_field_description),
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    color = SecondaryTextColor
                )
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                text = when (val state = commentCharactersLengthState) {
                    is CommentCharactersLengthState.Normal -> ""
                    is CommentCharactersLengthState.Warning -> stringResource(
                        id = R.string.send_comment_field_description_count_left,
                        state.charactersLeft
                    )
                    is CommentCharactersLengthState.Exceeded -> stringResource(
                        id = R.string.send_comment_field_description_count_exceeded,
                        state.characters
                    )
                },
                style = MaterialTheme.typography.body2.copy(
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    color = when (commentCharactersLengthState) {
                        is CommentCharactersLengthState.Normal -> Color.Transparent
                        is CommentCharactersLengthState.Warning -> WarningColor
                        is CommentCharactersLengthState.Exceeded -> Error2Color
                    }
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            TonSendConfirmRows(
                recipientAddress = address?.let { Json.decodeFromString<TonSendRecipientModel>(it).address },
                amountExtra = amount?.formatCurrency().toString(),
                feeExtra = fee
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        TonButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            enabled = commentCharactersLengthState !is CommentCharactersLengthState.Exceeded,
            click = {
                keyboard?.hide()
                onConfirmClick.invoke(comment.text.ifEmpty { " " })
            },
            text = stringResource(id = R.string.send_details_confirm_and_send)
        )
    }
}

private fun AnnotatedString.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        append(
            text = if (this@toAnnotatedString.count() > COMMENT_CHARACTERS_SIZE) {
                this@toAnnotatedString.take(COMMENT_CHARACTERS_SIZE)
            } else {
                this@toAnnotatedString
            }
        )

        if (this@toAnnotatedString.count() > COMMENT_CHARACTERS_SIZE) {
            withStyle(
                style = SpanStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Error2Color,
                    background = Error2Color12
                )
            ) {
                append(this@toAnnotatedString.takeLast(this@toAnnotatedString.length - COMMENT_CHARACTERS_SIZE))
            }
        }
    }
}

sealed interface CommentCharactersLengthState {
    object Normal : CommentCharactersLengthState
    data class Warning(val charactersLeft: Int) : CommentCharactersLengthState
    data class Exceeded(val characters: Int) : CommentCharactersLengthState
}

private const val COMMENT_CHARACTERS_SIZE = 50
private const val COMMENT_CHARACTERS_WARNING_SIZE = 20