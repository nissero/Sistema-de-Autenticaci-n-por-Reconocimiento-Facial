package com.biogin.myapplication.ui.login

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.databinding.ActivityRegisterBinding

import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.ui.LoadingDialog
import com.biogin.myapplication.utils.AllowedAreasUtils
import com.biogin.myapplication.utils.FormValidations
import com.biogin.myapplication.utils.InstitutesUtils
import com.google.firebase.firestore.FirebaseFirestoreException

class RegisterActivity : AppCompatActivity() {
    private lateinit var allowedAreasUtils : AllowedAreasUtils
    private lateinit var institutesUtils : InstitutesUtils
    private lateinit var loginViewModel: LoginViewModel
    private  var dataSource = LoginDataSource()
    private lateinit var binding: ActivityRegisterBinding
    private var validations  = FormValidations()
    private var loadingDialog = LoadingDialog(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)
        allowedAreasUtils = AllowedAreasUtils()
        institutesUtils = InstitutesUtils()
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
                checkContinueButtonActivation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        val name = binding.registerName
        val surname = binding.registerSurname
        val dni = binding.registerDni
        val email = binding.registerEmail
        val checkboxes = getCheckboxesArray()

        val spinner = binding.registerCategoriesSpinner
        val continueButton = binding.registerContinueButton

        name?.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateName(name)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        surname?.setOnEditorActionListener {  _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateSurname(surname)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        dni?.setOnEditorActionListener {  _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateDNI(dni)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        email?.setOnEditorActionListener {  _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateEmail(email)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        binding.checkboxIDH?.setOnCheckedChangeListener { _, _ ->
            checkContinueButtonActivation()
        }
        binding.checkboxICI?.setOnCheckedChangeListener { _, _ ->
            checkContinueButtonActivation()
        }
        binding.checkboxICO?.setOnCheckedChangeListener { _, _ ->
            checkContinueButtonActivation()
        }
        binding.checkboxIDEI?.setOnCheckedChangeListener { _, _ ->
            checkContinueButtonActivation()
        }

        continueButton?.setOnClickListener {
            loadingDialog.startLoadingDialog()
            var institutesSelected = institutesUtils.getInstitutesSelected(checkboxes)
            dataSource.uploadUserToFirebase(
                name?.text.toString(),
                surname?.text.toString(),
                dni?.text.toString(),
                email?.text.toString(),
                spinner?.selectedItem.toString(),
                institutesSelected).
            addOnSuccessListener {
                loadingDialog.dismissDialog()
                val intent = Intent(this@RegisterActivity, PhotoRegisterActivity::class.java)
                intent.putExtra("name", name?.text.toString())
                intent.putExtra("surname", surname?.text.toString())
                intent.putExtra("dni", dni?.text.toString())
                intent.putExtra("email", email?.text.toString())

                startActivity(intent)
            }.addOnFailureListener { ex ->
                loadingDialog.dismissDialog()
                try {
                    throw ex
                } catch (e : FirebaseFirestoreException) {
                    Log.e("Firebase", e.message.toString())
                    val dialogClickListener =
                        DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_NEUTRAL -> {
                                }
                            }
                        }

                    val builder = AlertDialog.Builder(binding.root.context)

                    if(e.code == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                        builder.setMessage("El usuario ingresado ya existe").
                        setNeutralButton("Reintentar", dialogClickListener).
                        show()
                    } else {
                        builder.setMessage("Error al dar de alta el usuario, intente nuevamente").
                        setNeutralButton("Reintentar", dialogClickListener).
                        show()
                    }
                }
            }

        }

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            if (continueButton != null) {
                continueButton.isEnabled = loginState.isDataValid
            }

            if (loginState.usernameError != null) {
                if (name != null) {
                    name.error = getString(loginState.usernameError)
                }
            }

        })

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

    private fun checkContinueButtonActivation() {
        var categoriesWithNoInstitute = resources.getStringArray(R.array.user_categories_with_no_institute)

        val spinner = findViewById<Spinner>(R.id.register_categories_spinner)
        var categorySelected  = spinner.selectedItem.toString()

        if (formHasNoErrors()) {
            if (!categoriesWithNoInstitute.contains(categorySelected)) {
                if (validations.isAnyInstituteSelected(getCheckboxesArray())) {
                    binding.registerContinueButton?.isEnabled = true
                } else {
                    binding.registerContinueButton?.isEnabled = false
                }
            } else {
                binding.registerContinueButton?.isEnabled = true
            }
        } else {
            binding.registerContinueButton?.isEnabled = false
        }

    }

    private fun getCheckboxesArray() : ArrayList<CheckBox> {
        val checkboxICO = findViewById<CheckBox>(R.id.checkbox_ICO)
        val checkboxICI = findViewById<CheckBox>(R.id.checkbox_ICI)
        val checkboxIDH = findViewById<CheckBox>(R.id.checkbox_IDH)
        val checkboxIDEI = findViewById<CheckBox>(R.id.checkbox_IDEI)
        return arrayListOf(checkboxICO, checkboxICI, checkboxIDH, checkboxIDEI)
    }

    private fun formHasNoErrors() : Boolean{
        val nameHasNoErrors = binding.registerName?.error == null
        val surnameHasNoErrors = binding.registerSurname?.error == null
        val dniHasNoErrors = binding.registerDni?.error == null
        val emailHasNoErrors = binding.registerEmail?.error == null

        return nameHasNoErrors && surnameHasNoErrors && dniHasNoErrors && emailHasNoErrors
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