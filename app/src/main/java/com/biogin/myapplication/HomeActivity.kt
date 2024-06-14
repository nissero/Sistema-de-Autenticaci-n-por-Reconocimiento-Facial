package com.biogin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivityHomeBinding
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper


class HomeActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityHomeBinding
    private lateinit var firebaseSyncManager: FirebaseSyncService
    private lateinit var logsRepository: LogsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MasterUserDataSession.clearUserData()
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(viewBinding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logsRepository = LogsRepository()
        firebaseSyncManager = FirebaseSyncService(this)
        val sqlDb = OfflineDataBaseHelper(this)
        Log.e("DB", sqlDb.getAllLogs())
        logsRepository.syncLogsOfflineWithOnline(sqlDb)
        viewBinding.buttonFaceRecognitionSecurity.setOnClickListener {
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

        viewBinding.buttonLoginOffline.setOnClickListener {
            Log.d("HOME", "USERS: ${db.getAllUsers()}")
            Log.d("HOME", "SECURITY ${db.getAllSecurity()}")
            val intent = Intent(this, OfflineLogInActivity::class.java)
            intent.putExtra("authenticationType", "seguridad")
            startActivity(intent)
        }

        viewBinding.buttonFaceRecognitionAdmin.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "admin")
            startActivity(intent)
        }

        viewBinding.buttonFaceRecognitionJerarquico.setOnClickListener {
            val intent = Intent(this, FaceRecognitionActivity::class.java)
            intent.putExtra("authenticationType", "jerarquico")
            startActivity(intent)
        }
    }
}