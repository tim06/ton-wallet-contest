package com.github.tim06.wallet_contest.ui.components.topBar

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R

@Composable
fun TonMainTopAppBar(
    modifier: Modifier = Modifier,
    status: String,
    titleAlpha: Float = 1f,
    backgroundColor: Color = Color.Transparent,
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = backgroundColor,
        title = {
            Text(
                modifier = Modifier.alpha(titleAlpha),
                text = status,
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = Color.White
                )
            )
        },
        actions = {
            IconButton(onClick = onScanClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_scan),
                    contentDescription = "Scan",
                    tint = MaterialTheme.colors.onSurface
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        },
        elevation = 0.dp
    )
}