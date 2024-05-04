package com.biogin.myapplication

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.biogin.myapplication.face_detection.FaceContourDetectionProcessor
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
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
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        selectAnalyzer(imageProxy).analyze(imageProxy)
                    }
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
            val imageRef = storageRef.child("images/${intent.getStringExtra("dni")}/${intent.getStringExtra("name") + "_" + intent.getStringExtra("surname")}")
            val uploadTask = imageRef.putFile(photo)
            uploadTask.addOnFailureListener {
                Log.e("Firebase", "Error al subir imagen")
            }.addOnSuccessListener {
                Log.e("Firebase", "Exito al subir imagen")
            }
        }
    }

    private fun selectAnalyzer(imageProxy: ImageProxy): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { image ->
            // Get the original image from the ImageProxy
            val originalImage = imageProxy.toBitmap() ?: return@Analyzer

            // Create a new instance of FaceContourDetectionProcessor
            val processor = FaceContourDetectionProcessor(
                context = viewBinding.root.context,
                graphicOverlay,
                originalImage = originalImage
            )

            // Analyze the image using the processor
            processor.analyze(image)
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
}