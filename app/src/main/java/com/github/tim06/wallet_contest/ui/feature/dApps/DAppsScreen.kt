package com.github.tim06.wallet_contest.ui.feature.dApps

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.github.tim06.wallet_contest.storage.Storage
import com.github.tim06.wallet_contest.ui.components.topBar.TonTopAppBar
import com.github.tim06.wallet_contest.ui.feature.tonConnect.TonConnectManager
import com.github.tim06.wallet_contest.ui.theme.ErrorColor
import com.github.tim06.wallet_contest.util.SystemBarIconsDark

@Composable
fun DAppsScreen(
    tonConnectManager: TonConnectManager,
    storage: Storage,
    onBackClick: () -> Unit
) {
    SystemBarIconsDark(isDark = true)
    val manifests by storage.getTonConnectManifests1Flow().collectAsState(initial = emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .systemBarsPadding()
    ) {
        TonTopAppBar(title = "dApps", backClick = onBackClick)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (manifests.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "Not found"
                )
            } else {
                manifests.forEach { data ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                model = data.manifest.iconUrl,
                                loading = { CircularProgressIndicator() },
                                contentDescription = "Ton Connect resource logo"
                            )
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = data.manifest.name,
                                    style = MaterialTheme.typography.body1.copy(
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = Uri.parse(data.manifest.url).host.orEmpty(),
                                    style = MaterialTheme.typography.body2.copy(
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        TextButton(
                            modifier = Modifier.padding(end = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            onClick = {
                                tonConnectManager.deleteAppWithManifest(data)
                            },
                            content = {
                                Text(
                                    text = "Disconnect",
                                    style = MaterialTheme.typography.caption.copy(color = ErrorColor)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}