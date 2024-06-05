package com.biogin.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.databinding.ActivityUserManagementBinding
import com.biogin.myapplication.utils.EmailService
import com.biogin.myapplication.utils.FormValidations
import com.biogin.myapplication.utils.InstitutesUtils
import com.biogin.myapplication.utils.PopUpUtils
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.mail.internet.InternetAddress

class UserManagement : AppCompatActivity() {
    private var dataSource = LoginDataSource()
    private var loginRepo = LoginRepository(dataSource)
    private var institutesUtils = InstitutesUtils()
    private lateinit var binding: ActivityUserManagementBinding
    private var oldDni : String = ""
    private var validations  = FormValidations()
    private val popUpUtil = PopUpUtils()
    private val emailService = EmailService("smtp-mail.outlook.com", 587)
    private val firebaseMethods = FirebaseMethods()
    private lateinit var oldDniLogs: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = binding.updateUserName
        val surname = binding.updateUserSurname
        val email = binding.updateUserEmail
        val newDni = binding.updateUserDni
        val categoriesSpinner = findViewById<Spinner>(R.id.update_user_categories_spinner)
        val userStateSpinner = findViewById<Spinner>(R.id.update_user_state_spinner)

        val userCategories = intent.getStringArrayListExtra("categories")
        val adapterCategories =
            userCategories?.let { ArrayAdapter(this, R.layout.simple_spinner_item, it.toList()) }
        categoriesSpinner.adapter = adapterCategories

        val temporaryCategories = intent.getStringArrayListExtra("temporary categories")

        val userStates = resources.getStringArray(R.array.user_active_options)
        val adapterStates = ArrayAdapter(this, R.layout.simple_spinner_item, userStates)
        userStateSpinner.adapter = adapterStates

        if (intent.getStringExtra("button_option_chosen") == "UpdateUser") {
            binding.updateUserDni.visibility = View.GONE
            binding.duplicateUserButton.visibility = View.GONE
            binding.updateUserButton.visibility = View.VISIBLE
            val params = binding.updateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.endToEnd = binding.linearLayout.id
        } else {
            oldDni = intent.getStringExtra("dni").toString()

            firebaseMethods.getLogsFromDni(oldDni) {
                    logs -> oldDniLogs = logs
            }

            binding.updateUserDni.visibility = View.VISIBLE
            binding.duplicateUserButton.visibility = View.VISIBLE
            binding.updateUserButton.visibility = View.GONE
            val params = binding.duplicateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = binding.linearLayout.id
        }

        val categories: ArrayList<String>? = intent.getStringArrayListExtra("categories")
        val categoryIndex = categories?.indexOfFirst { it == intent.getStringExtra("category") }

        binding.updateUserName.setText(intent.getStringExtra("name"))
        binding.updateUserSurname.setText(intent.getStringExtra("surname"))
        binding.updateUserEmail.setText(intent.getStringExtra("email"))
        binding.updateUserDni.setText(intent.getStringExtra("dni"))
        if (categoryIndex != null) {
            binding.updateUserCategoriesSpinner.setSelection(categoryIndex)
        }
        if (intent.getStringExtra("state").equals("Activo")) {
            binding.updateUserStateSpinner.setSelection(0)
        } else {
            binding.updateUserStateSpinner.setSelection(1)
        }
        val institutes = ArrayList<String>()
        institutes.addAll(intent.getStringArrayListExtra("institutes")!!)
        for (institute in institutes) {
            Log.e("firebase", institute)
            if (institute == "ICO") {
                binding.checkboxICO.isChecked = true
            }
            if (institute == "IDH") {
                binding.checkboxIDH.isChecked = true
            }
            if (institute == "ICI") {
                binding.checkboxICI.isChecked = true
            }
            if (institute == "IDEI") {
                binding.checkboxIDEI.isChecked = true
            }
        }

