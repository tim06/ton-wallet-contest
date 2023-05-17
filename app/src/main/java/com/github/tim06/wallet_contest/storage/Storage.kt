package com.github.tim06.wallet_contest.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.github.tim06.wallet_contest.storage.transaction.PasscodeData
import com.github.tim06.wallet_contest.storage.transaction.RawTransaction
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgData
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgDataDecryptedText
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgDataEncryptedText
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgDataRaw
import com.github.tim06.wallet_contest.storage.transaction.msg.MsgDataText
import com.github.tim06.wallet_contest.storage.transaction.msg.toStorage
import com.github.tim06.wallet_contest.ui.feature.settings.WalletSettingsModel
import com.github.tim06.wallet_contest.ui.feature.tonConnect.model.TonConnectConnectionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.Locale

class Storage(
    private val dataStore: DataStore<Preferences>
) {
    val module = SerializersModule {
        polymorphic(MsgData::class) {
            subclass(MsgDataDecryptedText::class, MsgDataDecryptedText.serializer())
            subclass(MsgDataEncryptedText::class, MsgDataEncryptedText.serializer())
            subclass(MsgDataRaw::class, MsgDataRaw.serializer())
            subclass(MsgDataText::class, MsgDataText.serializer())
        }
    }

    private val json = Json {
        serializersModule = module
        allowStructuredMapKeys = true
    }

    suspend fun isWalletExist(): Boolean {
        val preferences = dataStore.data.firstOrNull()
        return if (preferences == null) {
            return false
        } else {
            preferences.contains(stringPreferencesKey(WALLET_ADDRESS_v3R1))
                    || preferences.contains(stringPreferencesKey(WALLET_ADDRESS_v3R2))
                    || preferences.contains(stringPreferencesKey(WALLET_ADDRESS_v4R2))
        }
    }

    fun getCurrentWalletFlow(): Flow<WalletData?> {
        return dataStore.data.map { preferences ->
            val version = preferences.get(stringPreferencesKey(CURRENT_WALLET_VERSION))
                ?.let { json.decodeFromString<WalletVersion>(it) } ?: WalletVersion.V3R2
            preferences[version.toStringPreferencesKey()]?.let { json.decodeFromString(it) }
        }
    }

    fun getWalletSettingsModelFlow(): Flow<List<WalletSettingsModel>> {
        return dataStore.data.map { preferences ->
            val storedCurrentWalletVersion =
                preferences[stringPreferencesKey(CURRENT_WALLET_VERSION)]
            val currentWalletVersion =
                if (storedCurrentWalletVersion.isNullOrEmpty().not()) json.decodeFromString(
                    storedCurrentWalletVersion.orEmpty()
                ) else WalletVersion.V3R2
            WalletVersion.values().mapNotNull { version ->
                val storedWallet = preferences[version.toStringPreferencesKey()]
                if (storedWallet != null) {
                    json.decodeFromString<WalletData>(storedWallet)
                } else {
                    null
                }
            }.map { data ->
                WalletSettingsModel(
                    address = data.address,
                    version = data.walletVersion,
                    current = currentWalletVersion == data.walletVersion
                )
            }
        }
    }

    suspend fun getCurrentWallet(): WalletData? {
        return getWalletWithVersion(getCurrentWalletVersion())
    }

    suspend fun getWalletMaxAvailableVersion(): WalletData? {
        val walletV4 = getWalletWithVersion(WalletVersion.V4R2)
        if (walletV4 != null) {
            return walletV4
        }
        val walletV3R2 = getWalletWithVersion(WalletVersion.V3R2)
        if (walletV3R2 != null) {
            return walletV3R2
        }
        val walletV3R1 = getWalletWithVersion(WalletVersion.V3R1)
        if (walletV3R1 != null) {
            return walletV3R1
        }
        return null
    }

    suspend fun getWalletWithVersion(version: WalletVersion): WalletData? {
        return dataStore.data.firstOrNull()?.get(version.toStringPreferencesKey())
            ?.let { json.decodeFromString(it) }
    }

    suspend fun getCurrentWalletVersion(): WalletVersion {
        val versionFromStorage =
            dataStore.data.firstOrNull()?.get(stringPreferencesKey(CURRENT_WALLET_VERSION))
        return if (versionFromStorage != null) {
            json.decodeFromString(versionFromStorage)
        } else {
            WalletVersion.V3R2
        }
    }

    suspend fun getWalletVersionByAddress(address: String): WalletVersion {
        return dataStore.data.firstOrNull()?.let { preferences ->
            WalletVersion.values().mapNotNull { version ->
                val storedWallet = preferences[version.toStringPreferencesKey()]
                if (storedWallet != null) {
                    json.decodeFromString<WalletData>(storedWallet)
                } else {
                    null
                }
            }.find { it.address == address }?.walletVersion
        } ?: WalletVersion.V3R2
    }

    suspend fun setCurrentWalletVersion(version: WalletVersion) {
        dataStore.edit {
            it[stringPreferencesKey(CURRENT_WALLET_VERSION)] = json.encodeToString(version)
        }
    }


    suspend fun saveWalletData(data: WalletData) {
        dataStore.edit {
            it[data.walletVersion.toStringPreferencesKey()] = json.encodeToString(data)
        }
    }


    suspend fun getPasscodeEncryptedData(): PasscodeData? {
        return dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey(APPLICATION_PASSCODE))
            ?.let { json.decodeFromString(it) }
    }

    suspend fun updatePasscodeData(
        tonPublicKey: String? = null,
        encryptedPinData: String? = null,
        encryptedBiometricData: String? = null,
        passcodeSalt: ByteArray? = null,
        passcodeType: Int? = null
    ) {
        dataStore.edit {
            val current = it[stringPreferencesKey(APPLICATION_PASSCODE)]?.let {
                json.decodeFromString<PasscodeData>(it)
            }
            val newData = if (current == null) {
                PasscodeData(
                    tonPublicKey = tonPublicKey,
                    encryptedPinData = encryptedPinData,
                    encryptedBiometricData = encryptedBiometricData,
                    passcodeSalt = passcodeSalt,
                    passcodeType = passcodeType
                )
            } else {
                current.copy(
                    tonPublicKey = tonPublicKey ?: current.tonPublicKey,
                    encryptedPinData = encryptedPinData ?: current.encryptedPinData,
                    encryptedBiometricData = encryptedBiometricData
                        ?: current.encryptedBiometricData,
                    passcodeSalt = passcodeSalt ?: current.passcodeSalt,
                    passcodeType = passcodeType ?: current.passcodeType
                )
            }
            it[stringPreferencesKey(APPLICATION_PASSCODE)] = json.encodeToString(newData)
        }
    }

    fun getTransactionsFlow(address: String): Flow<List<RawTransaction>> {
        return dataStore.data.map<Preferences, List<RawTransaction>> {
            val walletVersion = getWalletVersionByAddress(address)
            val transactionsInStorage = it[stringPreferencesKey("${walletVersion.name}_transactions")]
            transactionsInStorage?.let { json.decodeFromString(transactionsInStorage) }
                ?: emptyList()
        }.map { items -> items.filterNot { it.isEmpty() } }
    }

    suspend fun getCurrentTransactions(): List<RawTransaction> {
        val preferences = dataStore.data.first()
        val walletVersion = getCurrentWalletVersion()
        val transactionsInStorage =
            preferences[stringPreferencesKey("${walletVersion.name}_transactions")]
        return transactionsInStorage?.let {
            json.decodeFromString<List<RawTransaction>>(
                transactionsInStorage
            )
        } ?: emptyList()
    }

    suspend fun getTransactionWithId(transactionId: Long): RawTransaction? {
        val transactions = getCurrentTransactions()
        return transactions.find { it.transactionId.lt == transactionId }
    }

    suspend fun saveTransactions(transactions: List<RawTransaction>, version: WalletVersion) {
        dataStore.edit {
            val transactionsResult = (transactions + getCurrentTransactions()).distinctBy { transaction ->
                    transaction.transactionId
                }
            it[stringPreferencesKey("${version.name}_transactions")] =
                json.encodeToString(transactionsResult)
        }
    }

    suspend fun setDecryptedMessageToTransaction(
        transaction: RawTransaction,
        msgData: drinkless.org.ton.TonApi.MsgDataDecryptedText
    ) {
        val transactions = getCurrentTransactions().toMutableList()
        val itemIndex = transactions.indexOfFirst { it.transactionId == transaction.transactionId }
        val item = transactions.removeAt(itemIndex)
        val resultItem = if (transaction.isIncome()) {
            item.copy(inMsg = item.inMsg?.copy(msgData = msgData.toStorage()))
        } else {
            item.copy(
                outMsgs = item.outMsgs.toMutableList().apply {
                    val msgItem = firstOrNull()
                    if (msgItem != null) {
                        removeAt(0)
                        add(0, msgItem.copy(msgData = msgData.toStorage()))
                    }
                }
            )
        }
        transactions.add(itemIndex, resultItem)
    }


    suspend fun saveNotificationsState(enabled: Boolean) {
        dataStore.edit {
            it[booleanPreferencesKey(SETTINGS_NOTIFICATIONS_KEY)] = enabled
        }
    }

    fun getNotificationsStateFlow(): Flow<Boolean> {
        return dataStore.data.map {
            it[booleanPreferencesKey(SETTINGS_NOTIFICATIONS_KEY)] ?: true
        }
    }


    suspend fun saveBiometricAuthState(enabled: Boolean) {
        dataStore.edit {
            it[booleanPreferencesKey(SETTINGS_BIOMETRIC_KEY)] = enabled
        }
    }

    fun getBiometricAuthFlow(): Flow<Boolean> {
        return dataStore.data.map {
            it[booleanPreferencesKey(SETTINGS_BIOMETRIC_KEY)] ?: true
        }
    }


    suspend fun savePrimaryCurrency(primaryCurrency: PrimaryCurrency) {
        dataStore.edit {
            it[stringPreferencesKey(SETTINGS_PRIMARY_CURRENCY_KEY)] =
                json.encodeToString(primaryCurrency)
        }
    }

    suspend fun getPrimaryCurrency(): PrimaryCurrency {
        return dataStore.data.firstOrNull()
            ?.get(stringPreferencesKey(SETTINGS_PRIMARY_CURRENCY_KEY))
            ?.let {
                json.decodeFromString(it)
            } ?: PrimaryCurrency.USD
    }

    fun getPrimaryCurrencyFlow(): Flow<PrimaryCurrency> {
        return dataStore.data.map {
            val currency = it[stringPreferencesKey(SETTINGS_PRIMARY_CURRENCY_KEY)]
            if (currency != null) {
                json.decodeFromString(currency)
            } else {
                PrimaryCurrency.USD
            }
        }
    }


    suspend fun saveTonConnectManifest1(tonConnectConnectionData: TonConnectConnectionData) {
        dataStore.edit { preferences ->
            val current = preferences[stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY)]
            if (current == null) {
                preferences[stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY)] =
                    setOf(json.encodeToString(tonConnectConnectionData))
            } else {
                val updated = current.map { jsonData ->
                    json.decodeFromString<TonConnectConnectionData>(jsonData)
                }.filter {
                    it.manifest != tonConnectConnectionData.manifest
                }
                    .toMutableList()
                    .apply { add(tonConnectConnectionData) }
                    .map { json.encodeToString(it) }
                    .toSet()
                preferences[stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY)] = updated
            }
        }
    }

    suspend fun getTonConnectManifests1(): List<TonConnectConnectionData> {
        return dataStore.data
            .firstOrNull()
            ?.get(stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY))
            ?.map { manifests ->
                json.decodeFromString(manifests)
            } ?: emptyList()
    }

    fun getTonConnectManifests1Flow(): Flow<List<TonConnectConnectionData>> {
        return dataStore.data.map {
            it[stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY)]?.map { manifest ->
                Json.decodeFromString(manifest)
            } ?: emptyList()
        }
    }

    suspend fun deleteTonConnectConnection(data: TonConnectConnectionData) {
        dataStore.edit { preferences ->
            preferences.remove(longPreferencesKey(data.id))
            val currentList = preferences.get(stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY))
                ?.map { manifests ->
                    json.decodeFromString<TonConnectConnectionData>(manifests)
                } ?: emptyList()
            currentList.toMutableList().apply {
                removeAll {
                    it.id == data.id
                }
            }.map { item ->
                json.encodeToString(item)
            }.let { updatedSet ->
                preferences[stringSetPreferencesKey(TON_CONNECT_MANIFESTS_KEY)] = updatedSet.toSet()
            }
        }
    }

    suspend fun saveTonConnectLastEventId(clientId: String, lastEventId: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(clientId)] = lastEventId
        }
    }

    suspend fun getTonConnectLastEventId(clientId: String): Long {
       return dataStore.data.firstOrNull()?.get(longPreferencesKey(clientId)) ?: -1
    }


    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private fun WalletVersion.toStringPreferencesKey(): Preferences.Key<String> {
        return when (this) {
            WalletVersion.V3R1 -> stringPreferencesKey(WALLET_ADDRESS_v3R1)
            WalletVersion.V3R2 -> stringPreferencesKey(WALLET_ADDRESS_v3R2)
            WalletVersion.V4R2 -> stringPreferencesKey(WALLET_ADDRESS_v4R2)
        }
    }

    companion object {
        const val WALLET_ADDRESS_v3R1 = "WALLET_ADDRESS_v3R1"
        const val WALLET_ADDRESS_v3R2 = "WALLET_ADDRESS_v3R2"
        const val WALLET_ADDRESS_v4R2 = "WALLET_ADDRESS_v4R2"
        const val CURRENT_WALLET_VERSION = "CURRENT_WALLET_VERSION"

        const val APPLICATION_PASSCODE = "APPLICATION_PASSCODE"

        const val SETTINGS_NOTIFICATIONS_KEY = "SETTINGS_NOTIFICATIONS_KEY"
        const val SETTINGS_BIOMETRIC_KEY = "SETTINGS_BIOMETRIC_KEY"
        const val SETTINGS_PRIMARY_CURRENCY_KEY = "SETTINGS_PRIMARY_CURRENCY_KEY"

        const val TON_CONNECT_MANIFESTS_KEY = "TON_CONNECT_MANIFESTS_KEY"
    }
}

enum class WalletVersion {
    V3R1,
    V3R2,
    V4R2;

    override fun toString(): String {
        return super.toString().replaceFirstChar { it.lowercase(Locale.getDefault()) }
    }
}

enum class PrimaryCurrency {
    USD,
    EUR,
    RUB,
    AED,
    CNY
}

fun PrimaryCurrency.getCurrencySymbol(): String {
    return when (this) {
        PrimaryCurrency.USD -> "$"
        PrimaryCurrency.EUR -> "€"
        PrimaryCurrency.RUB -> "₽"
        PrimaryCurrency.AED -> "د.إ"
        PrimaryCurrency.CNY -> "¥"
    }
}


