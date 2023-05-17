package com.github.tim06.wallet_contest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.tim06.wallet_contest.storage.Storage
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.feature.main.DeeplinkModel
import com.github.tim06.wallet_contest.ui.feature.tonConnect.TonConnectManager
import com.github.tim06.wallet_contest.ui.navigation.Navigation
import com.github.tim06.wallet_contest.ui.theme.WalletcontestTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : FragmentActivity() {

    private var walletClient: TonWalletClient? = null
    private val _deeplinkModelFlow = MutableSharedFlow<DeeplinkModel?>(replay = 1, extraBufferCapacity = 1)
    private val deeplinkModelFlow = _deeplinkModelFlow.asSharedFlow()

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()

        val storage = Storage(this.dataStore)
        val tonConnectManager = TonConnectManager(storage, okHttpClient, lifecycleScope)

        walletClient = TonWalletClient(this, storage, tonConnectManager, lifecycleScope, okHttpClient)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            WalletcontestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CompositionLocalProvider(
                        LocalOverscrollConfiguration provides null
                    ) {
                        val systemUiController = rememberSystemUiController()
                        SideEffect {
                            systemUiController.setSystemBarsColor(
                                color = Color.Transparent,
                                darkIcons = true
                            )
                        }
                        Navigation(walletClient!!, storage, deeplinkModelFlow)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.dataString?.let { data ->
            val uri = Uri.parse(data)
            val walletAddress = uri.getPath()?.drop(1).orEmpty()
            val amount = uri.getQueryParameter("amount")
            val text = uri.getQueryParameter("text")
            DeeplinkModel(walletAddress, amount, text)
        }?.let {
            _deeplinkModelFlow.tryEmit(it)
            lifecycleScope.launch {
                delay(500)
                _deeplinkModelFlow.tryEmit(null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO check biometric key is invalidated isKeyStoreInvalidated()
    }

    override fun onDestroy() {
        super.onDestroy()
        walletClient?.destroy()
        walletClient = null
    }
}