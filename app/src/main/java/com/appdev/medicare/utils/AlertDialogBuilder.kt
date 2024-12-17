package com.appdev.medicare.utils

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.appdev.medicare.R

fun buildAlertDialog(
    context: Context,
    title: String,
    message: String,
    showCancelButton: Boolean = false,
    onPositiveButtonClick: (dialog: DialogInterface) -> Unit = { dialog -> dialog.dismiss() }
): AlertDialog {
    val builder = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            onPositiveButtonClick(dialog)
        }

    if (showCancelButton) {
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }

    return builder.create()
}