package com.biogin.myapplication.utils

import android.content.Context
import android.content.Intent
import com.biogin.myapplication.Popup

class PopUpUtils {
    fun showPopUp(context: Context, popupText : String, popupButtonText : String) {
        val intent = Intent(context, Popup::class.java)
        intent.putExtra("popup_text", popupText)
        intent.putExtra("text_button", popupButtonText)
        context.startActivity(intent)
    }
}