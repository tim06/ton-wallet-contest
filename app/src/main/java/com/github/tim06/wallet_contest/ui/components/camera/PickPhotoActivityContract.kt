package com.github.tim06.wallet_contest.ui.components.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickPhotoActivityContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}