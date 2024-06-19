package com.biogin.myapplication.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.EditText
import java.util.Calendar

class DatePickerDialog {
    fun showDatePickerDialog(targetEditText: EditText, secondaryEditText: EditText?, minDate: Long, context: Context, enableButton: () -> Unit){
        val calendar = Calendar.getInstance()
        val actualYear = calendar.get(Calendar.YEAR)
        val actualMonth = calendar.get(Calendar.MONTH)
        val actualDay = calendar.get(Calendar.DAY_OF_MONTH)

        var selectedDate: String

        val datePickerDialog = DatePickerDialog(context,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth) // Add leading zero for day
                val formattedMonth = String.format("%02d", monthOfYear + 1) // Add leading zero for month
                selectedDate = "$formattedDay/$formattedMonth/$year"
                targetEditText.setText(selectedDate)

                targetEditText.tag = "editted"

                enableButton()
            },
            actualYear,
            actualMonth,
            actualDay
        )

        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    fun showDatePickerDialog(targetEditText: EditText, secondaryEditText: EditText?, context: Context, enableButton: () -> Unit){
        val calendar = Calendar.getInstance()
        val actualYear = calendar.get(Calendar.YEAR)
        val actualMonth = calendar.get(Calendar.MONTH)
        val actualDay = calendar.get(Calendar.DAY_OF_MONTH)

        var selectedDate: String

        val datePickerDialog = DatePickerDialog(context,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth) // Add leading zero for day
                val formattedMonth = String.format("%02d", monthOfYear + 1) // Add leading zero for month
                selectedDate = "$formattedDay/$formattedMonth/$year"
                targetEditText.setText(selectedDate)

                targetEditText.tag = "editted"

                enableButton()
            },
            actualYear,
            actualMonth,
            actualDay
        )

        datePickerDialog.show()
    }
    fun formatDate(date: String): String {
        val splitText = date.split("/")

        return "${splitText[2]}/${splitText[1]}/${splitText[0]}"
    }


}