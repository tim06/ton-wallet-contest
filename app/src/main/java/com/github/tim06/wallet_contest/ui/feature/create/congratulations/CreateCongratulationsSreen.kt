package com.github.tim06.wallet_contest.ui.feature.create.congratulations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.InfoScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun CreateCongratulationsScreen(
    tonWalletClient: TonWalletClient,
    onProceedClick: (String) -> Unit,
    backClickListener: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Box(modifier = Modifier.fillMaxSize()) {
        InfoScreen(
            modifier = Modifier.alpha(if (isLoading) 0.5f else 1f),
            icon = R.raw.congratulations,
            title = stringResource(id = R.string.create_congratulations_title),
            description = stringResource(id = R.string.create_congratulations_description),
            firstButtonText = stringResource(id = R.string.create_congratulations_proceed),
            firstButtonClick = {
                isLoading = true
                coroutineScope.launch(Dispatchers.IO) {
                    val inputKey = tonWalletClient.createWallet()
                    if (inputKey != null) {
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            onProceedClick.invoke(Json.encodeToString(inputKey))
                        }
                    }
                }
            },
            backClickListener = backClickListener
        )

        // TODO move loading to button
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}