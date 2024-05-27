package com.biogin.myapplication.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.data.Result

import com.biogin.myapplication.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
//    fun register(
//        name: String,
//        surname: String,
//        dni: String,
//        email: String,
//        category: String,
//        areasAllowed: MutableSet<String>,
//        institutesSelected: ArrayList<String>
//    ) {
//        // can be launched in a separate asynchronous job
//        val result = loginRepository.register(name, surname, dni, email, category, areasAllowed, institutesSelected)
//
//        if (result is Result.Success) {
//            _loginResult.value =
//                LoginResult(success = LoggedInUserView(displayName = result.data.name))
//        } else {
//            _loginResult.value = LoginResult(error = R.string.login_failed)
//        }
//    }

    fun loginDataChanged(name: String, password: String, confirmPassword: String) {
        if (!isUserNameValid(name)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (password != confirmPassword && confirmPassword != ""){
            _loginForm.value = LoginFormState(passwordError = R.string.confirm_password_not_equal_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.isNotBlank()) {
            username.length > 2 // Example validation logic
        } else {
            true // Valid if blank
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
//        return password.length > 5
        return true
    }
}