package com.biogin.myapplication.ui.login

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.databinding.ActivityRegisterBinding
import com.biogin.myapplication.ui.LoadingDialog
import com.biogin.myapplication.utils.FormValidations
import com.biogin.myapplication.utils.InstitutesUtils
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Calendar


class RegisterActivity : AppCompatActivity() {
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var institutesUtils: InstitutesUtils
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var dataSource: LoginDataSource
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var fechaDesdeEditText: EditText
    private lateinit var fechaHastaEditText: EditText
    private var validations  = FormValidations()
    private var loadingDialog = LoadingDialog(this)
    private var fechaDesde = ""
    private var fechaHasta = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataSource = LoginDataSource()
        institutesUtils = InstitutesUtils()
        val categoriesWithNoInstitute =
            resources.getStringArray(R.array.user_categories_with_no_institute)
        val userCategories = resources.getStringArray(R.array.user_categories)
        val categoriesSpinner = findViewById<Spinner>(R.id.register_categories_spinner)
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, userCategories)
        categoriesSpinner.adapter = adapter

        fechaDesdeEditText = findViewById<EditText>(R.id.register_fecha_desde)
        fechaHastaEditText = findViewById<EditText>(R.id.register_fecha_hasta)

        fechaDesdeEditText.setOnClickListener{
            mostrarDatePickerDialog(fechaDesdeEditText)
            fechaHastaEditText.setText("")
            fechaHastaEditText.visibility = View.VISIBLE
        }
        fechaHastaEditText.setOnClickListener {
            mostrarDatePickerDialog(fechaHastaEditText)
        }

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinner = findViewById<Spinner>(R.id.register_categories_spinner)
                
                val categorySelected  = spinner.selectedItem.toString()

                if (categoriesWithNoInstitute.contains(categorySelected)) {
                    disableCheckboxes()
                } else {
                    enableCheckboxes()
                }

                if (categorySelected == "Externo"){
                    fechaDesdeEditText.visibility = View.VISIBLE
                } else {
                    fechaDesdeEditText.visibility = View.INVISIBLE
                    fechaHastaEditText.visibility = View.INVISIBLE
                }

                checkContinueButtonActivation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
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
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateName(name)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        surname?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateSurname(surname)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        dni?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateDNI(dni)
                checkContinueButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        email?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
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

            fechaDesde = formatDate(fechaDesdeEditText.text.toString())
            fechaHasta = formatDate(fechaHastaEditText.text.toString())


            Log.d("REGISTERACTIVITY", fechaDesde)
            Log.d("REGISTERACTIVITY", fechaHasta)


            loadingDialog.startLoadingDialog()
            val institutesSelected = institutesUtils.getInstitutesSelected(checkboxes)
            dataSource.uploadUserToFirebase(
                name?.text.toString(),
                surname?.text.toString(),
                dni?.text.toString(),
                email?.text.toString(),
                spinner?.selectedItem.toString(),
                institutesSelected,
                fechaDesde,
                fechaHasta
            ).addOnSuccessListener {
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
                } catch (e: FirebaseFirestoreException) {
                    Log.e("Firebase", e.message.toString())
                    val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_NEUTRAL -> {
                            }
                        }
                    }

                    val builder = AlertDialog.Builder(binding.root.context)

                    if(e.code == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                        builder.setMessage(e.message).
                        setNeutralButton("Reintentar", dialogClickListener).
                        show()
                    } else {
                        builder.setMessage("Error al dar de alta el usuario, intente nuevamente")
                            .setNeutralButton("Reintentar", dialogClickListener)
                            .show()
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

        onResume()
    }

    private fun formatDate(date: String): String {
        val splitText = date.split("/")

        return "${splitText[2]}/${splitText[1]}/${splitText[0]}"
    }

    private fun mostrarDatePickerDialog(targetEditText: EditText?) {
        val calendar = Calendar.getInstance()
        val actualYear = calendar.get(Calendar.YEAR)
        val actualMonth = calendar.get(Calendar.MONTH)
        val actualDay = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(this,
            { _, year, monthOfYear, dayOfMonth ->
                val formattedDay = String.format("%02d", dayOfMonth) // Add leading zero for day
                val formattedMonth = String.format("%02d", monthOfYear + 1) // Add leading zero for month
                val fechaSeleccionada = "$formattedDay/$formattedMonth/$year"
                targetEditText?.setText(fechaSeleccionada)
            },
            actualYear,
            actualMonth,
            actualDay
        )

        if (targetEditText == fechaHastaEditText){
            datePickerDialog.datePicker.minDate = fechaDesdeEditText.text.toString().toCalendarDate().timeInMillis
        } else {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        }
        datePickerDialog.show()
    }

    private fun String.toCalendarDate(): Calendar {
        val splitDate = split("/")
        val year = splitDate[2].toInt()
        val month = splitDate[1].toInt() - 1 // Month in Calendar is 0-based
        val day = splitDate[0].toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar
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
        val categoriesWithNoInstitute = resources.getStringArray(R.array.user_categories_with_no_institute)

        val spinner = findViewById<Spinner>(R.id.register_categories_spinner)
        val categorySelected  = spinner.selectedItem.toString()

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

    private fun clearFields() {
        binding.registerName?.setText("")
        binding.registerSurname?.setText("")
        binding.registerDni?.setText("")
        binding.registerEmail?.setText("")
        binding.registerCategoriesSpinner?.setSelection(0)
        binding.checkboxICO?.isChecked = false
        binding.checkboxIDH?.isChecked = false
        binding.checkboxICI?.isChecked = false
        binding.checkboxIDEI?.isChecked = false
    }

    override fun onResume() {
        super.onResume()
        clearFields()
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