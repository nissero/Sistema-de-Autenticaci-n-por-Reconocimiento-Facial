package com.biogin.myapplication.ui.login

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.biogin.myapplication.PhotoRegisterActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.databinding.ActivityRegisterBinding
import com.biogin.myapplication.ui.LoadingDialog
import com.biogin.myapplication.utils.FormValidations
import com.biogin.myapplication.utils.InstitutesUtils
import com.biogin.myapplication.utils.StringUtils
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import java.util.Calendar


class RegisterActivity : AppCompatActivity() {
    private lateinit var institutesUtils: InstitutesUtils
    private lateinit var dataSource: LoginDataSource
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var fechaDesdeEditText: EditText
    private lateinit var fechaHastaEditText: EditText
    private lateinit var datePickerDialog: com.biogin.myapplication.utils.DatePickerDialog
    private var validations  = FormValidations()
    private var loadingDialog = LoadingDialog(this)
    private var fechaDesde = ""
    private var fechaHasta = ""
    private var areAllFieldsValid = false
    private val stringUtils = StringUtils()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataSource = LoginDataSource()
        institutesUtils = InstitutesUtils()
        val categoriesWithNoInstitute = intent.getStringArrayListExtra("categories with no institutes")
        val temporaryCategories = intent.getStringArrayListExtra("temporary categories")
        val userCategories = intent.getStringArrayListExtra("categories")
        val categoriesSpinner = findViewById<Spinner>(R.id.register_categories_spinner)
        val adapter = userCategories?.let { ArrayAdapter(this, R.layout.simple_spinner_item, it.toList()) }
        categoriesSpinner.adapter = adapter


        fechaDesdeEditText = findViewById(R.id.register_fecha_desde)
        fechaHastaEditText = findViewById(R.id.register_fecha_hasta)

        datePickerDialog = com.biogin.myapplication.utils.DatePickerDialog()

        fechaDesdeEditText.setOnClickListener{
            datePickerDialog.showDatePickerDialog(fechaDesdeEditText, fechaHastaEditText, System.currentTimeMillis(), this) {
                fechaHastaEditText.setText("")
                fechaHastaEditText.visibility = View.VISIBLE
            }
        }
        fechaHastaEditText.setOnClickListener {
            datePickerDialog.showDatePickerDialog(fechaHastaEditText, null, fechaDesdeEditText.text.toString().toCalendarDate().timeInMillis, this) {}
        }

        val name = binding.registerName
        val surname = binding.registerSurname
        val dni = binding.registerDni
        val email = binding.registerEmail
        val checkboxes = getCheckboxesArray()
        val continueButton = binding.registerContinueButton

        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val spinner = findViewById<Spinner>(R.id.register_categories_spinner)

                val categorySelected  = spinner.selectedItem.toString()

                if (categoriesWithNoInstitute != null) {
                    if (categoriesWithNoInstitute.contains(categorySelected)) {
                        disableCheckboxes()
                        disableAlertCheckAtLeastOneInstitute()
                    } else {
                        enableCheckboxes()
                        enableAlertCheckAtLeastOneInstitute()
                    }
                }

                if (temporaryCategories != null) {
                    if (temporaryCategories.contains(categorySelected)){
                        fechaDesdeEditText.visibility = View.VISIBLE
                    } else {
                        fechaDesdeEditText.visibility = View.INVISIBLE
                        fechaHastaEditText.visibility = View.INVISIBLE
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        name?.setOnFocusChangeListener { _, _ ->
            validations.validateName(name)
        }

        surname?.setOnFocusChangeListener { _, _ ->
            validations.validateSurname(surname)
        }

        dni?.setOnFocusChangeListener { _, _ ->
            validations.validateDNI(dni)
        }

        dni?.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                validations.validateDNI(dni)
                return@setOnEditorActionListener true
            }
            false
        }

        email?.setOnFocusChangeListener { _, _ ->
            validations.validateEmail(email)
        }

        fechaDesdeEditText.doOnTextChanged { _, _, _, _ ->
            validations.validateEmptyDate(fechaDesdeEditText)
        }

        fechaHastaEditText.doOnTextChanged { _, _, _, _ ->
            validations.validateEmptyDate(fechaHastaEditText)
        }

