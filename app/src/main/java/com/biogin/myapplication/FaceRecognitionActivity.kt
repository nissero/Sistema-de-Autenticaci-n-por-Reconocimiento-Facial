package com.biogin.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
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
    private var dialogShowTime = 10000L
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
                "seguridad" -> initCamera(:: ifSecurity)
                "rrhh" -> initCamera(:: ifRRHH)
                "admin" -> initCamera(:: ifAdmin)
                "jerarquico" -> initCamera(:: ifJerarquico)
                else -> initCamera(:: ifAny)
            }

            when(authenticationType) {
                "seguridad" ->
                    viewBinding.skipButton.setOnClickListener {
                        goToSeguridadActivity()
                    }
                "rrhh" -> viewBinding.skipButton.setOnClickListener {
                    goToRRHHActivity()
                }
                "admin" -> viewBinding.skipButton.setOnClickListener {
                    goToAdminActivity()
                }
                "jerarquico" -> viewBinding.skipButton.setOnClickListener {
                    goToJerarquicoActivity()
                }
                else -> {
                    viewBinding.skipButton.visibility = View.INVISIBLE
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

    @SuppressLint("SetTextI18n")
    fun showAuthorizationMessage(usuario: Usuario) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_authorization)

        // Configurar los datos del usuario en el diálogo
        dialog.findViewById<TextView>(R.id.textViewName).text = "Nombre: ${usuario.getNombre()}"
        dialog.findViewById<TextView>(R.id.textViewLastName).text = "Apellido: ${usuario.getApellido()}"
        dialog.findViewById<TextView>(R.id.textViewDNI).text = "DNI: ${usuario.getDni()}"
        dialog.findViewById<TextView>(R.id.textViewCategoria).text = "Categoria: ${usuario.getCategoria()}"
        dialog.findViewById<TextView>(R.id.textViewAreasPermitidas).text = "Areas Permitidas: ${usuario.getAreasPermitidas()}"

        Log.d(TAG, usuario.getAreasPermitidas())

        val mediaPlayer = MediaPlayer.create(this, R.raw.sound_authorization)
        mediaPlayer.start()

        dialog.window?.decorView?.findViewById<View>(android.R.id.content)?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        // Mostrar el diálogo por unos segundos y luego cerrarlo
        Handler().postDelayed({
            dialog.dismiss()
        }, dialogShowTime) // 3000 milisegundos (3 segundos)
    }

    @SuppressLint("SetTextI18n")
    fun showAccessDeniedMessage() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_access_denied)

        val mediaPlayer = MediaPlayer.create(this, R.raw.sound_denied)
        mediaPlayer.start()

        dialog.window?.decorView?.findViewById<View>(android.R.id.content)?.setOnClickListener {
            dialog.dismiss()
        }

        // Mostrar el diálogo por unos segundos y luego cerrarlo
        Handler().postDelayed({
            dialog.dismiss()
        }, dialogShowTime) // 3000 milisegundos (3 segundos)

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        when(authenticationType){
            "seguridad" -> initCamera(:: ifSecurity)
            "rrhh" -> initCamera(:: ifRRHH)
            "admin" -> initCamera(:: ifAdmin)
            else -> initCamera(:: ifAny)
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

    private fun ifAdmin(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "administrador") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.ADMIN_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())
            this.showAuthorizationMessage(user)
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
            Handler(Looper.getMainLooper()).postDelayed({
                goToAdminActivity()
            }, dialogShowTime)
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.ADMIN_UNSUCCESSFUL_LOGIN, user.getDni(), "", "")
            this.showAccessDeniedMessage()
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Admin")
        }
    }

    private fun ifJerarquico(user: Usuario) {
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "jerarquico") {
            MasterUserDataSession.setUserDataForSession(user.getDni(), user.getCategoria())
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.INFO, com.biogin.myapplication.logs.Log.LogEventName.HIERARCHICAL_SUCCESSFUL_LOGIN, user.getDni(), "", user.getCategoria())
            this.showAuthorizationMessage(user)
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
            Handler(Looper.getMainLooper()).postDelayed({
                goToJerarquicoActivity()
            }, dialogShowTime)
        } else {
            logsRepository.logEvent(com.biogin.myapplication.logs.Log.LogEventType.WARN, com.biogin.myapplication.logs.Log.LogEventName.HIERARCHICAL_UNSUCCESSFUL_LOGIN, user.getDni(), "", "")
            this.showAccessDeniedMessage()
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Jerárquico")
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