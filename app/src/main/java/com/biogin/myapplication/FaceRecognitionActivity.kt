package com.biogin.myapplication

import android.Manifest
import android.R.attr.text
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.biogin.myapplication.databinding.ActivityMainBinding
import com.biogin.myapplication.ui.seguridad.autenticacion.AutenticacionFragment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FaceRecognitionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var dialogShowTime = 10000L
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
            initCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons

//        viewBinding.registerButton.setOnClickListener {
//            startActivity(Intent(this, RegisterActivity::class.java))
//        }

        //saltearse la autenticacion y pasar a la activity como si se hubiera autenticado
        when(authenticationType) {
            "seguridad" -> viewBinding.skipButton.setOnClickListener {
                val intent = Intent(this, SeguridadActivity::class.java)
                startActivity(intent)
                finish()
            }
            "rrhh" -> viewBinding.skipButton.setOnClickListener {
                val intent = Intent(this, RRHHActivity::class.java)
                startActivity(intent)
                finish()
            }
            "fin de turno" -> viewBinding.skipButton.setOnClickListener {
                val intent = Intent(this@FaceRecognitionActivity,
                    AutenticacionFragment::class.java
                )
                intent.putExtra("autenticado", true)

                setResult(RESULT_OK, intent)
                finish()
            }
            else -> {
                viewBinding.skipButton.visibility = View.INVISIBLE
                viewBinding.skipButton.isClickable = false
            }

        }

        viewBinding.switchCameraButton.setOnClickListener {
            camera.flipCamera()
        }
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
        initCamera()
    }

    private fun initCamera(){
        camera = CameraHelper(this, this, viewBinding, viewBinding.viewFinder.surfaceProvider, viewBinding.graphicOverlayFinder, dialogShowTime, true)
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