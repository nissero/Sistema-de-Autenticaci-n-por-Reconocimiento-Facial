package com.biogin.myapplication.ui.login

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(view: View) : ViewModelProvider.Factory {
    private val root = view

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(
                    dataSource = LoginDataSource(root)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}