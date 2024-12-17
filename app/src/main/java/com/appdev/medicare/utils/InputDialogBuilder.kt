package com.appdev.medicare.utils

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.appdev.medicare.R

fun buildInputAlertDialog(
    context: Context,
    title: String,
    message: String,
    defaultText: String = "",
    onPositiveButtonClick: (inputText: String, dialog: DialogInterface) -> Unit
): AlertDialog {
    val dialogView = EditText(context)

    dialogView.setText(defaultText)

    val dialog = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setView(dialogView)
        .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
            val input = dialogView.text.toString().trim()
            if (!TextUtils.isEmpty(input)) {
                onPositiveButtonClick(input, dialog)
            }
        }
        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        .create()

    return dialog
}