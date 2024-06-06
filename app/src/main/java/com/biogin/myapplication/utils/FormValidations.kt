package com.biogin.myapplication.utils

import android.content.Context
import android.util.Patterns
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import com.biogin.myapplication.R

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
        } else if (!text.isDigitsOnly()) {
            textView.error = "El DNI debe ser numerico"
        } else if (!dniRegex.matches(text)) {
            textView.error = "El DNI debe tener 7 u 8 digitos"
        }
    }

    fun validateEmail(textView: EditText) {
        val text = textView.text.toString().trim()
        if (text.isEmpty()) {
            textView.error = "El email es requerido"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
            textView.error = "Ingrese un email valido"
        }
    }

    fun isCategoryValidWithInstitutesCheckboxes(
        context: Context,
        category: String,
        institutes: ArrayList<CheckBox>
    ): Boolean {
        val categoriesWithNoInstitute =
            context.resources.getStringArray(R.array.user_categories_with_no_institute)
        if (categoriesWithNoInstitute.contains(category)) {
            return true
        }
        return isAnyInstituteSelected(institutes)
    }

    fun isAnyInstituteSelected(institutesCheckboxes: ArrayList<CheckBox>): Boolean {
        var isAnyInstituteSelected = false
        for (checkbox in institutesCheckboxes) {
            isAnyInstituteSelected = isAnyInstituteSelected || checkbox.isChecked == true
        }

        return isAnyInstituteSelected
    }

    private fun hasOnlyLetters(text: String): Boolean {
        val alphanumericRegex = "^[A-Za-z]*$".toRegex()
        return alphanumericRegex.matches(text)
    }

    public fun isFormValid(
        context: Context,
        name: EditText,
        surname: EditText,
        dni: EditText,
        email: EditText,
        categorySelected: String,
        institutesCheckboxes: ArrayList<CheckBox>
    ): Boolean {
        checkAllTextValidations(name, surname, dni, email)

        val nameHasNoErrors = name.error == null
        val surnameHasNoErrors = surname.error == null
        val dniHasNoErrors = dni.error == null
        val emailHasNoErrors = email.error == null
        val institutesCheckboxesHasNoErrors =
            isCategoryValidWithInstitutesCheckboxes(context, categorySelected, institutesCheckboxes)

        return nameHasNoErrors && surnameHasNoErrors && dniHasNoErrors && emailHasNoErrors && institutesCheckboxesHasNoErrors
    }

    public fun checkAllTextValidations(
        name: EditText,
        surname: EditText,
        dni: EditText,
        email: EditText
    ) {
        validateName(name)
        validateSurname(surname)
        validateDNI(dni)
        validateEmail(email)
    }
}