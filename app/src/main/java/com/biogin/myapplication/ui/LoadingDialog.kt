package com.biogin.myapplication.ui

import android.app.Activity
import android.app.AlertDialog
import com.biogin.myapplication.R

class LoadingDialog (myActivity : Activity) {
    var activity : Activity = myActivity
    lateinit var dialog : AlertDialog

    fun startLoadingDialog() {
        var builder = AlertDialog.Builder(activity)
        var inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}