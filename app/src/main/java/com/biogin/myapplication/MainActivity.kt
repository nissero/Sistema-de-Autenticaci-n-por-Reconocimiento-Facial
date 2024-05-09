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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Log
import android.view.Window
import android.widget.TextView
import com.biogin.myapplication.databinding.ActivityMainBinding
import com.biogin.myapplication.ui.login.RegisterActivity

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private var dialogShowTime = 10000L
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: CameraHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            initCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewBinding.pruebaButton.setOnClickListener{
            buttonPrueba()
        }
    }

    private fun buttonPrueba() {
        val firebaseMethods = FirebaseMethods()
        val dniPrueba = "123" //esta valor de esta variable tiene que salir de la API
        //en caso de que no se reconozca a la persona en la api aka resultado = unkown
        //se llamaria directo a showAccessDeniedMessage
        firebaseMethods.readData(dniPrueba) { usuario ->
            if (usuario.getNombre().isNotEmpty()) {
                showAuthorizationMessage(usuario)
                Log.d("Firestore", "Nombre del usuario: ${usuario.getNombre()}")
            } else {
                showAccessDeniedMessage() //esto se podría dejar en un caso extremo de que la persona sea reconocida
                //por la api pero no este en la base de datos ???
                //no se si podria llegar a pasar
                Log.d("Firestore", "El usuario no existe en la base de datos")
            }
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
        camera = CameraHelper(this, this, cameraExecutor, viewBinding, viewBinding.viewFinder.surfaceProvider, viewBinding.graphicOverlayFinder, dialogShowTime, true)
        camera.startCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val TAG = "Sistema de Autenticación Facial"
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