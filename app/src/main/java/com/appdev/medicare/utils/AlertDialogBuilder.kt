package com.appdev.medicare.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.appdev.medicare.R

fun buildAlertDialog(context: Context, title: String, message: String): AlertDialog {
    val dialog = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()

    return dialog
}