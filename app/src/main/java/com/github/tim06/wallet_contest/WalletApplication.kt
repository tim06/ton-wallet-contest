package com.github.tim06.wallet_contest

import android.app.Application
import org.libsodium.jni.NaCl
import org.telegram.NativeLoader

class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NativeLoader.initNativeLibs(this)
        NaCl.sodium()
    }
}