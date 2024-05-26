package com.biogin.myapplication.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface




class DialogUtil {
    fun showDialog(context: Context, text: String) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->

            })
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    fun showDialogWithFunctionOnClose(context: Context, text: String, function: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                function()
            })
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}