        binding.updateUserButton.setOnClickListener {
            val checkboxes = arrayListOf(binding.checkboxICI, binding.checkboxICO, binding.checkboxIDEI, binding.checkboxIDH)
            val selectedInstitutes = institutesUtils.getInstitutesSelected(checkboxes)
            val task = dataSource.modifyUserFirebase(
                binding.updateUserName.text.toString(),
                binding.updateUserSurname.text.toString(),
                binding.updateUserDni.text.toString(),
                binding.updateUserEmail.text.toString(),
                binding.updateUserCategoriesSpinner.selectedItem.toString(),
                binding.updateUserStateSpinner.selectedItem.toString(),
                selectedInstitutes
            )

            task.addOnSuccessListener {
                Log.e("Firebase", "Update usuario exitoso")
                popUpUtil.showPopUp(binding.root.context,
                    "Se actualizó el usuario de forma exitosa", "Salir")
                finish()
            }.addOnFailureListener {ex ->
                try {
                    throw ex
                } catch (e : FirebaseFirestoreException) {
                    popUpUtil.showPopUp(binding.root.context,
                        "Error al modificar el usuario, intente nuevamente",
                        "Reintentar")
                }
            }
        }

        binding.buttonTest.setOnClickListener {
            sendEmailOnDniChange(oldDni, binding.updateUserDni.text.toString())
        }

