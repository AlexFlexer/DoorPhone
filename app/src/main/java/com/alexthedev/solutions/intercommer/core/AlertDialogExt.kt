package com.alexthedev.solutions.intercommer.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

fun createAlertDialogWithMessage(
    context: Context,
    title: String,
    buttonText: String,
    message: String,
    isCancellable: Boolean = true
): AlertDialog {
    return AlertDialog.Builder(context)
        .setCancelable(isCancellable)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(buttonText) { dialog, _ ->
            dialog.dismiss()
        }
        .create()
}

fun createAlertDialogWithMessage(
    context: Context,
    @StringRes title: Int,
    @StringRes buttonText: Int,
    @StringRes message: Int,
    isCancellable: Boolean = true
): AlertDialog {
    val resolvedMessage = context.resources.getString(message)
    val resolvedTitle = context.resources.getString(title)
    val resolvedBtnText = context.resources.getString(buttonText)
    return createAlertDialogWithMessage(
        context, resolvedTitle, resolvedBtnText, resolvedMessage, isCancellable
    )
}