package com.biogin.myapplication.data

import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.utils.AllowedAreasUtils

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {
    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

//    fun register(
//        name: String,
//        surname: String,
//        dni: String,
//        email: String,
//        category: String,
//        areasAllowed: MutableSet<String>,
//        institutesSelected: ArrayList<String>
//    ): Result<LoggedInUser> {
//        // handle login
//        val result = dataSource.register(name, surname, dni, email, category, areasAllowed, institutesSelected)
//
//        if (result is Result.Success) {
//            setLoggedInUser(result.data)
//        }
//
//        return result
//    }

    suspend fun getUser(dni: String) : Result<LoggedInUser>{
        if (dni == "") {
            return Result.Error(Exception("Se ingreso un dni vacío, ingrese uno nuevamente"))
        }
        return dataSource.getUserFromFirebase(dni)
    }



    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}