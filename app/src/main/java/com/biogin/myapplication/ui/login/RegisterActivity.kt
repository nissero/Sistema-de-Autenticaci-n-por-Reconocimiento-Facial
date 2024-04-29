package com.biogin.myapplication.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.databinding.ActivityRegisterBinding

import com.biogin.myapplication.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = binding.name
        val surname = binding.surname
        val dni = binding.dni
        val email = binding.email
        val area = binding.area
        val password = binding.password
        val confirmPassword = binding.confirmPassword
        val register = binding.registerButton

        register?.setOnClickListener {
            loginViewModel.register(name?.text.toString(), surname?.text.toString(), dni?.text.toString(), email?.text.toString(),area?.text.toString(), password.text.toString())

            val intent = Intent(this@RegisterActivity, PhotoRegisterActivity::class.java)
            intent.putExtra("name", name?.text.toString())
            intent.putExtra("surname", surname?.text.toString())
            intent.putExtra("dni", dni?.text.toString())
            intent.putExtra("email", email?.text.toString())
            intent.putExtra("area", area?.text.toString())
            intent.putExtra("password", password.text.toString())
            startActivity(intent)
        }

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            if (register != null) {
                register.isEnabled = loginState.isDataValid
            }

            if (loginState.usernameError != null) {
                if (name != null) {
                    name.error = getString(loginState.usernameError)
                }
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(RESULT_OK)

            //Complete and destroy login activity once successful
//            finish()
        })

        name?.afterTextChanged {
            loginViewModel.loginDataChanged(
                name.text.toString(),
                password.text.toString(),
                confirmPassword?.text.toString()
            )
        }

        confirmPassword?.apply {
            afterTextChanged {
                if (name != null) {
                    loginViewModel.loginDataChanged(
                        name.text.toString(),
                        password.text.toString(),
                        confirmPassword.text.toString()
                    )
                }
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (name != null) {
                            loginViewModel.login(
                                name.text.toString(),
                                password.text.toString()
                            )
                        }
                }
                false
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}