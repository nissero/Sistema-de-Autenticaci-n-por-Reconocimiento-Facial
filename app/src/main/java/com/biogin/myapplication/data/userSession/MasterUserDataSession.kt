package com.biogin.myapplication.data.userSession

object MasterUserDataSession {
    private var userDni : String = ""
    private var userCategory : String = ""
    public fun setUserDataForSession(dni : String, category: String) {
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