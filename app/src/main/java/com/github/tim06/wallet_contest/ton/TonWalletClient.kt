package com.github.tim06.wallet_contest.ton

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.core.content.FileProvider
import com.github.tim06.wallet_contest.BuildConfig
import com.github.tim06.wallet_contest.MainActivity
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.keystore.TonController
import com.github.tim06.wallet_contest.storage.*
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgDataEncryptedText
import com.github.tim06.wallet_contest.storage.transaction.toStorage
import com.github.tim06.wallet_contest.ui.feature.settings.WalletSettingsModel
import com.github.tim06.wallet_contest.ui.feature.tonConnect.TonConnectManager
import com.github.tim06.wallet_contest.ui.feature.tonConnect.hex
import com.github.tim06.wallet_contest.util.getWalletV4RawAddress
import drinkless.org.ton.Client
import drinkless.org.ton.TonApi
import drinkless.org.ton.TonApi.OptionsInfo
import drinkless.org.ton.TonApi.RawTransactions
import drinkless.org.ton.TonApi.WalletV3InitialAccountState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.telegram.time.FastDateFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.util.Locale
import javax.crypto.Cipher
import kotlin.coroutines.resume

class TonWalletClient(
    private val context: Context,
    private val storage: Storage,
    val tonConnectManager: TonConnectManager,
    private val coroutineScope: CoroutineScope,
    private val okHttpClient: OkHttpClient
) {

    private val biometricTonController = TonController(context, BIOMETRIC_TON_KEY_NAME)
    private val passcodeTonController = TonController(context, PASSCODE_TON_KEY_NAME)

    var isDeviceBiometricAllowed = false

    private val _syncProgressFlow = MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1)
    val syncProgressFlow = _syncProgressFlow.asSharedFlow()

    lateinit var walletData: WalletData

    private val client: Client = Client.create(
        { obj: TonApi.Object ->
            if (obj is TonApi.UpdateSyncState) {
                when (val state = obj.syncState) {
                    is TonApi.SyncStateInProgress -> {
                        val syncProgress =
                            ((state.currentSeqno - state.fromSeqno) / (state.toSeqno - state.fromSeqno).toDouble() * 100).toInt()
                        _syncProgressFlow.tryEmit(syncProgress)
                    }
                    is TonApi.SyncStateDone -> {
                        _syncProgressFlow.tryEmit(100)
                    }
                }
            }
        },
        { throwable: Throwable ->
            throwable.printStackTrace()
        },
        { throwable: Throwable ->
            throwable.printStackTrace()
        }
    )

    init {
        loadCurrentCurrency()
        initTonLib()
        isDeviceBiometricAllowed = BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        coroutineScope.launch(Dispatchers.IO) {
            storage.getCurrentWalletFlow().collect { updatedWalletData ->
                if (updatedWalletData != null) {
                    walletData = updatedWalletData
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            tonConnectManager.initTonConnectConnections()
        }
        coroutineScope.launch(Dispatchers.IO) {
            currentPrimaryCurrency = storage.getPrimaryCurrency()
        }
    }

    fun clear() {
        tonConnectManager.destroy()
    }

    private lateinit var optionsInfo: OptionsInfo
    private fun initTonLib() {
        coroutineScope.launch(Dispatchers.IO) {
            val keyDirectory = File(
                getFilesDirFixed(),
                "ton"
            )
            keyDirectory.mkdirs()

            val configString =
                downloadFileToString(url = "https://ton.org/global-config.json")
            val config = TonApi.Config(configString, "mainnet", false, false)
            val options =
                TonApi.Options(config, TonApi.KeyStoreTypeDirectory(keyDirectory.absolutePath))
            val optionsInfoResult = sendRequest(TonApi.Init(options))
            if (optionsInfoResult is OptionsInfo) {
                optionsInfo = optionsInfoResult
            }
        }
    }

    fun isWalletExists(): Boolean {
        return ::walletData.isInitialized
    }

    fun getCipher(): Cipher? = biometricTonController.cipherForDecrypt

    /**
     *  Create new wallet
     *
     *  @return Pair<ByteArray, TonApi.Key> or error
     */
    suspend fun createKey(): Pair<ByteArray, TonApi.Object> {
        val password = ByteArray(64)
        val seed = ByteArray(32)
        Util.random.nextBytes(password)
        Util.random.nextBytes(seed)
        return Pair(password, sendRequest(TonApi.CreateNewKey(password, ByteArray(0), seed)))
    }

    /**
     *  Import wallet
     *
     *  @param words secret words for import
     *
     *  @return [TonApi.Key] or error
     */
    suspend fun importKey(words: Array<String>): TonApi.Object {
        val password = ByteArray(64)
        Util.random.nextBytes(password)
        return sendRequest(TonApi.ImportKey(password, ByteArray(0), TonApi.ExportedKey(words)))
    }

    suspend fun importKeyWithPassword(words: Array<String>): Pair<ByteArray, TonApi.Object> {
        val password = ByteArray(64)
        Util.random.nextBytes(password)
        return Pair(
            password,
            sendRequest(TonApi.ImportKey(password, ByteArray(0), TonApi.ExportedKey(words)))
        )
    }

    suspend fun importKey(words: String): TonApi.Object {
        return importKey(words.split(" ").toTypedArray())
    }

    /**
     *  Load account address for publicKey
     *
     *  @param publicKey for search address
     *
     *  @return [TonApi.AccountRevisionList] or error
     */
    suspend fun guessAccountForKey(publicKey: String): TonApi.Object {
        return sendRequest(
            TonApi.GuessAccount(
                publicKey,
                optionsInfo.configInfo.defaultRwalletInitPublicKey
            )
        )
    }

    /**
     *  Load account status
     *
     *  @param tonAccountAddress address of account
     *
     *  @return [TonApi.FullAccountState] or error
     */
    suspend fun getAccountStatus(tonAccountAddress: String): TonApi.Object {
        return sendRequest(TonApi.GetAccountState(TonApi.AccountAddress(tonAccountAddress)))
    }

    suspend fun getAccountAddress(publicKey: String): TonApi.Object {
        return sendRequest(
            TonApi.GetAccountAddress(
                TonApi.WalletV3InitialAccountState(
                    publicKey,
                    optionsInfo.configInfo.defaultWalletId
                ), 0, 0
            )
        )
    }

    suspend fun getRawTransactions(
        address: TonApi.AccountAddress,
        fromTransaction: TonApi.InternalTransactionId
    ): TonApi.Object {
        return sendRequest(TonApi.RawGetTransactionsV2(null, address, fromTransaction, 100, false))
    }

    suspend fun importWallet(words: Array<String> = testWords): InputKeyRegular? {
        biometricTonController.createWallet(true, forPasscode = false)
        passcodeTonController.createWallet(false, forPasscode = true)
        val imported = importKeyWithPassword(words)
        val password = imported.first
        val key = imported.second
        if (key is TonApi.Key) {
            loadAccountRevisions(key.publicKey, true)
            val encryptedData = onFinishWalletCreate(password, key)
            passcodeTonController.onFinishWalletCreate(password, key)
            updatePasscodeData(encryptedBiometricData = encryptedData)
            return InputKeyRegular(
                key = Key(key.publicKey, key.secret),
                localPassword = password
            )
        } else {
            // TODO error with import key for words
        }
        return null
    }

    suspend fun createWallet(): InputKeyRegular? {
        biometricTonController.createWallet(useBiometric = true, forPasscode = false)
        passcodeTonController.createWallet(useBiometric = false, forPasscode = true)
        val pair = createKey()
        val password = pair.first
        val key = pair.second
        if (key is TonApi.Key) {
            val exportedKey = sendRequest(
                TonApi.ExportKey(
                    TonApi.InputKeyRegular(
                        key,
                        password
                    )
                )
            )
            if (exportedKey is TonApi.ExportedKey) {
                loadAccountRevisions(key.publicKey)
                val encryptedData = onFinishWalletCreate(password, key)
                passcodeTonController.onFinishWalletCreate(password, key)
                updatePasscodeData(encryptedBiometricData = encryptedData)

                return InputKeyRegular(
                    key = Key(key.publicKey, key.secret),
                    localPassword = password
                )
            }
        } else {

        }
        return null
    }

    suspend fun isWalletAddressExist(address: String): Boolean {
        val status = getAccountStatus(address)
        return if (status is TonApi.FullAccountState) {
            // TODO check
            true
        } else {
            false
        }
    }

    suspend fun resolveTonDnsAddress(address: String): String? {
        return dnsResolve(address)
    }


    private suspend fun dnsResolve(
        address: String,
        resolverAddress: TonApi.AccountAddress? = null
    ): String? {
        val root = sendRequest(TonApi.DnsResolve(resolverAddress, address, byteArrayOf(0), 0))
        return if (root is TonApi.DnsResolved) {
            val dnsEntry = root.entries.firstOrNull()
            if (dnsEntry != null) {
                val entry = dnsEntry.entry
                if (entry is TonApi.DnsEntryDataNextResolver) {
                    val splited = address.split(".")
                    if (splited.count() == 1) {
                        return entry.resolver.accountAddress
                    } else {
                        if (dnsEntry.name == splited.last()) {
                            dnsResolve(
                                address = splited.dropLast(1).joinToString(""),
                                resolverAddress = entry.resolver
                            )
                        } else if (dnsEntry.name == "${splited.last()}.") {
                            dnsResolve(
                                address = splited.dropLast(1).joinToString("").dropLast(1),
                                resolverAddress = entry.resolver
                            )
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    fun isValidWalletAddress(address: String): Boolean {
        return runBlocking {
            sendRequest(TonApi.UnpackAccountAddress(address)) is TonApi.UnpackedAccountAddress
        }
    }

    suspend fun calculateFee(
        address: String,
        amount: Long,
        comment: String? = null,
        unencrypted: Boolean = false
    ): FeeResponse {
        val currentWallet = storage.getCurrentWallet()
        val publicKey = currentWallet?.publicKey
        val msgData = if (unencrypted || comment.isNullOrEmpty()) {
            TonApi.MsgDataText(comment?.toByteArray())
        } else {
            TonApi.MsgDataDecryptedText(comment.toByteArray())
        }
        val actionMsg = TonApi.ActionMsg(
            Array<TonApi.MsgMessage>(1) {
                TonApi.MsgMessage(
                    TonApi.AccountAddress(address),
                    null,
                    amount,
                    msgData,
                    0
                )
            },
            true
        )
        val req = TonApi.CreateQuery(
            TonApi.InputKeyFake(),
            TonApi.AccountAddress(currentWallet?.address),
            0,
            actionMsg,
            TonApi.WalletV3InitialAccountState(
                publicKey,
                if (::optionsInfo.isInitialized) {
                    optionsInfo.configInfo.defaultWalletId
                } else {
                    0L
                }
            )
        )
        val queryResult = sendRequest(req)
        return if (queryResult is TonApi.QueryInfo) {
            val req2 = TonApi.QueryEstimateFees(queryResult.id, true)
            val feesResult = sendRequest(req2)
            if (feesResult is TonApi.QueryFees) {
                FeeResponse.Success(feesResult.sourceFees.fwdFee + feesResult.sourceFees.gasFee + feesResult.sourceFees.inFwdFee + feesResult.sourceFees.storageFee)
            } else {
                FeeResponse.Error
            }
        } else if (queryResult is TonApi.Error) {
            if (queryResult.message != null) {
                when {
                    queryResult.message.startsWith("MESSAGE_ENCRYPTION") -> {
                        calculateFee(address, amount, comment, true)
                    }
                    queryResult.message.startsWith("NOT_ENOUGH_FUNDS") -> {
                        FeeResponse.NotEnoughFunds
                    }
                    else -> FeeResponse.Error
                }
            } else {
                FeeResponse.Error
            }
        } else {
            FeeResponse.Error
        }
    }

    sealed interface FeeResponse {
        data class Success(val fee: Long) : FeeResponse
        object NotEnoughFunds : FeeResponse
        object Error : FeeResponse
    }

    suspend fun sendTon(
        keyRegular: TonApi.InputKeyRegular,
        message: String?,
        destinationAddress: String,
        amount: Long
    ): SendTonResponse {
        val currentWallet = storage.getCurrentWallet()
        val publicKey = currentWallet?.publicKey
        val currentWalletAddress = currentWallet?.address
        val msgData = TonApi.MsgDataText(message?.toByteArray())
        val actionMsg = TonApi.ActionMsg(
            Array<TonApi.MsgMessage>(1) {
                TonApi.MsgMessage(
                    TonApi.AccountAddress(destinationAddress),
                    null,
                    amount,
                    msgData,
                    0
                )
            },
            true
        )
        // TODO check address with send between wallets or other account
        val req = TonApi.CreateQuery(
            keyRegular,
            TonApi.AccountAddress(currentWalletAddress),
            0,
            actionMsg,
            TonApi.WalletV3InitialAccountState(
                publicKey,
                optionsInfo.configInfo.defaultWalletId
            )
        )
        val result = sendRequest(req)
        return if (result is TonApi.QueryInfo) {
            val req2 = TonApi.QuerySend(result.id)
            val result2 = sendRequest(req2)
            if (result2 is TonApi.Ok) {
                updateCurrentAccountState(3000)
                SendTonResponse.Success
            } else {
                SendTonResponse.Error
            }
        } else {
            SendTonResponse.Error
        }
    }

    sealed interface SendTonResponse {
        object Success : SendTonResponse
        object NotEnoughFunds : SendTonResponse
        object Error : SendTonResponse
    }

    fun getFilesDirFixed(): File {
        for (a in 0..9) {
            val path: File? = context.applicationContext.filesDir
            if (path != null) {
                return path
            }
        }
        try {
            val info: ApplicationInfo = context.applicationContext.applicationInfo
            val path = File(info.dataDir, "files")
            path.mkdirs()
            return path
        } catch (e: Exception) {
            Log.e("TonWalletClient", e.message.orEmpty())
        }
        return File("/data/data/org.telegram.messenger/files")
    }

    private suspend fun sendRequest(function: TonApi.Function) =
        suspendCancellableCoroutine<TonApi.Object> { cancellableContinuation ->
            // TODO add error handling
            client.send(function, cancellableContinuation::resume)
        }


// Transaction messages

// TODO
/*suspend fun decryptTransactionMessage(
    transaction: RawTransaction
) {
    val req = TonApi.MsgDecrypt(
        memInputKey,
        TonApi.MsgDataEncryptedArray(
            arrayOf<MsgDataEncrypted>(
                MsgDataEncrypted(
                    TonApi.AccountAddress(transaction.getDestinationOrSourceAddress()),
                    transaction.getMsg()?.toApi()
                )
            )
        )
    )
    val result = sendRequest(req)
    if (result is TonApi.MsgDataDecryptedArray) {
        val decrypted = result.elements.firstOrNull()?.data
        if (decrypted is TonApi.MsgDataDecryptedText) {
            try {
                val str = String(
                    decrypted.text,
                    0,
                    decrypted.text.size,
                    StandardCharsets.UTF_8
                )
                storage.setDecryptedMessageToTransaction(
                    transaction,
                    decrypted
                )
            } catch (e: Exception) {
                //FileLog.e(e)
            }
        }
    }
}*/


// Passcode

    fun setUserPasscode(passcode: String, type: Int) {
        passcodeTonController.setUserPasscode(
            passcode
        ) { encryptedData: String, salt: ByteArray ->
            updatePasscodeData(encryptedPinData = encryptedData, passcodeSalt = salt)
        }
    }

    suspend fun prepareForPasscodeChange(passcode: String) {
        val encryptedData = storage.getPasscodeEncryptedData()
        passcodeTonController.decryptTonData(
            passcode,
            null,
            null,
            true,
            encryptedData?.encryptedPinData,
            encryptedData?.passcodeSalt,
            0,
            encryptedData?.tonPublicKey
        )
    }

    suspend fun getInputKeyWithCipher(passcode: String): InputKeyRegular? {
        val encryptedData = storage.getPasscodeEncryptedData()
        val inputKey = passcodeTonController.decryptTonData(
            passcode,
            null,
            null,
            false,
            encryptedData?.encryptedPinData,
            encryptedData?.passcodeSalt,
            0,
            encryptedData?.tonPublicKey
        ) as? TonApi.InputKeyRegular
        inputKeyTemp = inputKey
        return inputKey?.toStorage()
    }

    suspend fun getInputKeyWithCipher(cipher: Cipher): InputKeyRegular? {
        val encryptedData = storage.getPasscodeEncryptedData()
        val inputKey = biometricTonController.decryptTonData(
            null,
            cipher,
            null,
            false,
            encryptedData?.encryptedBiometricData,
            encryptedData?.passcodeSalt,
            -1,
            encryptedData?.tonPublicKey
        ) as? TonApi.InputKeyRegular
        return inputKey?.toStorage()
    }

// Words

    suspend fun getSecretWords(key: InputKeyRegular): Array<String> {
        val tonApiKey = TonApi.InputKeyRegular(
            TonApi.Key(
                key.key.publicKey,
                key.key.secret
            ),
            key.localPassword
        )
        val result = sendRequest(TonApi.ExportKey(tonApiKey))
        return if (result is TonApi.ExportedKey) {
            result.wordList
        } else {
            emptyArray()
        }
    }

    fun finishCreateImportWallet(key: TonApi.InputKeyRegular) {
        coroutineScope.launch(Dispatchers.IO) {
            updatePasscodeData(tonPublicKey = key.key.publicKey)
        }
    }

// Storage

    fun getCurrentWalletFlow(): Flow<WalletData?> = storage.getCurrentWalletFlow()
    suspend fun getCurrentWallet(): WalletData? = storage.getCurrentWallet()

    fun getTransactions(address: String): Flow<Map<String, List<RawTransaction>>> =
        storage.getTransactionsFlow(address)
            .onEach { rawTransactions ->
                rawTransactions.forEach { transaction ->
                    if (transaction.getMsg() is MsgDataEncryptedText) {
                        // TODO
                        //decryptTransactionMessage(transaction)
                    }
                }
            }
            .map { transaction ->
                val date = FastDateFormat.getInstance("MMMM d", Locale.ENGLISH)
                transaction.groupBy { date.format(it.utime * 1000) }
            }

    fun getRawTransactionsFlow(address: String): Flow<List<RawTransaction>> =
        storage.getTransactionsFlow(address)

    suspend fun getTransactionWithId(transactionId: Long): RawTransaction? {
        return storage.getTransactionWithId(transactionId)
    }

    fun updatePasscodeData(
        tonPublicKey: String? = null,
        encryptedPinData: String? = null,
        encryptedBiometricData: String? = null,
        passcodeSalt: ByteArray? = null,
        passcodeType: Int? = null
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            storage.updatePasscodeData(
                tonPublicKey,
                encryptedPinData,
                encryptedBiometricData,
                passcodeSalt,
                passcodeType
            )
        }
    }

    // Settings
    fun getNotificationsFlow(): Flow<Boolean> = storage.getNotificationsStateFlow()
    fun getPrimaryCurrencyFlow(): Flow<PrimaryCurrency> = storage.getPrimaryCurrencyFlow()
    fun getBiometricAuthFlow(): Flow<Boolean> = storage.getBiometricAuthFlow()
    fun getCurrentWalletAddressVersion(): Flow<WalletVersion> =
        storage.getCurrentWalletFlow().map { it?.walletVersion ?: WalletVersion.V3R2 }

    fun getWalletSettingsModelFlow(): Flow<List<WalletSettingsModel>> =
        storage.getWalletSettingsModelFlow()

    fun saveNotificationsState(enabled: Boolean) =
        coroutineScope.launch(Dispatchers.IO) { storage.saveNotificationsState(enabled) }

    fun saveCurrentAddressVersion(version: WalletVersion) =
        coroutineScope.launch(Dispatchers.IO) { storage.setCurrentWalletVersion(version) }

    fun savePrimaryCurrency(currency: PrimaryCurrency) {
        currentPrimaryCurrency = currency
        loadCurrency(currency)
        coroutineScope.launch(Dispatchers.IO) { storage.savePrimaryCurrency(currency) }
    }

    fun saveBiometricAuthState(enabled: Boolean) =
        coroutineScope.launch(Dispatchers.IO) { storage.saveBiometricAuthState(enabled) }


// Sync

    fun updateCurrentAccountState(delay: Long = 0L) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(delay)
            storage.getCurrentWallet()?.let { walletData ->
                val accountStatus = getAccountStatus(walletData.address)
                if (accountStatus is TonApi.FullAccountState) {
                    storage.getCurrentWallet()?.lastTransactionId?.let { savedLastTransactionId ->
                        if (accountStatus.lastTransactionId.toStorage() != savedLastTransactionId) {
                            val transactions = getRawTransactions(
                                accountStatus.address,
                                accountStatus.lastTransactionId
                            )
                            if (transactions is TonApi.RawTransactions) {
                                storage.saveTransactions(
                                    transactions.transactions.map { it.toStorage() },
                                    if (accountStatus.revision == 1) WalletVersion.V3R1 else WalletVersion.V3R2
                                )
                            }
                        }
                    }
                    storage.saveWalletData(
                        data = WalletData(
                            walletData.address,
                            walletData.publicKey,
                            walletData.walletVersion,
                            accountStatus.lastTransactionId.toStorage(),
                            accountStatus.balance,
                        )
                    )
                }
            }
        }
    }

    fun loadTransactionsForAddress(
        walletVersion: WalletVersion,
        address: TonApi.AccountAddress,
        lastTransactionId: TonApi.InternalTransactionId
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val transactions = getRawTransactions(address, lastTransactionId)
            if (transactions is RawTransactions) {
                storage.saveTransactions(
                    transactions.transactions.map {
                        it.toStorage()
                    },
                    walletVersion
                )
            } else {
                // TODO error transactions
            }
        }
    }

    // TODO add v4 wallet
    private suspend fun loadAccountRevisions(publicKey: String, loadTransactions: Boolean = false) {
        // V3R1
        val accountAddress1 = sendRequest(
            TonApi.GetAccountAddress(
                WalletV3InitialAccountState(
                    publicKey,
                    optionsInfo.configInfo.defaultWalletId
                ), 1, 0
            )
        )
        // V3R2
        val accountAddress2 = sendRequest(
            TonApi.GetAccountAddress(
                WalletV3InitialAccountState(
                    publicKey,
                    optionsInfo.configInfo.defaultWalletId
                ), 2, 0
            )
        )
        // V4R2
        val unpackedResult = sendRequest(TonApi.UnpackAccountAddress(getWalletV4RawAddress(publicKey)))
        val accountAddressV4 = if (unpackedResult is TonApi.UnpackedAccountAddress) {
            val packedResult = sendRequest(TonApi.PackAccountAddress(unpackedResult))
            if (packedResult is TonApi.AccountAddress) {
                packedResult
            } else {
                null
            }
        } else {
            null
        }

        mapOf(
            WalletVersion.V3R1 to accountAddress1,
            WalletVersion.V3R2 to accountAddress2,
            WalletVersion.V4R2 to accountAddressV4
        ).filter { it.value is TonApi.AccountAddress }.forEach { entry ->
            val accountState = sendRequest(TonApi.GetAccountState(entry.value as TonApi.AccountAddress))
            if (accountState is TonApi.FullAccountState) {
                storage.saveWalletData(
                    data = WalletData(
                        address = accountState.address.accountAddress,
                        walletVersion = entry.key,
                        lastTransactionId = accountState.lastTransactionId.toStorage(),
                        balance = accountState.balance,
                        publicKey = publicKey
                    )
                )
                if (loadTransactions) {
                    loadTransactionsForAddress(
                        walletVersion = entry.key,
                        address = entry.value as TonApi.AccountAddress,
                        lastTransactionId = accountState.lastTransactionId
                    )
                }
            }
        }
    }


    private suspend fun onFinishWalletCreate(password: ByteArray, key: TonApi.Key) =
        suspendCancellableCoroutine<String> { continuation ->
            biometricTonController.onFinishWalletCreate(
                password,
                key
            ) { encryptedData: String? ->
                if (encryptedData != null) {
                    continuation.resume(encryptedData)
                } else {
                    continuation.resume("")
                }
            }
        }

    // Ton Connect
    suspend fun getCurrentRawAddress(): String? {
        return storage.getCurrentWallet()?.address?.let { getRawAddress(it) }
    }

    suspend fun getRawAddress(address: String): String? {
        val result = sendRequest(TonApi.UnpackAccountAddress(address))
        return if (result is TonApi.UnpackedAccountAddress) {
            "${result.workchainId}:${hex(result.addr)}"
        } else {
            null
        }
    }

    suspend fun getAddressFromRaw(rawAddress: String): String? {
        val unpackedResult = sendRequest(TonApi.UnpackAccountAddress(rawAddress))
        return if (unpackedResult is TonApi.UnpackedAccountAddress) {
            val result = sendRequest(TonApi.PackAccountAddress(unpackedResult))
            if (result is TonApi.AccountAddress) {
                result.accountAddress
            } else {
                null
            }
        } else {
            null
        }
    }

    suspend fun getCurrentWalletStateInit(): String? {
        return storage.getCurrentWallet()?.address?.let { getWalletStateInit(it) }
    }

    suspend fun getWalletStateInit(address: String): String? {
        val result = sendRequest(TonApi.RawGetAccountState(TonApi.AccountAddress(address)))
        return if (result is TonApi.RawFullAccountState) {
            hex(result.data)
        } else {
            null
        }
    }

    private var inputKeyTemp: TonApi.InputKeyRegular? = null

    suspend fun getPrivateKeyTempData(): ByteArray? {
        val keyArray = inputKeyTemp?.let { getPrivateKey(it) }
        inputKeyTemp = null
        return keyArray
    }

    fun getPrivateKeyTemp(): TonApi.InputKeyRegular? {
        val key = inputKeyTemp
        inputKeyTemp = null
        return key
    }

    fun isPrivateKeyWait(): Boolean = inputKeyTemp != null

    suspend fun getPrivateKey(key: TonApi.InputKeyRegular): ByteArray? {
        val result = sendRequest(
            TonApi.ExportUnencryptedKey(key)
        )
        return if (result is TonApi.ExportedUnencryptedKey) {
            result.data
        } else {
            null
        }
    }

    fun destroy() {
        coroutineScope.launch(Dispatchers.IO) {
            sendRequest(TonApi.Close())
        }
    }


    val currentCurrency = MutableStateFlow<BigDecimal?>(null)
    var currentPrimaryCurrency: PrimaryCurrency = PrimaryCurrency.USD

    private fun loadCurrentCurrency() {
        coroutineScope.launch(Dispatchers.IO) {
            val currency = storage.getPrimaryCurrency()
            loadCurrency(currency)
        }
    }

    private fun loadCurrency(currency: PrimaryCurrency) {
        okHttpClient.newCall(
            Request.Builder()
                .url("https://tonapi.io/v2/rates?tokens=ton&currencies=${currency.name.lowercase()}")
                .get()
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                kotlin.runCatching {
                    response.body?.string()
                        ?.split(":")
                        ?.last()
                        ?.dropLast(4)
                        ?.toBigDecimal()
                        ?.let {
                            currentCurrency.value = it
                        }
                }
            }
        })
    }

    suspend fun getWordsSuggestion(prefix: String): Array<String> {
        val result = sendRequest(TonApi.GetBip39Hints(prefix))
        return if (result is TonApi.Bip39Hints) {
            result.words
        } else {
            emptyArray()
        }
    }

    fun shareBitmap(bitmap: Bitmap, text: String?) {
        try {
            var f: File = context.getSharingDir()
            f.mkdirs()
            f = File(f, "qr.jpg")
            val outputStream = FileOutputStream(f.absolutePath)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 87, outputStream)
            outputStream.close()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/jpeg"
            if (!TextUtils.isEmpty(text)) {
                intent.putExtra(Intent.EXTRA_TEXT, text)
            }
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    intent.putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            context,
                            BuildConfig.APPLICATION_ID + ".provider",
                            f
                        )
                    )
                    intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                } catch (ignore: java.lang.Exception) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f))
                }
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f))
            }
            (context as MainActivity).startActivityForResult(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.WalletShareQr)
                ), 500
            )
        } catch (e: java.lang.Exception) {

        }
    }

    private companion object {
        private const val BIOMETRIC_TON_KEY_NAME = "biometric_ton_key_name"
        private const val PASSCODE_TON_KEY_NAME = "passcode_ton_key_name"
    }
}

fun TonApi.InputKeyRegular.toStorage(): InputKeyRegular {
    return InputKeyRegular(
        key = Key(
            key.publicKey,
            key.secret
        ),
        localPassword = localPassword
    )
}

fun Context.getSharingDir(): File {
    return File(this.applicationContext.getCacheDir(), "sharing/");
}