package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.databinding.ActivityHomeBinding
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.biogin.myapplication.utils.ConnectionCheck
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityHomeBinding
    private lateinit var firebaseSyncManager: FirebaseSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseSyncManager = FirebaseSyncService(this)

        viewBinding.buttonFaceRecognition.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "seguridad")
            startActivity(intent)
        }

        viewBinding.buttonFaceRecognitionRrhh.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "rrhh")
            startActivity(intent)
        }

        val db = OfflineDataBaseHelper(this)

        viewBinding.buttonLoginOffline.setOnClickListener{
            Log.d("HOME", "USERS: ${db.getAllUsers()}")
            Log.d("HOME", "SECURITY ${db.getAllSecurity()}")
            val intent = Intent(this, OfflineLogInActivity::class.java)
            intent.putExtra("authenticationType", "seguridad")
            startActivity(intent)
        }
    }
}