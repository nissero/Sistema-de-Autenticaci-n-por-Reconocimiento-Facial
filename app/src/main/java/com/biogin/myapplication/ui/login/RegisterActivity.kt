package com.biogin.myapplication.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.databinding.ActivityRegisterBinding

import com.biogin.myapplication.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var modulesAssociatedWithInstitutes : HashMap<String, ArrayList<String>>
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRegisterBinding

    init {
        modulesAssociatedWithInstitutes = HashMap<String, ArrayList<String>>()
        modulesAssociatedWithInstitutes.set("IDEI", arrayListOf("Módulo 1", "Módulo 3", "Módulo 4","Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
        modulesAssociatedWithInstitutes.set("ICO", arrayListOf("Módulo 1", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
        modulesAssociatedWithInstitutes.set("IDH", arrayListOf("Módulo 1", "Módulo 2","Módulo 3", "Módulo 5", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
        modulesAssociatedWithInstitutes.set("ICI", arrayListOf("Módulo 1", "Módulo 2", "Módulo 3", "Módulo 6", "Módulo 7", "Módulo 9", "Auditorio", "Biblioteca"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var categoriesWithNoInstitute = resources.getStringArray(R.array.user_categories_with_no_institute)
        var user_categories = resources.getStringArray(R.array.user_categories)
        val categories_spinner = findViewById<Spinner>(R.id.register_categories_spinner)
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, user_categories)
        categories_spinner.adapter = adapter

        categories_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinner = findViewById<Spinner>(R.id.register_categories_spinner)
                var categorySelected  = spinner.selectedItem.toString()

                if (categoriesWithNoInstitute.contains(categorySelected)) {
                    disableCheckboxes()
                } else {
                    enableCheckboxes()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        val name = binding.registerName
        val surname = binding.registerSurname
        val dni = binding.registerDni
        val email = binding.registerEmail
        val checkboxes = arrayListOf(binding.checkboxICI, binding.checkboxICO, binding.checkboxIDEI, binding.checkboxIDH)
        val spinner = binding.registerCategoriesSpinner
        val register = binding.registerContinueButton

        register?.setOnClickListener {
            var institutesSelected = getInstitutesSelected(checkboxes)
            loginViewModel.register(name?.text.toString(), surname?.text.toString(), dni?.text.toString(), email?.text.toString(),spinner?.selectedItem.toString(),getAllowedAreas(institutesSelected), institutesSelected)

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

    private fun enableCheckboxes() {
        binding.checkboxIDEI?.isEnabled = true
        binding.checkboxICO?.isEnabled = true
        binding.checkboxIDH?.isEnabled = true
        binding.checkboxICI?.isEnabled = true

        binding.checkboxIDEI?.visibility = View.VISIBLE
        binding.checkboxICO?.visibility = View.VISIBLE
        binding.checkboxIDH?.visibility = View.VISIBLE
        binding.checkboxICI?.visibility = View.VISIBLE
    }

    private fun disableCheckboxes() {
        binding.checkboxIDEI?.isEnabled = false
        binding.checkboxICO?.isEnabled = false
        binding.checkboxIDH?.isEnabled = false
        binding.checkboxICI?.isEnabled = false

        binding.checkboxIDEI?.isChecked = false
        binding.checkboxICO?.isChecked = false
        binding.checkboxIDH?.isChecked = false
        binding.checkboxICI?.isChecked = false

        binding.checkboxIDEI?.visibility = View.INVISIBLE
        binding.checkboxICO?.visibility = View.INVISIBLE
        binding.checkboxIDH?.visibility = View.INVISIBLE
        binding.checkboxICI?.visibility = View.INVISIBLE
    }
    private fun getAllowedAreas(institutes : ArrayList<String>) : MutableSet<String> {
        val allowedAreas = mutableSetOf<String>()
        for (institute in institutes) {
            allowedAreas.addAll(modulesAssociatedWithInstitutes.get(institute)!!)
        }
        return allowedAreas
    }
    private fun getInstitutesSelected(checkboxes: ArrayList<CheckBox?>): ArrayList<String> {
        var institutesSelected = ArrayList<String>()
        println(checkboxes)
        for (checkbox in checkboxes) {
            println(checkbox?.isChecked)
            if(checkbox?.isChecked == true) {
                institutesSelected.add(checkbox.text.toString())
                println(institutesSelected)
                println(checkbox.text.toString())
            }
        }

        return institutesSelected
    }
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