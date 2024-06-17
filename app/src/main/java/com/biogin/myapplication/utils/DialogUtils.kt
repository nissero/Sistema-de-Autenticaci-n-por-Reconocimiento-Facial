package com.biogin.myapplication.utils

import android.app.AlertDialog
import android.content.Context

class DialogUtils {
    fun showDialog(context: Context, text: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->

            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun showDialogWithFunctionOnClose(context: Context, text: String, function: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                function()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun showDialogWithTwoFunctionOnClose(context: Context, text: String, onYesFunction: () -> Unit,
                                         onNoFunction: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("Si") { _, _ ->
                onYesFunction()
            }
            .setNegativeButton("No") { _, _ ->
                onNoFunction()
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}