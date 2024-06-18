package com.biogin.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.biogin.myapplication.R.*
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivityAuthorizationMessageBinding
import com.biogin.myapplication.local_data_base.OfflineDataBaseHelper
import com.biogin.myapplication.ui.admin.AdminActivity
import com.biogin.myapplication.ui.jerarquico.JerarquicoActivity
import com.biogin.myapplication.utils.DialogUtils

class AuthorizationMessageActivity : AppCompatActivity() {

    private lateinit var authorizationMessageTextView: TextView
    private lateinit var binding: ActivityAuthorizationMessageBinding
    private val logsRepository = LogsRepository()
    private val offlineDataBaseHelper = OfflineDataBaseHelper(this)
    private val dialogUtils = DialogUtils()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthorizationMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authorizationMessageTextView = findViewById(id.authorization_message)

        val buttonContinuar = binding.buttonContinuar
        val buttonIngreso = binding.buttonIngreso
        val buttonEgreso = binding.buttonEgreso

        // Get the authorization result, DNI, and apellido from the intent
        val authorizationResult = intent.getStringExtra("authorizationResult")
        val typeOfLogIn = intent.getStringExtra("typeOfLogIn")
        val connection = intent.getStringExtra("connection")
        val apellido = intent.getStringExtra("apellido")
        val dni = intent.getStringExtra("dni")

        val hasAreasTemporales = intent.getBooleanExtra("hasAreasTemporales", false)

        val areasTemporales = intent.getStringExtra("areasTemporales")
        val accesoDesde = intent.getStringExtra("accesoDesde")
        val accesoHasta = intent.getStringExtra("accesoHasta")


        val nombre = intent.getStringExtra("nombre")
        val categoria = intent.getStringExtra("categoria")
        val areasPermitidas = intent.getStringExtra("areasPermitidas")

        if(typeOfLogIn == "visitor" && authorizationResult == "authorized") {
            buttonContinuar.visibility = View.INVISIBLE
            buttonIngreso.visibility = View.VISIBLE
            buttonEgreso.visibility = View.VISIBLE
            onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    dialogUtils.showDialog(binding.root.context,
                        "Presione alguno de los botones (Ingreso/Egreso) para volver")
                }
            })
        } else {
            buttonContinuar.visibility = View.VISIBLE
            buttonIngreso.visibility = View.INVISIBLE
            buttonEgreso.visibility = View.INVISIBLE
            onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    dialogUtils.showDialog(binding.root.context,
                        "Presione el botón OK para continuar a la pantalla correpsondiente")
                }
            })
        }

        val message: SpannableString
//        val colorResource: Int

        if (authorizationResult == "authorized") {
            message = if (connection == "online") {
                if (hasAreasTemporales){
                    SpannableString("ACCESO AUTORIZADO\n" +
                            "DNI: $dni " +
                            "\n APELLIDO: $apellido " +
                            "\n NOMBRE: $nombre " +
                            "\n CATEGORIA: $categoria " +
                            "\n \n AREAS PERMITIDAS: \n $areasPermitidas" +
                            "\n \n AREAS TEMPORALES: \n $areasTemporales" +
                            "\n DESDE: $accesoDesde" +
                            "\n HASTA: $accesoHasta")
                } else {
                    SpannableString("ACCESO AUTORIZADO\n" +
                            "DNI: $dni \n APELLIDO: $apellido \n NOMBRE: $nombre \n CATEGORIA: $categoria \n AREAS PERMITIDAS: $areasPermitidas")
                }
            } else {
                SpannableString("ACCESO AUTORIZADO\n" +
                        "DNI: $dni \n APELLIDO: $apellido")
            }

            message.setSpan(ForegroundColorSpan(Color.GREEN), 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)  // Green for "ACCESO AUTORIZADO"
//            colorResource = color.black  // Black for details
        } else {
            message = SpannableString("ACCESO DENEGADO")
            message.setSpan(ForegroundColorSpan(Color.RED), 0, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Red for "ACCESO DENEGADO"
//            colorResource = color.black  // Black for details
        }

        authorizationMessageTextView.text = message
        authorizationMessageTextView.setTextColor(ContextCompat.getColor(this, color.black))

        buttonContinuar.setOnClickListener {
            if (authorizationResult == "authorized") {
                when(typeOfLogIn) {
                    "security" -> {
                        val dniMaster = intent.getStringExtra("dni")
                        val intent = Intent(this, SeguridadActivity::class.java)
                        intent.putExtra("dniMaster", dniMaster)
                        startActivity(intent)
                        finish()
                    }
                    "rrhh" -> {
                        val intent = Intent(this, RRHHActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    "admin" -> {
                        val intent = Intent(this, AdminActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    "jerarquico" -> {
                        val intent = Intent(this, JerarquicoActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                finish()
            }
        }

        buttonIngreso.setOnClickListener {
            if(connection == "online") {
                if (dni != null && categoria != null && nombre != null) {
                    logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO,
                        com.biogin.myapplication.logs.Log.LogEventName.USER_SUCCESSFUL_AUTHENTICATION_IN,
                        MasterUserDataSession.getDniUser(), dni, categoria)
                    Log.d(FaceRecognitionActivity.TAG, "Nombre del usuario: $nombre" +
                            " - CATEGORIA: $categoria")
                }
            } else {
                if(dni != null) {
                    offlineDataBaseHelper.registerLogForInAuthentication(dni, MasterUserDataSession.getDniUser())
                }
            }
            finish()
        }

        buttonEgreso.setOnClickListener {
            if(connection == "online") {
                if (dni != null && categoria != null && nombre != null) {
                        logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO,
                            com.biogin.myapplication.logs.Log.LogEventName.USER_SUCCESSFUL_AUTHENTICATION_OUT,
                            MasterUserDataSession.getDniUser(), dni, categoria)
                        Log.d(FaceRecognitionActivity.TAG, "Nombre del usuario: $nombre" +
                                " - CATEGORIA: $categoria")
                }
            } else {
                if(dni != null) {
                    offlineDataBaseHelper.registerLogForOutAuthentication(dni, MasterUserDataSession.getDniUser())
                }
            }
            finish()
        }
    }
}