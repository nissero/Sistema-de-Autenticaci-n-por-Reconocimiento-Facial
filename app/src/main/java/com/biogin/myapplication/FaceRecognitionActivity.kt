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
import android.view.View
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.biogin.myapplication.databinding.ActivityMainBinding
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.biogin.myapplication.ui.seguridad.autenticacion.AutenticacionFragment


class FaceRecognitionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var dialogShowTime = 10000L
    private var analyzeAgain = 12000L
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: CameraHelper
    private lateinit var authenticationType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        authenticationType = intent.getStringExtra("authenticationType").toString()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            when(authenticationType){
                "seguridad" -> initCamera(:: ifSecurity)
                "rrhh" -> initCamera(:: ifRRHH)
                "fin de turno" -> initCamera(:: ifFinDeTurno)
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
                "fin de turno" -> viewBinding.skipButton.setOnClickListener {
                    finDeTurno()
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

//        viewBinding.registerButton.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }

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

    private fun finDeTurno() {
        camera.shutdown()
        val intent = Intent(this@FaceRecognitionActivity,
            AutenticacionFragment::class.java
        )
        intent.putExtra("autenticado", true)

        setResult(RESULT_OK, intent)
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
            else -> initCamera(:: ifAny)
        }
    }

    private fun ifSecurity(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "seguridad") {
            this.showAuthorizationMessage(user)
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
            Handler(Looper.getMainLooper()).postDelayed({
                goToSeguridadActivity()
            }, dialogShowTime)
        } else {
            this.showAccessDeniedMessage()
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Seguridad")
        }
    }

    private fun ifRRHH(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "rrhh") {
            this.showAuthorizationMessage(user)
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
            Handler(Looper.getMainLooper()).postDelayed({
                goToRRHHActivity()
            }, dialogShowTime)
        } else {
            this.showAccessDeniedMessage()
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es RRHH")
        }
    }

    private fun ifFinDeTurno(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado() && user.getCategoria().lowercase() == "seguridad") {
            this.showAuthorizationMessage(user)
            Log.d("AUTORIZACION", "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
            Handler(Looper.getMainLooper()).postDelayed({
                finDeTurno()
            }, dialogShowTime)
        } else {
            this.showAccessDeniedMessage()
            Log.d("AUTORIZACION", "El usuario no existe en la base de datos/No es Seguridad")
        }
    }

    private fun ifAny(user: Usuario){
        if (user.getNombre().isNotEmpty() && user.getEstado()) {
            this.showAuthorizationMessage(user)
            Log.d(TAG, "Nombre del usuario: ${user.getNombre()} - CATEGORIA: ${user.getCategoria()}")
        } else {
            this.showAccessDeniedMessage()
            Log.d(TAG, "El usuario no existe en la base de datos")
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