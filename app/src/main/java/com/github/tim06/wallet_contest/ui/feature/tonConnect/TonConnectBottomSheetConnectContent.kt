package com.github.tim06.wallet_contest.ui.feature.tonConnect

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import coil.compose.SubcomposeAsyncImage
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.animation.animateDpSize
import com.github.tim06.wallet_contest.ui.components.button.TonButton
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectionDataRequestUi
import com.github.tim06.wallet_contest.ui.theme.RobotoMonoRegular
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor
import com.github.tim06.wallet_contest.util.transformAddress

@Composable
fun TonConnectBottomSheetConnectContent(
    data: TonConnectionDataRequestUi,
    buttonState: ButtonState,
    onClose: () -> Unit,
    onConnectClick: () -> Unit
) {
    val connected by remember(buttonState) {
        derivedStateOf {
            buttonState is ButtonState.Success
        }
    }
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.background,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
            )
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
    ) {
        IconButton(
            modifier = Modifier.padding(4.dp),
            onClick = onClose
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_close), contentDescription = "Close")
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(44.dp))
            SubcomposeAsyncImage(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp)),
                model = data.data.manifest.iconUrl,
                loading = {
                    CircularProgressIndicator()
                },
                contentDescription = "Ton Connect resource logo"
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(
                    id = R.string.wallet_connect_to_fragment_title,
                    data.data.manifest.name
                ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                text = stringResource(
                    id = R.string.wallet_connect_to_fragment_description,
                    data.data.manifest.url.toDomainName(),
                    data.address.transformAddress(),
                    data.walletVersion.toString()
                ).toAnnotatedAddress(data.address.transformAddress()),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2.copy(color = Color.Black)
            )
            Spacer(modifier = Modifier.height(36.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                text = stringResource(id = R.string.wallet_connect_to_fragment_additional),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2.copy(color = SecondaryTextColor)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val initialButtonHeight = 48.dp
            val successButtonHeight = 34.dp
            val initialButtonWidth = LocalConfiguration.current.screenWidthDp.dp - 32.dp
            val successButtonWidth = 34.dp
            val transition = updateTransition(targetState = connected, label = "Connected transition")
            val size by transition.animateDpSize(label = "Button size animation") {
                if (it) DpSize(successButtonWidth, successButtonHeight) else DpSize(initialButtonWidth, initialButtonHeight)
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
            val alpha by transition.animateFloat(label ="Alpha animation", transitionSpec = { tween(800) }) {
                if (it) 1f else 0f
            }
            TonButton(
                modifier = Modifier
                    .padding(16.dp)
                    .size(size),
                shape = RoundedCornerShape(corner),
                contentPadding = PaddingValues(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical),
                click = {
                    onConnectClick.invoke()
                }
            ) {
                if (connected) {
                    Icon(
                        modifier = Modifier.alpha(alpha),
                        painter = painterResource(id = R.drawable.ic_success),
                        contentDescription = "Success"
                    )
                } else {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .alpha(1f - alpha)) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(id = R.string.wallet_connect_to_fragment_connect),
                            style = MaterialTheme.typography.button
                        )
                        if (buttonState is ButtonState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.CenterEnd),
                                strokeWidth = 2.dp,
                                color = Color.White,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun String.toAnnotatedAddress(address: String): AnnotatedString {
    val indexOfAddress = indexOf(address)
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = RobotoRegular,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        ) {
            append(this@toAnnotatedAddress)
        }

        addStyle(
            style = SpanStyle(
                fontFamily = RobotoMonoRegular,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = SecondaryTextColor
            ),
            indexOfAddress,
            indexOfAddress + address.length
        )
    }
}

private fun String.toDomainName(): String {
    val domain = Uri.parse(this).host.orEmpty()
    return if (domain.startsWith("www.")) domain.substring(4) else domain
}