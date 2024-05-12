package com.biogin.myapplication.utils

import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import org.checkerframework.checker.regex.qual.Regex
import java.util.regex.Pattern

class FormValidations {
    fun validateName(textView: EditText) {
        val text = textView.text.toString().trim()
        if (text.isEmpty()) {
            textView.error = "El nombre es requerido"
        } else if (!hasOnlyLetters(text)) {
            textView.error = "Ingrese caracteres que sean letras"
        } else if (text.length > 15) {
            textView.error = "El nombre no debe exceder los 15 caracteres"
        }
    }

    fun validateSurname(textView: EditText) {
        val text = textView.text.toString().trim()
        if (text.isEmpty()) {
            textView.error = "El apellido es requerido"
        } else if (!hasOnlyLetters(text)) {
            textView.error = "Ingrese caracteres que sean letras"
        } else if (text.length > 15) {
            textView.error = "El apellido no debe exceder los 15 caracteres"
        }
    }

    fun validateDNI(textView: EditText) {
        val dniRegex = "^\\d{7,8}\$".toRegex()
        val text = textView.text.toString().trim()
        if (text.isEmpty()) {
            textView.error = "El DNI es requerido"
        }else if (!text.isDigitsOnly()) {
            textView.error = "El DNI debe ser numerico"
        } else if (!dniRegex.matches(text)) {
            textView.error = "El DNI debe tener 7 u 8 digitos"
        }
    }

    fun validateEmail(textView: EditText) {
        val text = textView.text.toString().trim()
        if (text.isEmpty()) {
            textView.error = "El email es requerido"
        }else if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
            textView.error = "Ingrese un email valido"
        }
    }


    private fun hasOnlyLetters(text : String) : Boolean {
        val alphanumericRegex = "^[A-Za-z]*$".toRegex()
        return alphanumericRegex.matches(text)
    }


}