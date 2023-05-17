package com.github.tim06.wallet_contest.ui.feature.tonConnect

import com.github.tim06.wallet_contest.crypto.decryptMessage
import com.github.tim06.wallet_contest.crypto.encryptMessage
import com.github.tim06.wallet_contest.storage.Storage
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.ConnectItemReply
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.DeviceInfo
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonAddressItemReply
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionData
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectError
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectErrorRequest
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectWalletEvent
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonProof
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonProofDomain
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonProofItemReplySuccess
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload.TonConnectConnectionPayload
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload.TonConnectPayload
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.payload.TonConnectRejectPayload
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response.TonConnectMethodParamsResponse
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response.TonConnectMethodResponse
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.response.TonConnectResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.IOException
import org.libsodium.jni.encoders.Encoder
import org.libsodium.jni.keys.KeyPair
import kotlin.coroutines.resume

class TonConnectConnection(
    private val storage: Storage,
    val data: TonConnectConnectionData,
    private val scope: CoroutineScope,
    private val client: OkHttpClient
) {

    private val clientKeyPair = KeyPair(hex(data.key.privateKey), Encoder.HEX)
    private val eventsUrl = if (data.lastEventId == -1L) {
        "${BRIDGE_URL}events?client_id=${clientKeyPair.publicKey}"
    } else {
        "${BRIDGE_URL}events?client_id=${clientKeyPair.publicKey}&last_event_id=${data.lastEventId}"
    }

    private val _events = MutableSharedFlow<TonConnectEvent>(replay = 1, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    var isConnected: Boolean = false

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(ConnectItemReply::class) {
                subclass(TonProofItemReplySuccess::class)
                subclass(TonAddressItemReply::class)
            }
            polymorphic(TonConnectPayload::class) {
                subclass(TonConnectConnectionPayload::class)
                subclass(TonConnectRejectPayload::class)
            }
        }
        explicitNulls = false
        encodeDefaults = true
    }

    private var clientLastEventId: Long = 1
    private lateinit var eventSource: EventSource

    private val eventSourceListener = object : EventSourceListener() {
        override fun onOpen(eventSource: EventSource, response: Response) {
            super.onOpen(eventSource, response)
            isConnected = true
            println("TonConnect onOpen(): $response")
        }

        override fun onClosed(eventSource: EventSource) {
            super.onClosed(eventSource)
            isConnected = false
            println("TonConnect onClosed()")
        }

        override fun onEvent(
            eventSource: EventSource,
            id: String?,
            type: String?,
            data: String
        ) {
            super.onEvent(eventSource, id, type, data)
            id?.let {
                scope.launch(Dispatchers.IO) {
                    storage.saveTonConnectLastEventId(this@TonConnectConnection.data.id, lastEventId = id.toLong())
                }
            }
            val serializedResponse = Json.decodeFromString<TonConnectResponse>(data)
            val decryptedMessage = decryptMessage(
                base64(serializedResponse.message),
                hex(serializedResponse.from),
                clientKeyPair.privateKey.toBytes()
            )
            val model =
                json.decodeFromString<TonConnectMethodResponse>(decryptedMessage.decodeToString())
            when (model.method) {
                "sendTransaction" -> {
                    val response =
                        json.decodeFromString<TonConnectMethodParamsResponse>(model.params.first())
                    response.messages.firstOrNull()?.let { transactionRequest ->
                        _events.tryEmit(
                            TonConnectEvent.TonConnectTransactionRequest(
                                this@TonConnectConnection.data,
                                this@TonConnectConnection.data.walletAddress,
                                transactionRequest.address,
                                transactionRequest.amount
                            )
                        )
                    }
                }
                else -> throw UnsupportedOperationException("Can't process method with name ${model.method}")
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            super.onFailure(eventSource, t, response)
            // TODO check isConnected
            isConnected = false
            println("TonConnect onFailure(): response = $response, throwable = $t")
        }
    }

    fun init() {
        val request = Request.Builder()
            .url(eventsUrl)
            .addHeader("Accept", "text/event-stream")
            .get()
            .build()
        eventSource = EventSources.createFactory(client)
            .newEventSource(request = request, listener = eventSourceListener)
    }

    fun destroy() {
        eventSource.cancel()
    }

    suspend fun sendApproveRequest(
        rawAddress: String,
        publicKey: String,
        walletStateInit: String,
        privateKey: ByteArray
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val tonProofDomain = TonProofDomain(
            lengthBytes = 21,
            value = "ton-connect.github.io"
        )

        val timestamp = (System.currentTimeMillis() / 1000L)

        val signature = base64(
            createTonProofItemSignature(
                walletAddress = hex(rawAddress.drop(2)),
                appDomain = "ton-connect.github.io",
                timestamp = timestamp,
                payload = data.payload?.toByteArray() ?: byteArrayOf(),
                privateKey = privateKey
            )
        )

        val tonProof = TonProof(
            timestamp = timestamp,
            domain = tonProofDomain,
            signature = signature,
            payload = data.payload
        )

        val requestRaw = TonConnectWalletEvent(
            event = "connect",
            id = clientLastEventId++,
            payload = TonConnectConnectionPayload(
                items = listOf(
                    TonAddressItemReply(
                        address = rawAddress,
                        publicKey = publicKey,
                        walletStateInit = walletStateInit
                    ),
                    TonProofItemReplySuccess(
                        proof = tonProof
                    )
                ),
                device = DeviceInfo(
                    "android",
                    "Tonkeeper",
                    "3.1.313",
                    "2",
                    listOf(
                        "SendTransaction"
                    )
                )
            )
        )

        val fixedRequestMap = fixTypeFieldInList(requestRaw)
        sendRequest(
            request = json.encodeToString(fixedRequestMap),
            onSuccess = { continuation.resume(it) },
            onFail = { continuation.resume(false) }
        )
    }

    suspend fun sendRejectConnectionRequest(): Boolean =
        suspendCancellableCoroutine { continuation ->
            val requestModel = TonConnectWalletEvent(
                event = "connect_error",
                id = clientLastEventId++,
                payload = TonConnectRejectPayload(
                    code = 300,
                    message = "Wallet declined the request"
                ),
            )
            sendRequest(
                request = json.encodeToString(fixTypeFieldInPayload(requestModel)),
                onSuccess = { continuation.resume(it) },
                onFail = { continuation.resume(false) }
            )
        }

    suspend fun rejectTransferRequest(): Boolean = suspendCancellableCoroutine { continuation ->
        val requestModel = TonConnectErrorRequest(
            id = clientLastEventId++,
            error = TonConnectError(
                code = 300,
                message = "Wallet declined the request"
            )
        )
        sendRequest(
            request = json.encodeToString(requestModel),
            onSuccess = { continuation.resume(it) },
            onFail = { continuation.resume(false) }
        )
    }

    private fun sendRequest(
        request: String,
        onSuccess: (Boolean) -> Unit,
        onFail: (String) -> Unit
    ) {
        val req = encryptMessage(
            message = request.toByteArray(),
            recipientPublicKey = hex(data.id),
            senderPrivateKey = clientKeyPair.privateKey.toBytes()
        )
        val base64Request = base64(req)
        val body: RequestBody = base64Request.toRequestBody("application/json".toMediaTypeOrNull())
        client.newCall(
            Request.Builder()
                .url(
                    "${BRIDGE_URL}message?client_id=${
                        clientKeyPair.publicKey
                    }&to=${data.id}&ttl=300"
                )
                .post(body)
                .build()
        ).enqueue(responseCallback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFail.invoke(e.message.orEmpty())
            }

            override fun onResponse(call: Call, response: Response) {
                onSuccess.invoke(response.code in 200..299)
            }
        })
    }

    private fun fixTypeFieldInList(request: TonConnectWalletEvent): MutableMap<String, JsonElement> {
        val encodedJsonElement = json.encodeToJsonElement(request)
        val valuesWithoutType = encodedJsonElement.jsonObject.filterNot { it.key == "type" }

        // Remove type field from kotlinx.serialization
        val fixedItemsMap =
            (valuesWithoutType.get("payload") as JsonObject).get("items")?.jsonArray?.map {
                it.jsonObject.toMutableMap().apply { remove("type") }
            } ?: emptyList()
        val deviceObj = (valuesWithoutType.get("payload") as JsonObject).get("device")?.jsonObject
            ?: JsonObject(mapOf())
        val fixedMap = valuesWithoutType.toMutableMap().apply {
            val newPayload = JsonObject(
                content = mapOf(
                    "items" to JsonArray(
                        content = fixedItemsMap.map { JsonObject(it) }
                    ),
                    "device" to deviceObj
                )
            )
            put("payload", newPayload)
        }
        return fixedMap
    }

    private fun fixTypeFieldInPayload(request: TonConnectWalletEvent): MutableMap<String, JsonElement> {
        val encodedJsonElement = json.encodeToJsonElement(request)

        return encodedJsonElement.jsonObject.toMutableMap().apply {
            get("payload")?.jsonObject?.toMutableMap()?.apply {
                get("code")?.let { put("code", it) }
                get("message")?.let { put("message", it) }
            }?.let { updatedPayload ->
                put("payload", json.encodeToJsonElement(updatedPayload))
            }
        }
    }

    private companion object {
        private const val BRIDGE_URL = "https://bridge.tonapi.io/bridge/"
    }
}