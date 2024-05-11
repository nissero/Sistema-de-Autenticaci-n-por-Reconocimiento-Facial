package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.biogin.myapplication.data.LoginDataSource
import com.biogin.myapplication.data.LoginRepository
import com.biogin.myapplication.data.Result
import com.biogin.myapplication.data.model.LoggedInUser
import com.biogin.myapplication.databinding.ActivityRegisterBinding
import com.biogin.myapplication.databinding.ActivityUserFinderBinding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class UserFinderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserFinderBinding
    private lateinit var loginRepo: LoginRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loginRepo = LoginRepository(LoginDataSource())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.updateUserOptionButton.setOnClickListener {
            var result:  Result<LoggedInUser>
            runBlocking {
               lifecycleScope.launch {
                   var dni = binding.dniUserToFind.text.toString()
                    result = loginRepo.getUser(dni)

                    if (result is Result.Success) {
                        val userData = (result as Result.Success<LoggedInUser>).data
                        Log.e("data", userData.toString())
                        startActitivyUserManagement(userData, false)
                    } else {
                        val intent = Intent(this@UserFinderActivity, PopupUserNotFound::class.java)
                        startActivity(intent)
                    }
                }
            }

        }

        binding.updateDniOptionButton.setOnClickListener {
            var result:  Result<LoggedInUser>
            runBlocking {
                lifecycleScope.launch {
                    var dni = binding.dniUserToFind.text.toString()
                    result = loginRepo.getUser(dni)

                    if (result is Result.Success) {
                        val userData = (result as Result.Success<LoggedInUser>).data
                        Log.e("data", userData.toString())
                        startActitivyUserManagement(userData, true)
                    } else {
                        val intent = Intent(this@UserFinderActivity, PopupUserNotFound::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun startActitivyUserManagement(userData : LoggedInUser, hasDNIUpdate : Boolean) {
        val intent = Intent(this@UserFinderActivity, UserManagement::class.java)
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


}