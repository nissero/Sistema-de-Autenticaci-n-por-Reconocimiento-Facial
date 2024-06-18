package com.biogin.myapplication.data

import com.biogin.myapplication.data.model.LoggedInUser

class LoginRepository(private val dataSource: LoginDataSource) {
    suspend fun getUser(dni: String) : Result<LoggedInUser>{
        if (dni == "") {
            return Result.Error(Exception("Se ingreso un dni vacío, ingrese uno nuevamente"))
        }
        return dataSource.getUserFromFirebase(dni)
    }

}