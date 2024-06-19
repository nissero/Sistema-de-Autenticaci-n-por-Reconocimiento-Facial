package com.biogin.myapplication

import android.content.ContentValues
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.viewbinding.ViewBinding
import com.biogin.myapplication.face_detection.FaceContourDetectionProcessor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraHelper(private val typeOfAuthorization: ((Usuario) -> Unit)?,
                   private val lifecycleOwner: LifecycleOwner,
                   private val faceRecognitionActivity: FaceRecognitionActivity?,
                   private val viewBinding: ViewBinding,
                   private val surfaceProvider: SurfaceProvider,
                   private val graphicOverlay: GraphicOverlay,
                   private val dialogShowTime: Long?,
                   private var withAnalyzer: Boolean
) {

    private val firestore = FirebaseFirestore.getInstance()

    private var imageCapture: ImageCapture? = null
    private lateinit var imageAnalyzer: ImageAnalysis
    private var storageRef = FirebaseStorage.getInstance().getReference()

    private val apiCallIntervalMillis = 2000L
    private var lastApiCallTimeMillis = System.currentTimeMillis()
    private var isApiCallInProgress = false

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraNumber: Int = 1


    private val firebaseMethods = FirebaseMethods()

    private var currentPhotoIndex = 0

    fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        graphicOverlay.toggleSelector(cameraNumber)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewBinding.root.context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            startAnalyzer(withAnalyzer)

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

    fun flipCamera() {
        cameraProvider.unbindAll()

        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraNumber = 1
        } else {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            cameraNumber = 0
        }

        startCamera()
    }

    fun takePhoto(
        tag: String,
        fileNameFormat: String,
        context: ContextWrapper,
        intent: Intent,
        fin: () -> Unit
    ) {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(fileNameFormat, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(tag, "Photo capture failed: ${exc.message}", exc)
                    fin()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { photoUri ->
                        // Upload the photo to Firebase Storage
                        uploadPhotoToFirebase(photoUri, intent) { success ->
                            if (success) {
                                // If the photo was uploaded successfully, check if it's the third photo
                                if (currentPhotoIndex == 2) {
                                    // If it's the third photo, upload the request document to Firestore
                                    uploadRequestToFirestore(intent.getStringExtra("dni") ?: "", fin)
                                } else {
                                    // Increment the photo index
                                    currentPhotoIndex++
                                }
                            } else {
                                // Handle failure
                                Log.e(tag, "Failed to upload photo to Firebase Storage")
                                fin()
                            }
                        }
                    }
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(context.baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(tag, msg)
                    fin()
                }
            }
        )
    }

    fun uploadPhotoToFirebase(photo: Uri, intent: Intent, callback: (Boolean) -> Unit) {
        val dni = intent.getStringExtra("dni") ?: ""
        val imageName = "${dni}_${currentPhotoIndex}.jpg"
        val imageRef = storageRef.child("images/$dni/$imageName")

        val uploadTask = imageRef.putFile(photo)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // If the upload is successful, invoke the callback with true
                callback(true)
            } else {
                // If the upload fails, invoke the callback with false
                Log.e("Firebase", "Error uploading image: ${task.exception}")
                callback(false)
            }
        }
    }

   /* private fun sendImageForTraining(photoUri: Uri, dni: String, callback: (String?) -> Unit) {
        val context = viewBinding.root.context

        // Convert the photoUri to a Bitmap
        val imageBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(photoUri))

        // Rotate the bitmap based on the camera selector
        val rotatedBitmap = when (cameraSelector) {
            CameraSelector.DEFAULT_FRONT_CAMERA -> rotateBitmap(imageBitmap, -90f) // Rotate -90 degrees for front camera
            else -> rotateBitmap(imageBitmap, 90f) // Rotate 90 degrees for back camera or any other camera
        }

        // Convert the rotated bitmap to bytes
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        saveRequestToFirestore(imageBytes, dni)
    }

    */

    fun uploadRequestToFirestore(dni: String, fin: () -> Unit) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val data = hashMapOf(
            "dni" to dni,
            "timestamp" to timestamp,
            "photoPaths" to (0 until 3).map { index ->
                "images/$dni/${dni}_$index.jpg"
            },
            "processed" to false
        )

        firestore.collection("requests")
            .add(data)
            .addOnSuccessListener { documentReference ->
                Log.d("Firebase", "DocumentSnapshot added with ID: ${documentReference.id}")
                fin()
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "Error adding document", e)
                fin()
            }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun startAnalyzer(sendDataToAPI: Boolean) {
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        seleccionarAnalizador(sendDataToAPI)
    }

    private fun seleccionarAnalizador(sendDataToAPI: Boolean) {
        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            selectAnalyzer(sendDataToAPI, imageProxy).analyze(imageProxy)
        }
    }


    private fun selectAnalyzer(sendDataToAPI: Boolean, imageProxy: ImageProxy): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { image ->
            // Get the original image from the ImageProxy
            val originalImage = imageProxy.toBitmap() ?: return@Analyzer

            // Create a new instance of FaceContourDetectionProcessor
            val processor = FaceContourDetectionProcessor(
                graphicOverlay,
                originalImage,
                this,
                sendDataToAPI,
                cameraSelector
            )

            // Analyze the image using the processor
            processor.analyze(image)
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
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

    fun isAnalyzing(): Boolean{
        return isApiCallInProgress
    }
    fun analyzing(){
        isApiCallInProgress = true
    }

    fun stopAnalyzing(){
        isApiCallInProgress = false
    }

    fun wasAnalyzed(){
        imageAnalyzer.clearAnalyzer()

        if (dialogShowTime != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                seleccionarAnalizador(true)
            }, dialogShowTime)
        }
    }

    fun setLasApiCallTime(){
        lastApiCallTimeMillis = System.currentTimeMillis()
    }

    fun verifyUser(dni: String){
        firebaseMethods.readData(dni){ usuario ->
            typeOfAuthorization?.let { it(usuario) }
        }
    }
    fun shutdown(){
        cameraExecutor.shutdown()
    }

    fun isSomeoneAnalyzing(): Boolean{
        return isApiCallInProgress
    }

    fun iAmAnalyzing(){
        isApiCallInProgress = true
    }

    fun continueAnalyzing(){
        isApiCallInProgress = false
    }

    fun clearAnalyzer(){
        imageAnalyzer.clearAnalyzer()
    }

    companion object {
        private const val TAG = "CameraHelper"
    }
}