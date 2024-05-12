package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.databinding.ActivityUserManagementBinding
import com.biogin.myapplication.utils.InstitutesUtils
import com.google.firebase.firestore.FirebaseFirestoreException


class UserManagement : AppCompatActivity() {
    private lateinit var loginRepo: LoginRepository
    private lateinit var dataSource: LoginDataSource
    private lateinit var insitutesUtils : InstitutesUtils
    private lateinit var binding: ActivityUserManagementBinding
    private var oldDni : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_management)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        insitutesUtils = InstitutesUtils()
        dataSource = LoginDataSource()
        var userCategories = resources.getStringArray(R.array.user_categories)
        val categoriesSpinner = findViewById<Spinner>(R.id.update_user_categories_spinner)
        val adapterCategories = ArrayAdapter(this, R.layout.simple_spinner_item, userCategories)
        categoriesSpinner.adapter = adapterCategories

        var userStates = resources.getStringArray(R.array.user_active_options)
        val userStateSpinner = findViewById<Spinner>(R.id.update_user_state_spinner)
        val adapterStates = ArrayAdapter(this, R.layout.simple_spinner_item, userStates)
        userStateSpinner.adapter = adapterStates

        loginRepo = LoginRepository(LoginDataSource())

        if (intent.getStringExtra("button_option_chosed") == "UpdateUser") {
            binding.updateUserDni.visibility = View.GONE
            binding.duplicateUserButton.visibility = View.GONE
            binding.updateUserButton.visibility = View.VISIBLE
            val params = binding.updateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.endToEnd = binding.linearLayout.id
        } else {
            oldDni = intent.getStringExtra("dni").toString()
            binding.updateUserDni.visibility = View.VISIBLE
            binding.duplicateUserButton.visibility = View.VISIBLE
            binding.updateUserButton.visibility = View.GONE
            val params = binding.duplicateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = binding.linearLayout.id
        }


        val categories: Array<out String> = resources.getStringArray(R.array.user_categories)
        val categoryIndex = categories.indexOfFirst { it == intent.getStringExtra("category") }

        binding.updateUserName.setText(intent.getStringExtra("name"))
        binding.updateUserSurname.setText(intent.getStringExtra("surname"))
        binding.updateUserEmail.setText(intent.getStringExtra("email"))
        binding.updateUserDni.setText(intent.getStringExtra("dni"))
        binding.updateUserCategoriesSpinner.setSelection(categoryIndex)
        if (intent.getStringExtra("state").equals("Activo")) {
            binding.updateUserStateSpinner.setSelection(0)
        } else {
            binding.updateUserStateSpinner.setSelection(1)
        }
        var institutes = ArrayList<String>()
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
            val selectedIstitutes = insitutesUtils.getInstitutesSelected(checkboxes)
            val task = dataSource.modifyUserFirebase(
                binding.updateUserName.text.toString(),
                binding.updateUserSurname.text.toString(),
                binding.updateUserDni.text.toString(),
                binding.updateUserEmail.text.toString(),
                binding.updateUserCategoriesSpinner.selectedItem.toString(),
                selectedIstitutes
            )

            task.addOnSuccessListener {
                Log.e("Firebase", "Update usuario exitoso")
                showPopup("Se actualizó el usuario de forma exitosa", "Salir")
                finish()
            }.addOnFailureListener {ex ->
                try {
                    throw ex
                } catch (e : FirebaseFirestoreException) {
                    showPopup("Error al modificar el usuario, intente nuevamente", "Reintentar")
                }
            }
        }

        binding.duplicateUserButton.setOnClickListener {
            val checkboxes = arrayListOf(binding.checkboxICI, binding.checkboxICO, binding.checkboxIDEI, binding.checkboxIDH)
            val selectedIstitutes = insitutesUtils.getInstitutesSelected(checkboxes)
            val task = dataSource.duplicateUserInFirebase(
                binding.updateUserName.text.toString(),
                binding.updateUserSurname.text.toString(),
                oldDni,
                binding.updateUserDni.text.toString(),
                binding.updateUserEmail.text.toString(),
                binding.updateUserCategoriesSpinner.selectedItem.toString(),
                selectedIstitutes
            )

            task.addOnSuccessListener {
                Log.e("Firebase", "Duplicacion usuario/update dni exitoso")
                showPopup("Se actualizó el dni del usuario de forma exitosa", "Salir")
                finish()
            }.addOnFailureListener {ex ->
                try {
                    throw ex
                } catch (e : FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.ALREADY_EXISTS) {
                        showPopup("El DNI ingresado ya existe, compruebe el dato ingresado", "Reintentar")
                    } else {
                        showPopup("Error al modificar el DNI, intente nuevamente", "Reintentar")
                    }
                }
                Log.e("Firebase", ex.toString())
            }


        }


    }


    private fun showPopup(popupText : String, popupButtonText : String) {
        val intent = Intent(this@UserManagement, Popup::class.java)
        intent.putExtra("popup_text", popupText)
        intent.putExtra("text_button", popupButtonText)
        startActivity(intent)
    }

}