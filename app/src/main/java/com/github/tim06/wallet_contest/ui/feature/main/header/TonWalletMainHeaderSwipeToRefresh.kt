package com.github.tim06.wallet_contest.ui.feature.main.header

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.swipeToRefresh.SwipeToRefreshState
import com.github.tim06.wallet_contest.ui.theme.SwipeToRefreshBoxActiveColor
import com.github.tim06.wallet_contest.ui.theme.SwipeToRefreshBoxInactiveColor
import com.github.tim06.wallet_contest.ui.theme.WhiteAlpha12
import com.github.tim06.wallet_contest.util.calculateRadius
import com.github.tim06.wallet_contest.util.mapTo

@Composable
fun TonWalletMainHeaderSwipeToRefresh(
    modifier: Modifier = Modifier,
    swipeToRefreshState: SwipeToRefreshState,
    successSwipe: Boolean
) {
    val density = LocalDensity.current
    val swipeToRefreshBoxHeight by remember(swipeToRefreshState) {
        derivedStateOf {
            val boxMaxHeightPx = with(density) { 72.dp.toPx() }
            with(density) {
                swipeToRefreshState.position.coerceAtMost(boxMaxHeightPx).toDp()
            }
        }
    }

    val isActive by remember(swipeToRefreshBoxHeight, successSwipe) {
        derivedStateOf {
            swipeToRefreshBoxHeight == 72.dp || successSwipe
        }
    }

    var bgColor by remember { mutableStateOf(SwipeToRefreshBoxInactiveColor) }
    val progress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(400)
    ) {
        bgColor = if (it == 1f) SwipeToRefreshBoxActiveColor else SwipeToRefreshBoxInactiveColor
    }

    val boxWidth = LocalConfiguration.current.screenWidthDp.dp
    val revealFrom by remember(boxWidth) {
        derivedStateOf {
            with(density) {
                Offset(
                    x = (20 + 8).dp.toPx(),
                    y = (swipeToRefreshBoxHeight - 8.dp).toPx()
                )
            }
        }
    }
    val maxRadius by remember(boxWidth) {
        derivedStateOf {
            val boxHeight = 72.dp
            val distanceToCornerX = boxWidth - 20.dp
            val distanceToCornerY = boxHeight - 8.dp
            with(density) {
                kotlin.math.max(distanceToCornerX.toPx(), distanceToCornerY.toPx())
            }
        }
    }
    val radius by remember(progress, swipeToRefreshState) {
        derivedStateOf {
            if (isActive) {
                com.github.tim06.wallet_contest.util.lerp(0f, maxRadius, progress)
            } else {
                com.github.tim06.wallet_contest.util.lerp(0f, maxRadius, 1 - progress)
            }
        }
    }
    val color by remember(swipeToRefreshState, successSwipe) {
        derivedStateOf {
            if (isActive) {
                SwipeToRefreshBoxActiveColor
            } else {
                SwipeToRefreshBoxInactiveColor
            }
        }
    }

    Box(
        modifier = modifier
            .height(swipeToRefreshBoxHeight)
            .fillMaxWidth()
            .clipToBounds()
            .background(color = bgColor)
            .drawBehind {
                drawCircle(
                    color = color,
                    radius = radius,
                    center = revealFrom
                )
            }
    ) {
        TonWalletMainHeaderSwipeToRefreshIndicator(
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
            swipeToRefreshState = swipeToRefreshState,
            isActive = isActive
        )
        Text(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            text = stringResource(id = if (isActive) R.string.wallet_main_refresh_release else R.string.wallet_main_refresh_swipe),
            style = MaterialTheme.typography.body1.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                color = Color.White
            )
        )
    }
}

@Composable
fun TonWalletMainHeaderSwipeToRefreshIndicator(
    modifier: Modifier = Modifier,
    swipeToRefreshState: SwipeToRefreshState,
    isActive: Boolean
) {
    val density = LocalDensity.current

    val progress by remember(swipeToRefreshState.position) {
        val max = with(density) { 72.dp.toPx() }
        val min = 0f
        derivedStateOf {
            ((swipeToRefreshState.position - min) / (max - min)).coerceIn(0f, 1f)
        }
    }

    val height by remember(progress) {
        derivedStateOf {
            lerp(16.dp, 56.dp, progress)
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isActive) 0f else 180f,
        animationSpec = tween(300)
    )

    val iconColor = if (isActive) SwipeToRefreshBoxActiveColor else SwipeToRefreshBoxInactiveColor

    Box(
        modifier = modifier
            .height(height)
            .background(color = WhiteAlpha12, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.rotate(rotation),
                painter = painterResource(id = R.drawable.ic_swipe_to_refresh_arrow),
                contentDescription = null,
                tint = iconColor
            )
        }
    }
}
