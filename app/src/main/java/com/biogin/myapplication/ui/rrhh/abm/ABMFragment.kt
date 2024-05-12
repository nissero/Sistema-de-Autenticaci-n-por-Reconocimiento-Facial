package com.biogin.myapplication.ui.rrhh.abm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.biogin.myapplication.Popup
import com.biogin.myapplication.UserManagement
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.data.Result
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.databinding.FragmentAbmBinding
import com.biogin.myapplication.ui.login.RegisterActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ABMFragment : Fragment() {

    private var _binding: FragmentAbmBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var loginRepo: LoginRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentAbmBinding.inflate(layoutInflater)
        loginRepo = LoginRepository(LoginDataSource())

        binding.updateUserOptionButton.setOnClickListener {
            var result: Result<LoggedInUser>
            runBlocking {
                lifecycleScope.launch {
                    var dni = binding.dniUserToFind.text.toString()
                    result = loginRepo.getUser(dni)

                    if (result is Result.Success) {
                        val userData = (result as Result.Success<LoggedInUser>).data
                        startActitivyUserManagement(userData, false)
                    } else {
                        openPopupUserNotFound()
                    }
                }
            }

        }

        binding.updateUserDniOptionButton.setOnClickListener {
            var result: Result<LoggedInUser>
            runBlocking {
                lifecycleScope.launch {
                    var dni = binding.dniUserToFind.text.toString()
                    result = loginRepo.getUser(dni)

                    if (result is Result.Success) {
                        val userData = (result as Result.Success<LoggedInUser>).data
                        Log.e("data", userData.toString())
                        startActitivyUserManagement(userData, true)
                    } else {
                        openPopupUserNotFound()
                    }
                }
            }
        }

        binding.registerUserOptionButton.setOnClickListener {
            startActivity(Intent(binding.root.context, RegisterActivity::class.java))
        }

        return binding.root
    }

    private fun openPopupUserNotFound() {
        val intent = Intent(binding.root.context, Popup::class.java)
        intent.putExtra("popup_text", "El usuario no existe para el DNI ingresado")
        intent.putExtra("text_button", "Reintentar")
        startActivity(intent)
    }
    private fun startActitivyUserManagement(userData : LoggedInUser, hasDNIUpdate : Boolean) {
        val intent = Intent(binding.root.context, UserManagement::class.java)
        intent.putExtra("dni", userData.dni)
        intent.putExtra("name", userData.name)
        intent.putExtra("surname", userData.surname)
        intent.putExtra("email", userData.email)
        intent.putExtra("state", userData.state)
        intent.putExtra("category", userData.category)
        intent.putStringArrayListExtra("institutes", userData.institutes)

        if (hasDNIUpdate) {
            intent.putExtra("button_option_chosed", "UpdateDNI")
        } else {
            intent.putExtra("button_option_chosed", "UpdateUser")
        }

        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}