        binding.duplicateUserButton.setOnClickListener {
            val checkboxes = arrayListOf(binding.checkboxICI, binding.checkboxICO, binding.checkboxIDEI, binding.checkboxIDH)
            val selectedInstitutes = institutesUtils.getInstitutesSelected(checkboxes)
            val task = dataSource.duplicateUserInFirebase(
                binding.updateUserName.text.toString(),
                binding.updateUserSurname.text.toString(),
                oldDni,
                binding.updateUserDni.text.toString(),
                binding.updateUserEmail.text.toString(),
                binding.updateUserCategoriesSpinner.selectedItem.toString(),
                selectedInstitutes
            )

            task.addOnSuccessListener {
                Log.e("Firebase", "Duplicacion usuario/update dni exitoso")
                popUpUtil.showPopUp(binding.root.context,
                    "Se actualizó el dni del usuario de forma exitosa",
                    "Salir")
                intent.getStringExtra("dni")?.let { it1 ->
                    sendEmailOnDniChange(
                        it1,
                        binding.updateUserDni.text.toString())
                }
                finish()
            }.addOnFailureListener {ex ->
                try {
                    throw ex
                } catch (e : FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                        popUpUtil.showPopUp(binding.root.context,
                            "El DNI ingresado ya existe, compruebe el dato ingresado",
                            "Reintentar")
                    } else {
                        popUpUtil.showPopUp(binding.root.context,
                            "Error al modificar el DNI, intente nuevamente",
                            "Reintentar")
                    }
                }
                Log.e("Firebase", ex.toString())
            }
        }

        val categoriesWithNoInstitute = intent.getStringArrayListExtra("categories with no institutes")
        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val categorySelected  = categoriesSpinner.selectedItem.toString()

                if (categoriesWithNoInstitute != null) {
                    if (categoriesWithNoInstitute.contains(categorySelected)) {
                        disableCheckboxes()
                    } else {
                        enableCheckboxes()
                    }
                }
                checkUpdateButtonActivation()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.checkboxIDH.setOnCheckedChangeListener { _, _ ->
            checkUpdateButtonActivation()
        }
        binding.checkboxICI.setOnCheckedChangeListener { _, _ ->
            checkUpdateButtonActivation()
        }
        binding.checkboxICO.setOnCheckedChangeListener { _, _ ->
            checkUpdateButtonActivation()
        }
        binding.checkboxIDEI.setOnCheckedChangeListener { _, _ ->
            checkUpdateButtonActivation()
        }

        name.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateName(name)
                checkUpdateButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        surname.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateSurname(surname)
                checkUpdateButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }

        email.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                validations.validateEmail(email)
                checkUpdateButtonActivation()
                return@setOnEditorActionListener false
            }
            false
        }
    }

    private fun checkUpdateButtonActivation() {

        val buttonToEnable : Button = if(intent.getStringExtra("button_option_chosen") == "UpdateUser") {
            findViewById(R.id.update_user_button)
        } else {
            findViewById(R.id.duplicate_user_button)
        }

        val categoriesWithNoInstitute = resources.getStringArray(R.array.user_categories_with_no_institute)

        val spinner = findViewById<Spinner>(R.id.update_user_categories_spinner)
        val categorySelected  = spinner.selectedItem.toString()

        if (formHasNoErrors()) {
            if (!categoriesWithNoInstitute.contains(categorySelected)) {
                if (validations.isAnyInstituteSelected(getCheckboxesArray())) {
                    buttonToEnable.isEnabled = true
                } else {
                    buttonToEnable.isEnabled = false
                }
            } else {
                buttonToEnable.isEnabled = true
            }
        } else {
            buttonToEnable.isEnabled = false
        }
    }

    private fun formHasNoErrors() : Boolean{
        val nameHasNoErrors = binding.updateUserName.error == null
        val surnameHasNoErrors = binding.updateUserSurname.error == null
        val dniHasNoErrors = binding.updateUserDni.error == null
        val emailHasNoErrors = binding.updateUserEmail.error == null

        return nameHasNoErrors && surnameHasNoErrors && dniHasNoErrors && emailHasNoErrors
    }

    private fun getCheckboxesArray() : ArrayList<CheckBox> {
        val checkboxICO = findViewById<CheckBox>(R.id.checkbox_ICO)
        val checkboxICI = findViewById<CheckBox>(R.id.checkbox_ICI)
        val checkboxIDH = findViewById<CheckBox>(R.id.checkbox_IDH)
        val checkboxIDEI = findViewById<CheckBox>(R.id.checkbox_IDEI)
        return arrayListOf(checkboxICO, checkboxICI, checkboxIDH, checkboxIDEI)
    }

    private fun enableCheckboxes() {
        binding.checkboxIDEI.isEnabled = true
        binding.checkboxICO.isEnabled = true
        binding.checkboxIDH.isEnabled = true
        binding.checkboxICI.isEnabled = true

        binding.checkboxIDEI.visibility = View.VISIBLE
        binding.checkboxICO.visibility = View.VISIBLE
        binding.checkboxIDH.visibility = View.VISIBLE
        binding.checkboxICI.visibility = View.VISIBLE
    }

    private fun disableCheckboxes() {
        binding.checkboxIDEI.isEnabled = false
        binding.checkboxICO.isEnabled = false
        binding.checkboxIDH.isEnabled = false
        binding.checkboxICI.isEnabled = false

        binding.checkboxIDEI.isChecked = false
        binding.checkboxICO.isChecked = false
        binding.checkboxIDH.isChecked = false
        binding.checkboxICI.isChecked = false

        binding.checkboxIDEI.visibility = View.INVISIBLE
        binding.checkboxICO.visibility = View.INVISIBLE
        binding.checkboxIDH.visibility = View.INVISIBLE
        binding.checkboxICI.visibility = View.INVISIBLE
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendEmailOnDniChange(oldDni: String, newDni: String) {
        println("success")
        val auth = EmailService.UserPassAuthenticator("fernandoivanantunez@hotmail.com",
            "steveharris40184869")
        val to = listOf(InternetAddress("antunez.fernandoivan.43377@gmail.com"))
        val from = InternetAddress("fernandoivanantunez@hotmail.com")
        val subject = "Aviso de cambio de DNI"
        val body = "Buenas, le enviamos este mail para informarle que al usuario registrado con el " +
                "DNI $oldDni se le ha modificado el mismo por $newDni.\nLe dejamos un reporte de los " +
                "registros del DNI previo:\n$oldDniLogs"
        val email = EmailService.Email(auth, to, from, subject, body)

        GlobalScope.launch {
            try {
                emailService.send(email)
            } catch (e: Exception) {
                try {
                    emailService.send(email)
                } catch (e: Exception) {
                    println(e.stackTrace)
                }
            }
        }
    }

}