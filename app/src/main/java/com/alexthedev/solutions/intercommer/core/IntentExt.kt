package com.alexthedev.solutions.intercommer.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.alexthedev.solutions.intercommer.R


fun Context.openAppSettings(shouldOpenAsNewTask: Boolean = true) {
    try {
        startActivity(prepareIntentForDetailSettings(packageName, shouldOpenAsNewTask))
    } catch (e: ActivityNotFoundException) {
        notifyAboutNoSettings()
    }
}

private fun prepareIntentForDetailSettings(
    packageName: String,
    shouldOpenAsNewTask: Boolean
): Intent {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    if (shouldOpenAsNewTask) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    return intent
}

private fun Context.notifyAboutNoSettings() {
    createAlertDialogWithMessage(
        this,
        R.string.err_dial_title,
        R.string.no_settings,
        R.string.err_dial_ok
    ).show()
}


fun createIntentForSharingImage(uri: Uri): Intent {
    return Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = "image/png"
    }
}