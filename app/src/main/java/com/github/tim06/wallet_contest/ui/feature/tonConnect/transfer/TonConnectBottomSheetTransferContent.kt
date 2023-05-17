package com.github.tim06.wallet_contest.ui.feature.tonConnect.transfer

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.animation.animateDpSize
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.components.rlottie.LottieIcon
import com.github.tim06.wallet_contest.ui.components.row.TonRow
import com.github.tim06.wallet_contest.ui.components.title.TonTitle
import com.github.tim06.wallet_contest.ui.theme.BalanceBigTextStyle
import com.github.tim06.wallet_contest.ui.theme.InterRegular
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.TonBlue10

@Composable
fun TonConnectBottomSheetTransferContent(
    tonWalletClient: TonWalletClient,
    from: String,
    recipient: String,
    amount: String,
    onCloseClick: () -> Unit,
    onPasscodeRequest: () -> Unit,
    viewModel: TonConnectBottomSheetTransferViewModel = viewModel(
        factory = TonConnectBottomSheetTransferViewModelFactory(
            tonWalletClient = tonWalletClient,
            from = from,
            amount = amount
        )
    )
) {
    SideEffect {
        viewModel.setNewRecipient(recipient)
        if (viewModel.needPasscode().not()) {
            viewModel.onConfirmClick()
        }
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.clearStates()
        }
    }
    val collectedState by viewModel.state.collectAsState()
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.background,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
            .navigationBarsPadding()
    ) {
        when (val state = collectedState) {
            is TonConnectTransferState.InfoLoaded -> {
                val buttonState by viewModel.buttonState.collectAsState()
                Content(
                    amount = state.amount,
                    recipient = state.recipient,
                    fee = state.fee,
                    buttonState = buttonState,
                    onCancelClick = onCloseClick,
                    onConfirmClick = {
                        if (viewModel.needPasscode()) {
                            viewModel.awaitPasscode = true
                            onPasscodeRequest.invoke()
                        } else {
                            viewModel.onConfirmClick()
                        }
                    }
                )
            }
            is TonConnectTransferState.Loading -> {
                Content(
                    modifier = Modifier.alpha(0f),
                    amount = "",
                    recipient = "",
                    fee = 0.0,
                    buttonState = TonConnectTransferButtonState.Empty,
                    onCancelClick = onCloseClick,
                    onConfirmClick = viewModel::onConfirmClick
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                )
            }
            is TonConnectTransferState.NotEnoughFunds -> {
                Content(
                    modifier = Modifier.alpha(0f),
                    amount = "",
                    recipient = "",
                    fee = 0.0,
                    buttonState = TonConnectTransferButtonState.Empty,
                    onCancelClick = onCloseClick,
                    onConfirmClick = viewModel::onConfirmClick
                )
                Error(
                    message = "Not enough funds on wallet!",
                    onCloseClick = onCloseClick
                )
            }
            is TonConnectTransferState.LoadingError -> {
                Content(
                    modifier = Modifier.alpha(0f),
                    amount = "",
                    recipient = "",
                    fee = 0.0,
                    buttonState = TonConnectTransferButtonState.Empty,
                    onCancelClick = onCloseClick,
                    onConfirmClick = viewModel::onConfirmClick
                )
                Error(
                    message = state.error,
                    onCloseClick = onCloseClick
                )
            }
        }
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    amount: String,
    recipient: String,
    fee: Double,
    buttonState: TonConnectTransferButtonState,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TonTitle(text = stringResource(id = R.string.wallet_connect_ton_transfer))
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LottieIcon(
                modifier = Modifier.size(44.dp, 56.dp),
                iconSize = DpSize(44.dp, 56.dp),
                icon = R.raw.main
            )
            Text(
                text = amount,
                style = BalanceBigTextStyle.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Column {
            TonRow(
                leftText = stringResource(id = R.string.wallet_connect_ton_recipient),
                rightText = recipient,
                rightTextFont = RobotoMonoRegular
            )
            TonRow(
                leftText = stringResource(id = R.string.wallet_connect_ton_fee),
                rightText = stringResource(id = R.string.fee, fee.toString()),
                showDivider = false,
                rightTextFont = InterRegular
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
        Buttons(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            buttonState = buttonState,
            onCancelClick = onCancelClick,
            onConfirmClick = onConfirmClick
        )
    }
}

@Composable
private fun BoxScope.Error(
    message: String,
    onCloseClick: () -> Unit
) {
    Text(
        modifier = Modifier.align(Alignment.Center),
        text = message
    )
    TonButton(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        text = "Close",
        click = onCloseClick
    )
}

@Composable
private fun Buttons(
    modifier: Modifier = Modifier,
    buttonState: TonConnectTransferButtonState,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val rowWidthDp = LocalConfiguration.current.screenWidthDp.dp - 32.dp
    val buttonWidthDp = (rowWidthDp - 8.dp) / 2
    var buttonWidth by remember { mutableStateOf(0) }
    var buttonPosition by remember { mutableStateOf(0f) }

    val connected by remember(buttonState) {
        derivedStateOf {
            buttonState is TonConnectTransferButtonState.Success
        }
    }

    val transition = updateTransition(
        targetState = connected,
        label = "Connected transition"
    )
    val cancelButtonWidth by transition.animateDp(label = "Cancel button width") {
        if (it) 0.dp else buttonWidthDp
    }
    val cancelButtonOffset by transition.animateDp(label = "Cancel button offset") {
        if (it) (rowWidthDp / 2) - 17.dp - 8.dp else 0.dp
    }

    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TonButton(
            modifier = Modifier.width(cancelButtonWidth),
            click = onCancelClick,
            backgroundColor = TonBlue10
        ) {
            Text(
                text = stringResource(id = R.string.wallet_connect_ton_cancel),
                style = MaterialTheme.typography.button.copy(
                    color = MaterialTheme.colors.primary
                )
            )
        }

        val initialButtonHeight = 48.dp
        val successButtonHeight = 34.dp
        val successButtonWidth = 34.dp
        val size by transition.animateDpSize(label = "Button size animation") {
            if (it) DpSize(successButtonWidth, successButtonHeight) else DpSize(
                buttonWidthDp,
                initialButtonHeight
            )
        }
        val corner by transition.animateDp(label = "Shape corners animation") {
            if (it) 18.dp else 8.dp
        }
        val contentPaddingHorizontal by transition.animateDp(label = "Button content padding horizontal") {
            if (it) 0.dp else 16.dp
        }
        val contentPaddingVertical by transition.animateDp(label = "Button content padding vertical") {
            if (it) 0.dp else 14.dp
        }
        val alpha by transition.animateFloat(
            label = "Alpha animation",
            transitionSpec = { tween(500) }) {
            if (it) 1f else 0f
        }
        TonButton(
            modifier = Modifier
                .onGloballyPositioned { layoutCoordinates ->
                    if (buttonPosition == 0f) {
                        buttonPosition = layoutCoordinates.positionInRoot().x
                    }
                    if (buttonWidth == 0) {
                        buttonWidth = layoutCoordinates.size.width
                    }
                }
                .size(size)
                .offset(x = cancelButtonOffset),
            shape = RoundedCornerShape(corner),
            contentPadding = PaddingValues(
                horizontal = contentPaddingHorizontal,
                vertical = contentPaddingVertical
            ),
            click = onConfirmClick
        ) {
            if (connected) {
                Icon(
                    modifier = Modifier.alpha(alpha),
                    painter = painterResource(id = R.drawable.ic_success),
                    contentDescription = "Success"
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(1f - alpha)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.wallet_connect_ton_confirm),
                        style = MaterialTheme.typography.button
                    )
                    if (buttonState is TonConnectTransferButtonState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.CenterEnd),
                            strokeWidth = 2.5.dp,
                            color = Color.White,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}