        continueButton?.setOnClickListener {
            val spinner = findViewById<Spinner>(R.id.register_categories_spinner)
            val categorySelected = spinner.selectedItem.toString()

            val categoryIsTemporary = temporaryCategories?.contains(categorySelected)!!

            if(categoryIsTemporary) {
                if(fechaDesdeEditText.text.toString().isNotEmpty()) {
                    fechaDesde = datePickerDialog.formatDate(fechaDesdeEditText.text.toString())
                }
                if(fechaHastaEditText.text.toString().isNotEmpty()) {
                    fechaHasta = datePickerDialog.formatDate(fechaHastaEditText.text.toString())
                }
            }

            Log.d("REGISTERACTIVITY", fechaDesde)
            Log.d("REGISTERACTIVITY", fechaHasta)

            areAllFieldsValid = validations.isFormValid(
                name!!,
                surname!!,
                dni!!,
                email!!,
                categorySelected,
                getCheckboxesArray(),
                categoryIsTemporary,
                fechaDesdeEditText,
                fechaHastaEditText
            )
            if (areAllFieldsValid) {
                val institutesSelected = institutesUtils.getInstitutesSelected(checkboxes)
                val normalizedName = stringUtils.normalizeAndSentenceCase(name.text.toString())
                val normalizedSurname = stringUtils.normalizeAndSentenceCase(surname.text.toString())
                var existsUserWithGivenEmail : Boolean
                var existsUserWithGivenDni : Boolean
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_NEUTRAL -> {
                        }
                    }
                }

                runBlocking {
                    existsUserWithGivenEmail = dataSource.existsUserWithGivenEmail(email.text.toString())
                    existsUserWithGivenDni = dataSource.existsUserWithGivenDni(dni.text.toString())
                    if(existsUserWithGivenEmail) {
                        val builder = AlertDialog.Builder(binding.root.context)
                        builder.setMessage("El email ingresado ya existe, intente nuevamente")
                            .setNeutralButton("Reintentar", dialogClickListener).show()
                    } else if (existsUserWithGivenDni) {
                        val builder = AlertDialog.Builder(binding.root.context)
                        builder.setMessage("El dni ingresado ya existe, intente nuevamente")
                            .setNeutralButton("Reintentar", dialogClickListener).show()
                    } else {

                    }
                }

                if(!existsUserWithGivenDni && !existsUserWithGivenEmail) {
                    val intent = Intent(this@RegisterActivity, PhotoRegisterActivity::class.java)
                    intent.putExtra("name", normalizedName)
                    intent.putExtra("surname", normalizedSurname)
                    intent.putExtra("dni", dni.text.toString())
                    intent.putExtra("email", email.text.toString())
                    intent.putExtra("category", spinner?.selectedItem.toString())
                    intent.putStringArrayListExtra("institutes", institutesSelected)
                    onActivityResultLauncherRegisterActivity.launch(intent)
                }
            }
        }

        onResume()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val onActivityResultLauncherRegisterActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        val result = activityResult.resultCode
        val data = activityResult.data
        if (data != null) {
            if (result == RESULT_OK) {
                loadingDialog.startLoadingDialog()
                dataSource.uploadUserToFirebase(
                    data.getStringExtra("name")!!,
                    data.getStringExtra("surname")!! ,
                    data.getStringExtra("dni")!!,
                    data.getStringExtra("email")!!,
                    data.getStringExtra("category")!!,
                    data.getStringArrayListExtra("institutes")!!,
                    fechaDesde,
                    fechaHasta
                ).addOnSuccessListener {
                    loadingDialog.dismissDialog()
                    Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_LONG)
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

                        if (e.code == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                            builder.setMessage(e.message)
                                .setNeutralButton("Reintentar", dialogClickListener).show()
                        } else {
                            builder.setMessage("Error al dar de alta el usuario, intente nuevamente")
                                .setNeutralButton("Reintentar", dialogClickListener)
                                .show()
                        }
                    }
                }
        }
        } else {
            Toast.makeText(binding.root.context, "No se pudo registrar al usuario", Toast.LENGTH_LONG)
        }
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
    private fun enableAlertCheckAtLeastOneInstitute() {
        binding.errTextCheckboxesNotSelectedRegister?.visibility = View.VISIBLE
    }

    private fun disableAlertCheckAtLeastOneInstitute() {
        binding.errTextCheckboxesNotSelectedRegister?.visibility = View.INVISIBLE
    }

    private fun getCheckboxesArray(): ArrayList<CheckBox> {
        val checkboxICO = findViewById<CheckBox>(R.id.checkbox_ICO)
        val checkboxICI = findViewById<CheckBox>(R.id.checkbox_ICI)
        val checkboxIDH = findViewById<CheckBox>(R.id.checkbox_IDH)
        val checkboxIDEI = findViewById<CheckBox>(R.id.checkbox_IDEI)
        return arrayListOf(checkboxICO, checkboxICI, checkboxIDH, checkboxIDEI)
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