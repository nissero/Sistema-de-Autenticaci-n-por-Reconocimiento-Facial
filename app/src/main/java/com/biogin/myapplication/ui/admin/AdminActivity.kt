package com.biogin.myapplication.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.HomeActivity
import com.biogin.myapplication.R
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivityAdminBinding
import com.biogin.myapplication.logs.Log
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.parse.Parse
import com.parse.ParseObject
import java.util.Calendar

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var firebaseAppId: String
    private lateinit var firebaseProjectId: String
    private lateinit var back4appAppId: String
    private lateinit var back4appClientKey: String
    private val logsRepository = LogsRepository()
    private var routingOptionSelected = "Firebase"
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

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(binding.root.context, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finishAffinity()
                finish()
            }
        })

        firebaseAppId = getString(R.string.firebase_app_id)
        firebaseProjectId = getString(R.string.firebase_project_id)
        back4appAppId = getString(R.string.back4app_app_id)
        back4appClientKey = getString(R.string.back4app_client_key)

        // Por defecto, la configuración esta seteada a la de Firebase
        val editTextAppId = findViewById<EditText>(R.id.edit_app_id)
        val editTextSecondaryRoutingOption = findViewById<EditText>(R.id.edit_project_id)
        editTextAppId.setText(firebaseAppId)
        editTextSecondaryRoutingOption.setText(firebaseProjectId)


        val chipGroupRoutingOptions = findViewById<ChipGroup>(R.id.chip_group_routing_opts)

        chipGroupRoutingOptions.setOnCheckedChangeListener { _, _ ->
            val chipFirebaseOption = findViewById<Chip>(R.id.firebase_config_option)
            val chipBack4AppOption = findViewById<Chip>(R.id.back4apps_config_option)
            if (chipFirebaseOption.isChecked) {
                setFirebaseRoutingData()
            } else if (chipBack4AppOption.isChecked) {
                setBack4AppRoutingData()
            } else {
                clearRoutingData()
            }
        }

        val adminConnectionBtn = findViewById<Button>(R.id.admin_connection_btn)
        adminConnectionBtn.setOnClickListener {
            if (routingOptionSelected == "Firebase") {
                try {
                    logsRepository.logEvent(
                        Log.LogEventType.INFO,
                        Log.LogEventName.FIREBASE_SUCCESSFUL_CONNECTION,
                        MasterUserDataSession.getDniUser(),
                        "",
                        MasterUserDataSession.getCategoryUser()
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "Falló la conexión a Firebase",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Toast.makeText(
                    applicationContext,
                    "Conexión a Firebase exitosa",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (routingOptionSelected == "Back4App") {
                val connectionSuccessful = connectToBack4App()
                if (connectionSuccessful) {
                    logConnectionBack4App()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Conexión a Back4App fallida",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Selecciona una opción de ruteo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun setFirebaseRoutingData() {
        val editTextAppId = findViewById<EditText>(R.id.edit_app_id)
        val editTextSecondaryRoutingOption = findViewById<EditText>(R.id.edit_project_id)
        editTextAppId.setText(firebaseAppId)
        editTextSecondaryRoutingOption.setText(firebaseProjectId)
        routingOptionSelected = "Firebase"
    }

    private fun setBack4AppRoutingData() {
        val editTextAppId = findViewById<EditText>(R.id.edit_app_id)
        val editTextProjectId = findViewById<EditText>(R.id.edit_project_id)
        editTextAppId.setText(back4appAppId)
        editTextProjectId.setText(back4appClientKey)
        routingOptionSelected = "Back4App"
    }

    private fun clearRoutingData() {
        val editTextAppId = findViewById<EditText>(R.id.edit_app_id)
        val editTextProjectId = findViewById<EditText>(R.id.edit_project_id)
        editTextAppId.setText("")
        editTextProjectId.setText("")
        routingOptionSelected = ""
    }

    private fun connectToBack4App() : Boolean {
        try {
            val editTextAppId = findViewById<EditText>(R.id.edit_app_id)
            val editTextProjectId = findViewById<EditText>(R.id.edit_project_id)
            Parse.initialize(
                Parse.Configuration.Builder(this)
                    .applicationId(editTextAppId.text.toString())
                    .clientKey(editTextProjectId.text.toString())
                    .server(getString(R.string.back4app_server_url))
                    .build()
            )
        } catch (e :  Exception) {
            return false
        }

        return true
    }
    private fun logConnectionBack4App() {
        val logsCollection = ParseObject("logs")
        logsCollection.put("logEventType", Log.LogEventType.INFO.name)
        logsCollection.put("logEventName", Log.LogEventName.BACK4APP_SUCCESSFUL_CONNECTION.value)
        logsCollection.put("dniMasterUser", MasterUserDataSession.getDniUser())
        logsCollection.put("category", MasterUserDataSession.getCategoryUser())
        logsCollection.put("timestamp", Calendar.getInstance().time)

        logsCollection.saveInBackground { exception ->
            if (exception == null) {
                android.util.Log.e("Back4Apps", "Conexión a back4app exitosa")
                Toast.makeText(
                    applicationContext,
                    "Conexión a Back4App exitosa",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                android.util.Log.e("Back4Apps", "Fallo la conexión a back4app")
                Toast.makeText(
                    applicationContext,
                    "Conexión a Back4App fallida",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}
