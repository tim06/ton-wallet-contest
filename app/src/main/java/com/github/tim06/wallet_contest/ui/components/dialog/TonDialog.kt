package com.github.tim06.wallet_contest.ui.components.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.tim06.wallet_contest.ui.components.button.TonTextButton

@Composable
fun TonDialog(
    @StringRes title: Int,
    @StringRes description: Int,
    @StringRes leftButtonText: Int? = null,
    @StringRes rightButtonText: Int,
    leftButtonClick: (() -> Unit)? = null,
    rightButtonClick: () -> Unit,
    dismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = dismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(start = 24.dp, top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = title),
                        style = MaterialTheme.typography.body2.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 19.sp,
                            lineHeight = 26.sp
                        )
                    )
                    Text(
                        text = stringResource(id = description),
                        style = MaterialTheme.typography.body2
                    )
                }
                Row(modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 12.dp)) {
                    if (leftButtonText != null && leftButtonClick != null) {
                        TonTextButton(text = stringResource(id = leftButtonText), click = leftButtonClick)
                    }
                    TonTextButton(text = stringResource(id = rightButtonText), click = rightButtonClick)
                }
            }
        }
    }
}