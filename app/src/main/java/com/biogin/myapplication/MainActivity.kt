package com.biogin.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceLandmark
import java.io.ByteArrayOutputStream

typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
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
    private fun showAuthorizationMessage(usuario: Usuario) {
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

        // Mostrar el diálogo por unos segundos y luego cerrarlo
        Handler().postDelayed({
            dialog.dismiss()
        }, 3000) // 3000 milisegundos (3 segundos)

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showAccessDeniedMessage() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_access_denied)
        
    private fun selectAnalyzer(originalImage: Bitmap): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(this, viewBinding.graphicOverlayFinder, originalImage)
    }

    private inner class ImageAnalyzer : ImageAnalysis.Analyzer {
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val originalImage = imageProxy.toBitmap() ?: return
            val analyzer = selectAnalyzer(originalImage)
            analyzer.analyze(imageProxy)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }
            
        val mediaPlayer = MediaPlayer.create(this, R.raw.sound_denied)
        mediaPlayer.start()

        // Mostrar el diálogo por unos segundos y luego cerrarlo
        Handler().postDelayed({
            dialog.dismiss()
        }, 3000) // 3000 milisegundos (3 segundos)

        dialog.show()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val originalImage = imageProxy.toBitmap() ?: return@setAnalyzer
                val analyzer = selectAnalyzer(originalImage)
                analyzer.analyze(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis)
            } catch (e: Exception) {
                Log.e(TAG, "startCamera: $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResume() {
        super.onResume()
        initCamera()
    }

    private fun initCamera(){
        camera = CameraHelper(this, cameraExecutor, viewBinding, viewBinding.viewFinder.surfaceProvider, viewBinding.graphicOverlayFinder)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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