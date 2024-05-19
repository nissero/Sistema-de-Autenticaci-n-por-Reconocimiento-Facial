package com.example.bioginx_barras

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import java.time.LocalDate
import java.time.LocalTime


class MainActivity : AppCompatActivity() {

    private companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 200
    }

    private var toRegister: Boolean = false
    private var result: Cursor? = null
    private lateinit var resultTextView: TextView
    private lateinit var database: SQLiteDatabase
    private lateinit var closeButton: Button
    private var localSurname: String = ""
    private var localDNI: String = ""
    private var localData: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainactivity)

        closeButton = findViewById(R.id.close_button)
        resultTextView = findViewById(R.id.result_text_view)
        val scanButton: Button = findViewById(R.id.scan_button)
        val registerButton: Button = findViewById(R.id.register_button)
        val deleteButton: Button = findViewById(R.id.delete_button)

        // Iniciar DB
        database = openOrCreateDatabase("BarcodeDB", MODE_PRIVATE, null)
        database.execSQL("CREATE TABLE IF NOT EXISTS Barcodes(id INTEGER PRIMARY KEY AUTOINCREMENT,data TEXT)")
        database.execSQL("CREATE TABLE IF NOT EXISTS Users(id INTEGER PRIMARY KEY AUTOINCREMENT,dni TEXT,apellido TEXT)")
        database.execSQL("CREATE TABLE IF NOT EXISTS OfflineLogs(id INTEGER PRIMARY KEY AUTOINCREMENT,tipo TEXT,dni TEXT,apellido TEXT,fecha DATE,hora TIME)")

        // Comprobar permisos de camara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        scanButton.setOnClickListener {
            // Iniciar escaner para inicio
            val integrator = IntentIntegrator(this@MainActivity)
            integrator.setPrompt("Acerca el codigo del DNI")
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        registerButton.setOnClickListener {
            // Iniciar escaner para registro
            toRegister = true
            val integrator = IntentIntegrator(this@MainActivity)
            integrator.setPrompt("Acerca el codigo del DNI")
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        deleteButton.setOnClickListener {
            cleanDatabase()
        }

        closeButton.setOnClickListener {
            registerLog("Cierre Sesion Offline",localData)
            closeButton.setEnabled(false)
            localSurname=""
            localDNI=""
            localData=""
        }
    }

    // Permisos de camara
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

    // Escaneo del codigo de barras
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(toRegister) {
            registerUser(requestCode, resultCode, data)
            toRegister=false
        }
        else {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
                } else {
                    if (checkDataInDatabase(result.contents.toString())) {
                        // Display scanned barcode data
                        registerLog("Inicio Sesion Offline",result.contents.toString())
                        localData = result.contents.toString()
                        resultTextView.text = "Hola: ${localSurname}, Bienvenido al sistema"
                        closeButton.setEnabled(true)
                    } else {
                        // Display scanned barcode data
                        resultTextView.text = "No se ha encontrado ningun usuario con estos datos"
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun registerUser(requestCode: Int, resultCode: Int, data: Intent?){
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && !checkDataInDatabase(result.contents.toString())) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_LONG).show()
            } else {
                saveDataToDatabase(result.contents)
                resultTextView.text = "Registro exitoso"
            }
        }
        else {
            resultTextView.text = "Registro fallido el usuario ya esta registrado o el codigo no es valido"
        }
    }

    // Metodos de la DB
    private fun checkDataInDatabase(data: String): Boolean {
        result = database.rawQuery("SELECT 1 FROM Users WHERE dni='${getDNINumber(data)}'", null)
        val exists = result?.moveToFirst() ?: false
        return exists
    }

    private fun saveDataToDatabase(data: String) {
        database.execSQL("INSERT INTO Barcodes(data) VALUES('$data')")
        database.execSQL("INSERT INTO Users(dni,apellido) VALUES('${getDNINumber(data)}','${getDNISurname(data)}')")
    }

    private fun cleanDatabase() {
        database.execSQL("DELETE FROM Barcodes")
        database.execSQL("DELETE FROM Users")
        resultTextView.text = "Base de datos borrada"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerLog(type: String,data: String) {
        database.execSQL("INSERT INTO OfflineLogs(tipo,dni,apellido,fecha,hora) VALUES('$type','${getDNINumber(data)}','${getDNISurname(data)}','${currentDate()}','${currentTime()}')")
    }

    // Metodos para conseguir datos
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
        localDNI = result
        return result
    }

    private fun getDNISurname(data: String): String {
        var result = ""
        var count = 0
        for (char in data) {
            if (char == '@') {
                count+=1
            }
            if (count==1 && char != '@'){
                result += char
            }
        }
        localSurname = result
        return "Veron"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun currentDate(): LocalDate {
        return LocalDate.now()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun currentTime(): LocalTime {
        return LocalTime.now()
    }

}
