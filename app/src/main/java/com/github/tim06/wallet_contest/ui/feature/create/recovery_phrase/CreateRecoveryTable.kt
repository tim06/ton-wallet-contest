package com.github.tim06.wallet_contest.ui.feature.create.recovery_phrase

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CreateRecoveryTable(
    modifier: Modifier = Modifier,
    items: Array<String>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(items.count() / 2) { index ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableItem(modifier = Modifier.weight(1.4f).padding(start = 8.dp), index + 1, items[index])
                TableItem(modifier = Modifier.weight(1f).padding(start = 8.dp),index + 1 + 12, items[index + 12])
            }
        }
    }
}

@Composable
private fun TableItem(modifier: Modifier, index: Int, value: String) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.secondary)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CreateRecoveryTablePreview() {
    CreateRecoveryTable(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        items = arrayOf(
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test",
            "test", "test"
        )
    )
}

val previewTableItems = listOf(
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test",
    "test", "test"
)