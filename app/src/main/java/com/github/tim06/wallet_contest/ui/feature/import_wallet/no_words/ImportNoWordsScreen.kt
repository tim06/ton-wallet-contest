package com.github.tim06.wallet_contest.ui.feature.import_wallet.no_words

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ui.components.InfoScreen

@Composable
fun ImportNoWordsScreen(
    onBackClick: () -> Unit,
    onEnterWordsClick: () -> Unit,
    onCreateNewClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val destiny = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val text = stringResource(id = R.string.import_no_words_button_second)
    val style = MaterialTheme.typography.caption
    val horizontalPadding = remember {
        derivedStateOf {
            val textWidth = with(destiny) {
                textMeasurer.measure(text = text, style = style).size.width.toDp()
            }
            val screenWidth = configuration.screenWidthDp.dp
            val buttonInsidePadding = 16.dp * 2
            ((screenWidth - textWidth - buttonInsidePadding) / 2)
        }
    }
    InfoScreen(
        icon = R.raw.too_bad,
        buttonsHorizontalPadding = horizontalPadding.value,
        title = stringResource(id = R.string.import_no_words_title),
        description = stringResource(id = R.string.import_no_words_description),
        firstButtonText = stringResource(id = R.string.import_no_words_button_first),
        firstButtonClick = onEnterWordsClick,
        secondButtonText = stringResource(id = R.string.import_no_words_button_second),
        secondButtonClick = onCreateNewClick,
        backClickListener = onBackClick
    )
}