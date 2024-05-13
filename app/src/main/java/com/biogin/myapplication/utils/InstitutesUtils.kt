package com.biogin.myapplication.utils

import android.widget.CheckBox

class InstitutesUtils {
    public fun getInstitutesSelected(checkboxes: ArrayList<CheckBox>): ArrayList<String> {
        var institutesSelected = ArrayList<String>()

        for (checkbox in checkboxes) {
            println(checkbox?.isChecked)
            if(checkbox?.isChecked == true) {
                institutesSelected.add(checkbox.text.toString())
            }
        }

        return institutesSelected
    }


}