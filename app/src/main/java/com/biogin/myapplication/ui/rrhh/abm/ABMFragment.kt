package com.biogin.myapplication.ui.rrhh.abm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.biogin.myapplication.UserManagement
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.data.Result
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.databinding.FragmentAbmBinding
import com.biogin.myapplication.ui.login.RegisterActivity
import com.biogin.myapplication.utils.PopUpUtil
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ABMFragment : Fragment() {
    private var _binding: FragmentAbmBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var loginRepo: LoginRepository
    private lateinit var dataSource : LoginDataSource
    private val popUpUtil = PopUpUtil()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentAbmBinding.inflate(layoutInflater)
        dataSource = LoginDataSource()
        loginRepo = LoginRepository(dataSource)

        binding.updateUserOptionButton.setOnClickListener {
            var result: Result<LoggedInUser>
            runBlocking {
                lifecycleScope.launch {
                    val dni = binding.dniUserToFind.text.toString()
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
                    val dni = binding.dniUserToFind.text.toString()
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

        binding.deactivateUserOption.setOnClickListener {
            dataSource.deactivateUserFirebase(binding.dniUserToFind.text.toString()).
            addOnSuccessListener {
                openPopupUserSuccessfullyDeleted()
            }.addOnFailureListener { ex ->
                try {
                    throw ex
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                        openPopupUserNotFound()
                    } else if (e.code == FirebaseFirestoreException.Code.INVALID_ARGUMENT) {
                        openPopupUserAlreadyDeactivated()
                    }
                }
            }
        }

        return binding.root
    }

    private fun openPopupUserNotFound() {
        popUpUtil.showPopUp(binding.root.context,
            "El usuario no existe para el DNI ingresado",
            "Reintentar")
//        val intent = Intent(binding.root.context, Popup::class.java)
//        intent.putExtra("popup_text", "El usuario no existe para el DNI ingresado")
//        intent.putExtra("text_button", "Reintentar")
//        startActivity(intent)
    }

    private fun openPopupUserAlreadyDeactivated() {
        popUpUtil.showPopUp(binding.root.context,
            "El usuario ya se encuentra inactivo",
            "Continuar")
//        val intent = Intent(binding.root.context, Popup::class.java)
//        intent.putExtra("popup_text", "El usuario ya se encuentra eliminado")
//        intent.putExtra("text_button", "Continuar")
//        startActivity(intent)
    }

    private fun openPopupUserSuccessfullyDeleted() {
        popUpUtil.showPopUp(binding.root.context,
            "Usuario desactivado de forma exitosa",
            "Continuar")
//        val intent = Intent(binding.root.context, Popup::class.java)
//        intent.putExtra("popup_text", "Usuario eliminado de forma exitosa")
//        intent.putExtra("text_button", "Continuar")
//        startActivity(intent)
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
            intent.putExtra("button_option_chosen", "UpdateDNI")
        } else {
            intent.putExtra("button_option_chosen", "UpdateUser")
        }

        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}