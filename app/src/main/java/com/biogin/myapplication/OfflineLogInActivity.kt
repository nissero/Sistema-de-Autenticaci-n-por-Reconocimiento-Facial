package com.biogin.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.google.zxing.integration.android.IntentIntegrator

class OfflineLogInActivity : AppCompatActivity() {
    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 200
        const val TAG = "OfflineLoginActivity"
    }

    private lateinit var dniMaster: String
    private lateinit var authenticationType: String
    private lateinit var integrator: IntentIntegrator
    private lateinit var database: OfflineDataBaseHelper
    //private lateinit var resultTextView: TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_offline_log_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = OfflineDataBaseHelper(this)

        authenticationType = intent.getStringExtra("authenticationType").toString()
        dniMaster = intent.getStringExtra("dniMaster").toString()

        Log.d(TAG, "DNI MASTER INICIO DE TURNO: $dniMaster")

        integrator = IntentIntegrator(this@OfflineLogInActivity)

        integrator.setPrompt("Acerca el codigo del DNI")
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null){
            if (result.contents == null){
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
                finish()
            } else {
                val apellido = getDNISurname(result.contents.toString())
                val dni = getDNINumber(result.contents.toString())
                when(authenticationType){
                    "seguridad" -> securityScan(dni, apellido)
                    else -> noCategoryScan(dni, apellido)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun securityScan(dni: String, apellido: String) {
        if (database.registerSecurityAuthenticationLog(dni)){
            Log.d(TAG, "ENCONTRADO SEGURIDAD - APELLIDO: $apellido")
            Log.d(TAG, "ENCONTRADO SEGURIDAD - DNI: $dni")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("dni", dni)
            intent.putExtra("typeOfLogIn", "security")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("apellido", apellido)
            intent.putExtra("connection", "offline")
            startActivity(intent)

            finish()
        } else {
            Log.e(TAG, "ENCONTRADO - APELLIDO: $apellido")
            Log.e(TAG, "ENCONTRADO - DNI: $dni")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "security")
            intent.putExtra("authorizationResult", "denied")
            intent.putExtra("connection", "offline")
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun noCategoryScan(dni: String, apellido: String) {
        if (database.registerAuthenticationLog(dni, dniMaster)) {
            Log.d(TAG, "ENCONTRADO - APELLIDO: $apellido")

            Log.d(TAG, "ENCONTRADO - DNI: $dni")
            Log.d(TAG, "MASTER DNI: $dniMaster")

            Log.d(TAG, "RESULTADO DE TODOS LOS LOGS: ${database.getAllLogs()}")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("typeOfLogIn", "visitor")
            intent.putExtra("dni", dni)
            intent.putExtra("apellido", apellido)

            startActivity(intent)

            finish()
        } else {
            Log.e(TAG, "USUARIO NO REGISTRADO EN LA BASE DE DATOS")

            Log.d(TAG, "RESULTADO DE TODOS LOS LOGS: ${database.getAllLogs()}")

            Log.e(TAG, "ENCONTRADO - APELLIDO: $apellido")
            Log.e(TAG, "ENCONTRADO - DNI: $dni")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("authorizationResult", "denied")

            startActivity(intent)
            finish()
        }
    }

    private fun getDNISurname(data:String): String {
        var result = ""
        var count = 0
        for (char in data){
            if (char == '@'){
                count+=1
            }
            if (count == 1 && char != '@'){
                result += char
            }
        }
        return result
    }

    private fun getDNINumber(data: String): String {
        var result = ""
        var count = 0
        for (char in data) {
            if (char == '@') {
                count+=1
            }
            if (count==4 && char != '@'){
                result += char
            }
        }
        return result
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de camara validos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permisos de camara invalidos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}