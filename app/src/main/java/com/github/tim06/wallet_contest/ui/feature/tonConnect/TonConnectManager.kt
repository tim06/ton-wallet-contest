package com.github.tim06.wallet_contest.ui.feature.tonConnect

import android.net.Uri
import com.github.tim06.wallet_contest.crypto.generateX25519KeyPair
import com.github.tim06.wallet_contest.storage.Storage
import com.github.tim06.wallet_contest.ton.downloadFileToString
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionData
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionKeyData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class TonConnectManager(
    private val storage: Storage,
    private val client: OkHttpClient,
    private val scope: CoroutineScope
) {

    private val json = Json { ignoreUnknownKeys = true }

    private val connections: MutableList<TonConnectConnection> = mutableListOf()

    private val _events =
        MutableSharedFlow<List<SharedFlow<TonConnectEvent>>>(replay = 1, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var eventsLocal = listOf<SharedFlow<TonConnectEvent>>()
        set(value) {
            _events.tryEmit(value)
            field = value
        }

    suspend fun initTonConnectConnections() {
        storage.getTonConnectManifests1().filter { manifest ->
            connections.none { it.data.id == manifest.id }
        }.map { connectionData ->
            val lastEventId = storage.getTonConnectLastEventId(connectionData.id)
            connectionData.copy(lastEventId = lastEventId)
        }.forEach { connectionData ->
            initTonConnection(connectionData, save = true, toStorage = false)
        }
    }

    suspend fun processString(
        tonConnectData: String,
        currentWalletAddress: String
    ): TonConnectConnectionData {
        val uri = Uri.parse(tonConnectData)
        val manifestData = uri.getQueryParameter("r")
        val id = uri.getQueryParameter("id")

        val request = json.decodeFromString<TonConnectRequest>(manifestData.orEmpty())
        val manifestString = downloadFileToString(request.manifestUrl)
        val manifest = json.decodeFromString<TonConnectManifestModel>(manifestString)

        val (publicKey, privateKey) = generateX25519KeyPair()
        val data = TonConnectConnectionData(
            manifest = manifest,
            id = id.orEmpty(),
            key = TonConnectConnectionKeyData(publicKey, privateKey),
            walletAddress = currentWalletAddress,
            payload = request.items.find { it.name == "ton_proof" }?.payload
        )
        return data
    }

    suspend fun approveTonConnectionRequest(
        tonConnectData: TonConnectConnectionData,
        rawAddress: String,
        publicKey: String,
        walletStateInit: String,
        privateKey: ByteArray
    ): Boolean {
        val connection = initTonConnection(tonConnectData, save = true, toStorage = true)
        return connection.sendApproveRequest(
            rawAddress,
            publicKey,
            walletStateInit,
            privateKey
        )
    }

    suspend fun rejectConnectionRequest(data: TonConnectConnectionData) {
        val connection = initTonConnection(tonConnectConnectionData = data, save = false, false)
        connection.sendRejectConnectionRequest()
        connection.destroy()
    }

    suspend fun rejectRequest(data: TonConnectConnectionData) {
        getTonConnectionWithData(data)?.rejectTransferRequest()
    }

    fun deleteAppWithManifest(data: TonConnectConnectionData) {
        scope.launch(Dispatchers.IO) {
            connections.find { it.data.manifest == data.manifest }?.let { conn ->
                conn.sendRejectConnectionRequest()
                conn.destroy()
                connections.remove(conn)
            }
            storage.deleteTonConnectConnection(data)
        }
    }

    fun destroy() {
        connections.forEach { it.destroy() }
        eventsLocal = emptyList()
    }

    private fun initTonConnection(
        tonConnectConnectionData: TonConnectConnectionData,
        save: Boolean,
        toStorage: Boolean
    ): TonConnectConnection {
        val connection =
            TonConnectConnection(data = tonConnectConnectionData, storage = storage, scope = scope, client = client)
        connection.init()
        if (save) {
            connections.find { it.data.manifest == tonConnectConnectionData.manifest }
                ?.let { conn ->
                    conn.destroy()
                    connections.remove(conn)
                }
            connections.add(connection)
            eventsLocal = eventsLocal + connection.events
        }
        if (toStorage) {
            scope.launch(Dispatchers.IO) {
                storage.saveTonConnectManifest1(tonConnectConnectionData)
            }
        }
        return connection
    }

    private fun getTonConnectionWithData(tonConnectData: TonConnectConnectionData): TonConnectConnection? {
        return connections.find { it.data.id == tonConnectData.id }
    }
}