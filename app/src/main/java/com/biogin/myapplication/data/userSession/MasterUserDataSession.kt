package com.biogin.myapplication.data.userSession

import android.util.Log

object MasterUserDataSession {
    private var userDni : String = ""
    private var userCategory : String = ""
    public fun setUserDataForSession(dni : String, category: String) {
        Log.e("SESSION DATA", "Registrado el usuario con dni $dni y categoria $category")
        userDni = dni
        userCategory = category
    }

    public fun getDniUser(): String {
        return userDni
    }

    public fun getCategoryUser() : String {
        return userCategory
    }

    public fun clearUserData() {
        userDni  = ""
        userCategory = ""
    }
}