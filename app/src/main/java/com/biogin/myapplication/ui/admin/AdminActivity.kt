package com.biogin.myapplication.ui.admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.R
import com.biogin.myapplication.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAdminBinding
    private val firebaseAppId = getString(R.string.firebase_app_id)
    private val firebaseProjectId = getString(R.string.firebase_project_id)
    private val back4appAppId = getString(R.string.back4app_app_id)
    private val back4appClientKey = getString(R.string.back4app_client_key)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Por defecto, la configuración esta seteada a la de Firebase


        binding.firebaseConfigOption.setOnClickListener {

        }
    }

}