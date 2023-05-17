package com.github.tim06.wallet_contest.ui.components.topBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.ui.components.BackIcon

@Composable
fun TonTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = Color.Black,
    elevation: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colors.background,
    onTitlePositioned: ((LayoutCoordinates) -> Unit)? = null,
    backIconColor: Color = Color.Black,
    backClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        TopAppBar(
            modifier = Modifier,
            title = {
                Text(
                    modifier = if (onTitlePositioned != null) {
                        Modifier.onGloballyPositioned { layoutCoordinates ->
                            onTitlePositioned.invoke(layoutCoordinates)
                        }
                    } else {
                        Modifier
                    },
                    text = title,
                    color = titleColor
                )
            },
            navigationIcon = {
                backClick?.let {
                    BackIcon(iconColor = backIconColor, click = backClick)
                }
            },
            elevation = 0.dp,
            backgroundColor = backgroundColor
        )
        Box(modifier = Modifier.align(Alignment.BottomStart).height(0.5.dp).fillMaxSize().shadow(elevation))
    }
}

@Preview
@Composable
private fun TonTopAppBarPreview() {
    TonTopAppBar(title = "Title")
}