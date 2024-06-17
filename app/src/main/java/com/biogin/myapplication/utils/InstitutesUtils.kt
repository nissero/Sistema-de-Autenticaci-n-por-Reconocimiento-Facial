package com.biogin.myapplication.utils

import android.widget.CheckBox

class InstitutesUtils {
    fun getInstitutesSelected(checkboxes: ArrayList<CheckBox>): ArrayList<String> {
        val institutesSelected = ArrayList<String>()

        for (checkbox in checkboxes) {
            println(checkbox.isChecked)
            if(checkbox.isChecked) {
                institutesSelected.add(checkbox.text.toString())
            }
        }

        return institutesSelected
    }


}