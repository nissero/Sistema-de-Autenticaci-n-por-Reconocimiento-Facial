package com.biogin.myapplication.data

import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.utils.AllowedAreasUtils

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {
    private var allowedAreasUtils : AllowedAreasUtils = AllowedAreasUtils()
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

//    fun login(username: String, password: String): Result<LoggedInUser> {
//        // handle login
//        val result = dataSource.login(username, password)
//
//        if (result is Result.Success) {
//            setLoggedInUser(result.data)
//        }
//
//        return result
//    }

    fun register(
        name: String,
        surname: String,
        dni: String,
        email: String,
        category: String,
        areasAllowed: MutableSet<String>,
        institutesSelected: ArrayList<String>
    ): Result<LoggedInUser> {
        // handle login
        val result = dataSource.register(name, surname, dni, email, category, areasAllowed, institutesSelected)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }


//    fun modifyUser(
//        name: String,
//        surname: String,
//        dni: String,
//        email: String,
//        category: String,
//        institutesSelected: ArrayList<String>
//    ) : Result<Boolean>  {
//        if (dni.isEmpty()) {
//            return Result.Error(Exception("No se ingresó ningun dni válido, ingrese uno"))
//        }
//
//        dataSource.modifyUserFirebase(name, surname, dni, email, category, allowedAreasUtils.getAllowedAreas(institutesSelected), institutesSelected) {result ->
//
//        if (!successfulUpdate) {
//            return Result.Error(Exception("No se pudo actualizar el usuario"))
//        }
//
//        return Result.Success(successfulUpdate)
//    }

//    fun duplicateUser(
//        name: String,
//        surname: String,
//        dni: String,
//        email: String,
//        category: String,
//        areasAllowed: MutableSet<String>,
//        institutesSelected: ArrayList<String>
//    ) : Result<Boolean> {
//        if (dni.isEmpty()) {
//            return Result.Error(Exception("No se ingresó ningun dni válido, ingrese uno"))
//        }
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