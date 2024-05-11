package com.biogin.myapplication

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.data.Result
import com.biogin.myapplication.databinding.ActivityUserManagementBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class UserManagement : AppCompatActivity() {
    private lateinit var loginRepo: LoginRepository
    private lateinit var binding: ActivityUserManagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_management)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var userCategories = resources.getStringArray(R.array.user_categories)
        val categoriesSpinner = findViewById<Spinner>(R.id.update_user_categories_spinner)
        val adapterCategories = ArrayAdapter(this, R.layout.simple_spinner_item, userCategories)
        categoriesSpinner.adapter = adapterCategories

        var userStates = resources.getStringArray(R.array.user_active_options)
        val userStateSpinner = findViewById<Spinner>(R.id.update_user_state_spinner)
        val adapterStates = ArrayAdapter(this, R.layout.simple_spinner_item, userStates)
        userStateSpinner.adapter = adapterStates

        loginRepo = LoginRepository(LoginDataSource())

        if(intent.getStringExtra("button_option_chosed") == "UpdateUser")  {
            binding.updateUserDni.visibility = View.GONE
            binding.duplicateUserButton.visibility = View.GONE
            binding.updateUserButton.visibility = View.VISIBLE
            val params = binding.updateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.endToEnd = binding.linearLayout.id
        } else {
            binding.updateUserDni.visibility = View.VISIBLE
            binding.duplicateUserButton.visibility = View.VISIBLE
            binding.updateUserButton.visibility = View.GONE
            val params = binding.duplicateUserButton.layoutParams as ConstraintLayout.LayoutParams
            params.startToStart = binding.linearLayout.id
        }



        runBlocking {
            lifecycleScope.launch {
                val result = intent.getStringExtra("dni")?.let { loginRepo.getUser(it) }
                if (result is Result.Success) {

                    val categories : Array<out String> = resources.getStringArray(R.array.user_categories)
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
                    var institutes = intent.getStringArrayListExtra("institutes")
                    if (institutes != null) {
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
                    }
                    Log.e("test", institutes.toString())
                }
            }
        }





    }


}