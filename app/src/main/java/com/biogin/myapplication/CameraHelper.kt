package com.biogin.myapplication

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.biogin.myapplication.face_detection.FaceContourDetectionProcessor
import com.google.firebase.storage.FirebaseStorage
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import kotlin.coroutines.CoroutineContext

class CameraHelper(private val lifecycleOwner: LifecycleOwner,
                   private val cameraExecutor: ExecutorService,
                   private val viewBinding: ViewBinding,
                   private val surfaceProvider: SurfaceProvider,
                   private val graphicOverlay: GraphicOverlay
)  {


    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var storageRef = FirebaseStorage.getInstance().getReference()

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewBinding.root.context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, selectAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(viewBinding.root.context))
    }

    fun takePhoto(tag: String, fileNameFormat: String, context: ContextWrapper, intent: Intent, fin: () -> Unit){
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(fileNameFormat, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply{
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(tag, "Photo capture failed: ${exc.message}", exc)
                    fin()
                }
                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    uploadPhotoToFirebase(output.savedUri, intent)
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(context.baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(tag, msg)
                    fin()
                }
            }
        )
    }

    fun uploadPhotoToFirebase(photo: Uri?, intent: Intent){
        if (photo != null) {
            var imageRef = storageRef.child("images/${intent.getStringExtra("dni")}/${intent.getStringExtra("name") + "_" + intent.getStringExtra("surname")}")
            var uploadTask = imageRef.putFile(photo)
            uploadTask.addOnFailureListener {
                Log.e("Firebase", "Error al subir imagen")
            }.addOnSuccessListener {
                Log.e("Firebase", "Exito al subir imagen")
            }
        }
    }

    private fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(graphicOverlay)
    }
}