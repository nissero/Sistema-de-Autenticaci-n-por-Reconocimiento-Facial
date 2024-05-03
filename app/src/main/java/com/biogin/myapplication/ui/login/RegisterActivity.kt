package com.biogin.myapplication.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.databinding.ActivityRegisterBinding

import com.biogin.myapplication.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRegisterBinding

    init {
        val modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()
        modulesAssociatedWithInstitutes.set("IDEI", arrayListOf())
        modulesAssociatedWithInstitutes.set("ICO", arrayListOf())
        modulesAssociatedWithInstitutes.set("IDH", arrayListOf())
        modulesAssociatedWithInstitutes.set("ICI", arrayListOf())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var user_categories = resources.getStringArray(R.array.user_categories)
        val categories_spinner = findViewById<Spinner>(R.id.register_categories)
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, user_categories)

        categories_spinner.adapter = adapter
        val name = binding.registerName
        val surname = binding.registerSurname
        val dni = binding.registerDni
        val email = binding.registerEmail
        val checkboxes = arrayListOf(binding.checkboxICI, binding.checkboxICO, binding.checkboxIDEI, binding.checkboxIDH)
        val register = binding.registerContinueButton

        register?.setOnClickListener {
//            var institutesSelected = getInstitutesSelected(checkboxes)
            loginViewModel.register(name?.text.toString(), surname?.text.toString(), dni?.text.toString(), email?.text.toString(),checkboxes)

            val intent = Intent(this@RegisterActivity, PhotoRegisterActivity::class.java)
            intent.putExtra("name", name?.text.toString())
            intent.putExtra("surname", surname?.text.toString())
            intent.putExtra("dni", dni?.text.toString())
            intent.putExtra("email", email?.text.toString())

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

        })

//        loginViewModel.loginResult.observe(this, Observer {
//            val loginResult = it ?: return@Observer
//
//            if (loginResult.error != null) {
//                showLoginFailed(loginResult.error)
//            }
//            if (loginResult.success != null) {
//                updateUiWithUser(loginResult.success)
//            }
//            setResult(RESULT_OK)
//
//            //Complete and destroy login activity once successful
//            finish()
//        })

        name?.afterTextChanged {
            loginViewModel.loginDataChanged(
                name.text.toString(),
                "",
               ""
            )
        }
        }

//    private fun getInstitutesSelected(checkboxes: ArrayList<CheckBox?>): Any {
//        for (checkbox in checkboxes) {
//            if(checkbox?.isSelected == true) {
//
//            }
//        }
//    }
}

//    private fun updateUiWithUser(model: LoggedInUserView) {
//        val displayName = model.displayName
//        // TODO : initiate successful logged in experience
//        Toast.makeText(
//            applicationContext,
//            "$welcome $displayName",
//            Toast.LENGTH_LONG
//        ).show()
//    }

//    private fun showLoginFailed(@StringRes errorString: Int) {
//        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
//    }
//}

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