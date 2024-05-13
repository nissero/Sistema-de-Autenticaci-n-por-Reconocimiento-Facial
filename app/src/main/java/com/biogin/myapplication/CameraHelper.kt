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
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
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
)  {

    private var imageCapture: ImageCapture? = null
    private lateinit var imageAnalyzer: ImageAnalysis
    private var storageRef = FirebaseStorage.getInstance().getReference()

    private val apiCallIntervalMillis = 2000L
    private var lastApiCallTimeMillis = System.currentTimeMillis()
    private var isApiCallInProgress = false

    private lateinit var cameraExecutor : ExecutorService

    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var cameraNumber: Int = 1


    val firebaseMethods = FirebaseMethods()

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

    fun flipCamera(){
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
                    output.savedUri?.let {
                        sendImageForTraining(it, intent.getStringExtra("dni") ?: "") { result ->
                            if (result != null) {
                                Log.d(TAG, "Training result: $result")
                            } else {
                                Log.e(TAG, "Failed to make request")
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

    fun uploadPhotoToFirebase(photo: Uri?, intent: Intent) {
        if (photo != null) {
            val imageRef = storageRef.child("images/${intent.getStringExtra("dni")}/${intent.getStringExtra("name") + "_" + intent.getStringExtra("surname")}")
            val uploadTask = imageRef.putFile(photo)
            uploadTask.addOnFailureListener {
                Log.e("Firebase", "Error al subir imagen")
            }.addOnSuccessListener {
                Log.d("Firebase", "Exito al subir imagen")
                val dni = intent.getStringExtra("dni") ?: ""
                Log.d("DNI: ", "dni: $dni") // Log the dni value
            }
        }
    }

    private fun sendImageForTraining(photoUri: Uri, dni: String, callback: (String?) -> Unit) {
        val context = viewBinding.root.context

        // Convert the photoUri to a Bitmap
        val imageBitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(photoUri))

        // Rotate the bitmap if needed
        val rotatedBitmap = rotateBitmap(imageBitmap, 90f)

        // Convert the rotated bitmap to bytes
        val outputStream = ByteArrayOutputStream()
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Build the request body with the image bytes
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "face.jpg", imageBytes.toRequestBody(MultipartBody.FORM))
            .addFormDataPart("name", dni)
            .build()

        // Build the request with the request body
        val request = Request.Builder()
            .url("https://Biogin.pythonanywhere.com/train")
            .post(requestBody)
            .build()

        // Execute the request asynchronously
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to make request: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Log training result
                Log.d(TAG, "Training result: $responseBody")
                callback(responseBody)
            }
        })
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
                viewBinding.root.context,
                graphicOverlay,
                originalImage,
                this,
                sendDataToAPI
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
    fun canAnalize(): Boolean{
        return System.currentTimeMillis() - lastApiCallTimeMillis >= apiCallIntervalMillis
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

    companion object {
        private const val TAG = "CameraHelper"
    }
}