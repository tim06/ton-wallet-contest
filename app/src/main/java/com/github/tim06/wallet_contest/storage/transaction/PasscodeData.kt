package com.github.tim06.wallet_contest.storage.transaction

@kotlinx.serialization.Serializable
data class PasscodeData(
    val tonPublicKey: String? = null,
    val encryptedPinData: String? = null,
    val encryptedBiometricData: String? = null,
    val passcodeSalt: ByteArray? = null,
    val passcodeType: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasscodeData

        if (tonPublicKey != other.tonPublicKey) return false
        if (encryptedPinData != other.encryptedPinData) return false
        if (encryptedBiometricData != other.encryptedBiometricData) return false
        if (passcodeSalt != null) {
            if (other.passcodeSalt == null) return false
            if (!passcodeSalt.contentEquals(other.passcodeSalt)) return false
        } else if (other.passcodeSalt != null) return false
        if (passcodeType != other.passcodeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tonPublicKey?.hashCode() ?: 0
        result = 31 * result + (encryptedPinData?.hashCode() ?: 0)
        result = 31 * result + (encryptedBiometricData?.hashCode() ?: 0)
        result = 31 * result + (passcodeSalt?.contentHashCode() ?: 0)
        result = 31 * result + (passcodeType ?: 0)
        return result
    }
}
