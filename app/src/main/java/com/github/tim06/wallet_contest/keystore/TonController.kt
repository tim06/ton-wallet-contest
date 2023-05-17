/*
 * This is the source code of Wallet for Android v. 1.0.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright Nikolai Kudashov, 2019-2020.
 */
package com.github.tim06.wallet_contest.keystore

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.github.tim06.wallet_contest.BuildConfig
import drinkless.org.ton.TonApi
import drinkless.org.ton.TonApi.InputKey
import org.telegram.messenger.Utilities
import java.math.BigInteger
import java.security.*
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.security.auth.x500.X500Principal

class TonController(private val context: Context, private val keyName: String) {

    private var creatingDataForLaterEncrypt: ByteArray? = null
    private var memInputKey: InputKey? = null

    private lateinit var keyStore: KeyStore
    private lateinit var keyGenerator: KeyPairGenerator
    private lateinit var cipher: Cipher

    private val keyProtectionType: Int
        get() {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    val key = keyStore.getKey(keyName, null)
                    val factory = KeyFactory.getInstance(key.algorithm, "AndroidKeyStore")
                    val keyInfo = factory.getKeySpec(key, KeyInfo::class.java)
                    if (keyInfo.isUserAuthenticationRequired) {
                        return if (keyInfo.userAuthenticationValidityDurationSeconds > 0) {
                            KEY_PROTECTION_TYPE_LOCKSCREEN
                        } else {
                            KEY_PROTECTION_TYPE_BIOMETRIC
                        }
                    }
                } catch (ignore: Exception) {
                    log("keyProtectionType $ignore")
                }
            }
            return KEY_PROTECTION_TYPE_NONE
        }

    val cipherForDecrypt: Cipher?
        get() {
            try {
                val key = keyStore.getKey(
                    keyName, null
                ) as PrivateKey
                val spec = OAEPParameterSpec(
                    "SHA-1",
                    "MGF1",
                    MGF1ParameterSpec.SHA1,
                    PSource.PSpecified.DEFAULT
                )
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
                return cipher
            } catch (e: Exception) {
                log("cipherForDecrypt ${e.message}")
            }
            return null
        }

    init {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (Build.VERSION.SDK_INT >= 23) {
                keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
                cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
            } else {
                keyGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            }
        } catch (e: Exception) {
            log(e.message)
        }
    }

    private fun createKeyPair(useBiometric: Boolean, forPasscode: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            for (a in 0..1) {
                try {
                    val builder = KeyGenParameterSpec.Builder(
                        keyName, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
                    )
                        .setDigests(KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA256)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .setKeySize(2048)
                    if (a == 0 && Build.VERSION.SDK_INT >= 28 && context.packageManager.hasSystemFeature(
                            PackageManager.FEATURE_STRONGBOX_KEYSTORE
                        )
                    ) {
                        builder.setIsStrongBoxBacked(true)
                    }
                    if (!forPasscode) {
                        val keyguardManager =
                            context.applicationContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                        if (keyguardManager.isDeviceSecure) {
                            builder.setUserAuthenticationRequired(true)
                            if (!useBiometric) {
                                builder.setUserAuthenticationValidityDurationSeconds(15)
                            }
                            if (Build.VERSION.SDK_INT >= 24) {
                                builder.setInvalidatedByBiometricEnrollment(true)
                            }
                        }
                    }
                    keyGenerator.initialize(builder.build())
                    keyGenerator.generateKeyPair()
                    return true
                } catch (e: Throwable) {
                    log("createKeyPair() ${e.message}")
                }
            }
        } else {
            try {
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 30)
                val spec = KeyPairGeneratorSpec.Builder(context.applicationContext)
                    .setAlias(keyName)
                    .setSubject(X500Principal("CN=Telegram, O=Telegram C=UAE"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
                keyGenerator.initialize(spec)
                keyGenerator.generateKeyPair()
                return true
            } catch (ignore: Throwable) {
                log("createKeyPair() ${ignore.message}")
            }
        }
        return false
    }

    private fun initCipher(mode: Int): Int {
        try {
            when (mode) {
                Cipher.ENCRYPT_MODE -> {
                    val key = keyStore.getCertificate(
                        keyName
                    ).publicKey
                    val unrestricted = KeyFactory.getInstance(key.algorithm)
                        .generatePublic(X509EncodedKeySpec(key.encoded))
                    if (Build.VERSION.SDK_INT >= 23) {
                        val spec = OAEPParameterSpec(
                            "SHA-1",
                            "MGF1",
                            MGF1ParameterSpec.SHA1,
                            PSource.PSpecified.DEFAULT
                        )
                        cipher.init(mode, unrestricted, spec)
                    } else {
                        cipher.init(mode, unrestricted)
                    }
                }
                Cipher.DECRYPT_MODE -> {
                    val key = keyStore.getKey(
                        keyName, null
                    ) as PrivateKey
                    if (Build.VERSION.SDK_INT >= 23) {
                        val spec = OAEPParameterSpec(
                            "SHA-1",
                            "MGF1",
                            MGF1ParameterSpec.SHA1,
                            PSource.PSpecified.DEFAULT
                        )
                        cipher.init(mode, key, spec)
                    } else {
                        cipher.init(mode, key)
                    }
                }
                else -> return CIPHER_INIT_FAILED
            }
            return CIPHER_INIT_OK
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (e is KeyPermanentlyInvalidatedException) {
                    return CIPHER_INIT_KEY_INVALIDATED
                }
            } else if (e is InvalidKeyException) {
                try {
                    if (!keyStore.containsAlias(keyName)) {
                        return CIPHER_INIT_KEY_INVALIDATED
                    }
                } catch (ignore: Exception) {
                }
            }
            if (e is UnrecoverableKeyException) {
                return CIPHER_INIT_KEY_INVALIDATED
            }
            log("initCipher() ${e.message}")
        }
        return CIPHER_INIT_FAILED
    }

    private fun isKeyCreated(useBiometric: Boolean, forPasscode: Boolean): Boolean {
        try {
            return keyStore.containsAlias(keyName) || createKeyPair(useBiometric, forPasscode)
        } catch (e: Exception) {
            log("isKeyCreated() ${e.message}")
        }
        return false
    }

    fun encrypt(input: ByteArray?): String? {
        try {
            if (initCipher(Cipher.ENCRYPT_MODE) == CIPHER_INIT_OK) {
                val bytes = cipher.doFinal(input)
                return Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            log("encrypt() ${e.message}")
        }
        return null
    }

    fun decrypt(encodedString: String?, decryptCipher: Cipher?): ByteArray? {
        var decryptCipher1 = decryptCipher
        try {
            if (decryptCipher1 == null) {
                initCipher(Cipher.DECRYPT_MODE)
                decryptCipher1 = cipher
            }
            val bytes = Base64.decode(encodedString, Base64.NO_WRAP)
            return decryptCipher1.doFinal(bytes)
        } catch (e: Exception) {
            log("decrypt() ${e.message}")
        }
        return null
    }

    fun onFinishWalletCreate(
        password: ByteArray,
        key: TonApi.Key,
        callback: ((String?) -> Unit)? = null
    ) {
        memInputKey = TonApi.InputKeyRegular(TonApi.Key(key.publicKey, key.secret), password)
        var len = 1 + 2 + password.size + key.secret.size
        var padding = len % 16
        if (padding != 0) {
            padding = 16 - padding
            len += padding
        }
        val dataToEncrypt = ByteArray(len)
        dataToEncrypt[0] = padding.toByte()
        dataToEncrypt[1] = 'o'.code.toByte()
        dataToEncrypt[2] = 'k'.code.toByte()
        System.arraycopy(password, 0, dataToEncrypt, 3, password.size)
        System.arraycopy(key.secret, 0, dataToEncrypt, 3 + password.size, key.secret.size)
        if (padding != 0) {
            val pad = ByteArray(padding)
            Utilities.random.nextBytes(pad)
            System.arraycopy(pad, 0, dataToEncrypt, 3 + password.size + key.secret.size, pad.size)
        }
        if (keyProtectionType != KEY_PROTECTION_TYPE_NONE) {
            val creatingEncryptedData = encrypt(dataToEncrypt)
            Arrays.fill(dataToEncrypt, 0.toByte())
            callback?.invoke(creatingEncryptedData!!)
        } else {
            creatingDataForLaterEncrypt = dataToEncrypt
            callback?.invoke(null)
        }
    }

    fun isKeyStoreInvalidated(callback: (Boolean) -> Unit) {
        val invalidated = initCipher(Cipher.DECRYPT_MODE) == CIPHER_INIT_KEY_INVALIDATED
        callback.invoke(invalidated)
    }

    fun createWallet(useBiometric: Boolean, forPasscode: Boolean) {
        if (::keyStore.isInitialized.not()) {
            return
        }
        cleanup()
        if (!isKeyCreated(useBiometric, forPasscode)) {
            return
        }
    }

    fun setUserPasscode(passcode: String, callback: (String, ByteArray) -> Unit) {
        val creatingPasscodeSalt = ByteArray(32)
        Utilities.random.nextBytes(creatingPasscodeSalt)
        val hash = Utilities.computePBKDF2(passcode.toByteArray(), creatingPasscodeSalt)
        val key = ByteArray(32)
        val iv = ByteArray(32)
        System.arraycopy(hash, 0, key, 0, key.size)
        System.arraycopy(hash, key.size, iv, 0, iv.size)
        Utilities.aesIgeEncryptionByteArray(
            creatingDataForLaterEncrypt,
            key,
            iv,
            true,
            false,
            0,
            creatingDataForLaterEncrypt!!.size
        )
        val creatingEncryptedData = encrypt(creatingDataForLaterEncrypt)
        callback.invoke(creatingEncryptedData!!, creatingPasscodeSalt)
    }

    fun finishSettingUserPasscode() {
        if (creatingDataForLaterEncrypt != null) {
            Arrays.fill(creatingDataForLaterEncrypt, 0.toByte())
            creatingDataForLaterEncrypt = null
        }
    }

    val isWaitingForUserPasscode: Boolean
        get() = creatingDataForLaterEncrypt != null

    fun isEncryptedTransaction(transaction: TonApi.RawTransaction): Boolean {
        if (transaction.inMsg != null) {
            if (transaction.inMsg.msgData is TonApi.MsgDataEncryptedText) {
                return true
            }
        }
        if (transaction.outMsgs != null && transaction.outMsgs.isNotEmpty()) {
            for (a in transaction.outMsgs.indices) {
                if (transaction.outMsgs[a].msgData is TonApi.MsgDataEncryptedText) {
                    return true
                }
            }
        }
        return false
    }

    fun hasDecryptKey(): Boolean {
        return memInputKey != null
    }

    fun decryptTonData(
        passcode: String?,
        cipherForDecrypt: Cipher?,
        onPasscodeOk: Runnable?,
        forPasscodeChange: Boolean,
        encryptedData: String?,
        passcodeSalt: ByteArray?,
        passcodeType: Int,
        tonPublicKey: String?
    ): InputKey? {
        val decrypted = decrypt(encryptedData, cipherForDecrypt)
        if (decrypted == null || decrypted.size <= 3) {
            log("KEYSTORE_FAIL")
            return null
        }
        if (passcodeType != -1) {
            val hash = Utilities.computePBKDF2(passcode?.toByteArray(), passcodeSalt)
            val key = ByteArray(32)
            val iv = ByteArray(32)
            System.arraycopy(hash, 0, key, 0, key.size)
            System.arraycopy(hash, key.size, iv, 0, iv.size)
            Utilities.aesIgeEncryptionByteArray(decrypted, key, iv, false, false, 0, decrypted.size)
        }
        return if (decrypted[1] == 'o'.code.toByte() && decrypted[2] == 'k'.code.toByte()) {
            if (!TextUtils.isEmpty(passcode) && onPasscodeOk != null) {
                onPasscodeOk.run()
            }
            val padding = decrypted[0].toInt()
            val password = ByteArray(64)
            val secret = ByteArray(decrypted.size - 64 - padding - 3)
            System.arraycopy(decrypted, 3, password, 0, password.size)
            System.arraycopy(decrypted, 3 + password.size, secret, 0, secret.size)
            if (forPasscodeChange) {
                creatingDataForLaterEncrypt = decrypted
            }
            TonApi.InputKeyRegular(TonApi.Key(tonPublicKey, secret), password)
        } else {
            if (!TextUtils.isEmpty(passcode)) {
                log("PASSCODE_INVALID")
            } else {
                log("KEYSTORE_FAIL_DECRYPT")
            }
            null
        }
    }

    fun prepareForPasscodeChange(
        passcode: String,
        onFinishRunnable: Runnable?,
        encryptedData: String?,
        passcodeSalt: ByteArray?,
        passcodeType: Int,
        tonPublicKey: String?
    ) {
        val inputKey = decryptTonData(
            passcode,
            null,
            null,
            true,
            encryptedData,
            passcodeSalt,
            passcodeType,
            tonPublicKey
        ) ?: return
    }

    fun fillMemInputKey(
        passcode: String,
        cipherForDecrypt: Cipher?,
        onPasscodeOk: Runnable,
        encryptedData: String?,
        passcodeSalt: ByteArray?,
        passcodeType: Int,
        tonPublicKey: String?
    ) {
        memInputKey = decryptTonData(
            passcode,
            cipherForDecrypt,
            onPasscodeOk,
            false,
            encryptedData,
            passcodeSalt,
            passcodeType,
            tonPublicKey
        )
        if (memInputKey != null && TextUtils.isEmpty(passcode)) {
            onPasscodeOk.run()
        }
    }

    fun cleanup() {
        try {
            keyStore.deleteEntry(keyName)
        } catch (e: Exception) {
            log("cleanup $e")
        }
        creatingDataForLaterEncrypt = null
        memInputKey = null
    }

    private fun log(message: String?) {
        if (BuildConfig.DEBUG) {
            Log.e("TonController", message.orEmpty())
        }
    }

    companion object {
        private const val CIPHER_INIT_FAILED = 0
        private const val CIPHER_INIT_OK = 1
        private const val CIPHER_INIT_KEY_INVALIDATED = 2
        private const val KEY_PROTECTION_TYPE_NONE = 0
        private const val KEY_PROTECTION_TYPE_LOCKSCREEN = 1
        private const val KEY_PROTECTION_TYPE_BIOMETRIC = 2
    }
}