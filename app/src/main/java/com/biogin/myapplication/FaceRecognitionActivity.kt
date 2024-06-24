package com.biogin.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.biogin.myapplication.data.LogsRepository
import com.biogin.myapplication.data.userSession.MasterUserDataSession
import com.biogin.myapplication.databinding.ActivityMainBinding
import com.biogin.myapplication.ui.admin.AdminActivity
import com.biogin.myapplication.ui.jerarquico.JerarquicoActivity
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FaceRecognitionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var analyzeAgain = 12000L
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: CameraHelper
    private lateinit var authenticationType: String
    private lateinit var logsRepository : LogsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        logsRepository = LogsRepository()

        authenticationType = intent.getStringExtra("authenticationType").toString()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            when(authenticationType){
                "seguridad" -> {
                    initCamera(:: ifSecurity)
                }
                "rrhh" -> {
                    initCamera(:: ifRRHH)
                }
                "jerarquico" -> {
                    initCamera(:: ifJerarquico)
                }
                "admin" -> {
                    initCamera(:: ifAdmin)
                }
                else -> {
                    initCamera(:: ifAny)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons

        viewBinding.switchCameraButton.setOnClickListener {
            camera.flipCamera()
        }
    }

    private fun goToRRHHActivity() {
        camera.shutdown()
        val intent = Intent(this, RRHHActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToSeguridadActivity() {
        camera.shutdown()
        val intent = Intent(this, SeguridadActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToAdminActivity() {
        camera.shutdown()
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToJerarquicoActivity() {
        camera.shutdown()
        val intent = Intent(this, JerarquicoActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        when(authenticationType){
            "seguridad" -> {
                initCamera(:: ifSecurity)
            }
            "rrhh" -> {
                initCamera(:: ifRRHH)
            }
            "jerarquico" -> {
                initCamera(:: ifJerarquico)
            }
            "admin" -> {
                initCamera(:: ifAdmin)
            }
            else -> {
                initCamera(:: ifAny)
            }
        }
    }

    private fun ifSecurity(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "seguridad") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.SECURITY_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())

            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)

            //USER DATA
            intent.putExtra("dni", user.getDni())
            intent.putExtra("apellido", user.getApellido())
            intent.putExtra("nombre", user.getNombre())
            intent.putExtra("categoria", user.getCategoria())
            intent.putExtra("areasPermitidas", user.getAreasPermitidas())

            //LOGIN DATA
            intent.putExtra("typeOfLogIn", "security")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("connection", "online")

            startActivity(intent)

            camera.shutdown()
            finish()
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.SECURITY_UNSUCCESSFUL_LOGIN, MasterUserDataSession.getDniUser(), "", "")
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Seguridad")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "security")
            intent.putExtra("authorizationResult", "denied")
            startActivity(intent)

            camera.shutdown()
            finish()
        }
    }

    private fun ifRRHH(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "rrhh") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.RRHH_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)

            //USER DATA
            intent.putExtra("dni", user.getDni())
            intent.putExtra("apellido", user.getApellido())
            intent.putExtra("nombre", user.getNombre())
            intent.putExtra("categoria", user.getCategoria())
            intent.putExtra("areasPermitidas", user.getAreasPermitidas())

            //LOGIN DATA
            intent.putExtra("typeOfLogIn", "rrhh")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("connection", "online")

            startActivity(intent)

            camera.shutdown()
            finish()
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.RRHH_UNSUCCESSFUL_LOGIN, MasterUserDataSession.getDniUser(), "", "")
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es RRHH")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "rrhh")
            intent.putExtra("authorizationResult", "denied")
            startActivity(intent)

            camera.shutdown()
            finish()
        }
    }

    private fun ifAdmin(user: Usuario) {
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "administrador") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.ADMIN_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)

            //USER DATA
            intent.putExtra("dni", user.getDni())
            intent.putExtra("apellido", user.getApellido())
            intent.putExtra("nombre", user.getNombre())
            intent.putExtra("categoria", user.getCategoria())
            intent.putExtra("areasPermitidas", user.getAreasPermitidas())

            //LOGIN DATA
            intent.putExtra("typeOfLogIn", "admin")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("connection", "online")

            startActivity(intent)

            camera.shutdown()
            finish()

        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.ADMIN_UNSUCCESSFUL_LOGIN, user.getDni(), "", "")
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Admin")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "admin")
            intent.putExtra("authorizationResult", "denied")
            startActivity(intent)

            camera.shutdown()
            finish()
        }
    }

    private fun ifJerarquico(user: Usuario) {
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "jerarquico") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.HIERARCHICAL_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)

            //USER DATA
            intent.putExtra("dni", user.getDni())
            intent.putExtra("apellido", user.getApellido())
            intent.putExtra("nombre", user.getNombre())
            intent.putExtra("categoria", user.getCategoria())
            intent.putExtra("areasPermitidas", user.getAreasPermitidas())

            //LOGIN DATA
            intent.putExtra("typeOfLogIn", "jerarquico")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("connection", "online")

            startActivity(intent)

            camera.shutdown()
            finish()
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.HIERARCHICAL_UNSUCCESSFUL_LOGIN, user.getDni(), "", "")
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Jerárquico")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "jerarquico")
            intent.putExtra("authorizationResult", "denied")
            startActivity(intent)

            camera.shutdown()
            finish()
        }
    }

    private fun ifAny(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado()) {
            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("dni", user.getDni())
            intent.putExtra("apellido", user.getApellido())
            intent.putExtra("nombre", user.getNombre())
            intent.putExtra("categoria", user.getCategoria())
            intent.putExtra("areasPermitidas", user.getAreasPermitidas())

            if(user.hasAreasTemporales()){
                intent.putExtra("hasAreasTemporales", true)
                intent.putExtra("areasTemporales", user.getAreasTemporales())
                intent.putExtra("accesoDesde", user.getAccesoDesde())
                intent.putExtra("accesoHasta", user.getAccesoHasta())
            } else{
                intent.putExtra("hasAreasTemporales", false)
            }

            //LOGIN DATA
            intent.putExtra("typeOfLogIn", "visitor")
            intent.putExtra("authorizationResult", "authorized")
            intent.putExtra("connection", "online")

            startActivity(intent)
            camera.shutdown()
            finish()
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.USER_UNSUCCESSFUL_AUTHENTICATION,
                dniMasterUser = MasterUserDataSession.getDniUser(), "", "")
            Log.d(TAG, "El usuario no existe en la base de datos")

            val intent = Intent(this, AuthorizationMessageActivity::class.java)
            intent.putExtra("typeOfLogIn", "visitor")
            intent.putExtra("authorizationResult", "denied")
            startActivity(intent)

            camera.shutdown()
            finish()
        }
    }

    private fun initCamera(typeOfAuthorization: (Usuario) -> Unit){
        camera = CameraHelper(typeOfAuthorization, this, this, viewBinding, viewBinding.viewFinder.surfaceProvider, viewBinding.graphicOverlayFinder, analyzeAgain, true)
        camera.startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.shutdown()
    }

    companion object {
        const val TAG = "FaceRecognitionActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}