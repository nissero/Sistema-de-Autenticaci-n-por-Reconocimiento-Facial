package com.biogin.myapplication.data.userSession

import android.util.Log

object MasterUserDataSession {
    private var userDni : String = ""
    private var userCategory : String = ""
    fun setUserDataForSession(dni : String, category: String) {
        Log.e("SESSION DATA", "Registrado el usuario con dni $dni y categoria $category")
        userDni = dni
        userCategory = category
    }

    fun getDniUser(): String {
        return userDni
    }

    fun getCategoryUser() : String {
        return userCategory
    }

    fun clearUserData() {
        userDni  = ""
        userCategory = ""
    }
}