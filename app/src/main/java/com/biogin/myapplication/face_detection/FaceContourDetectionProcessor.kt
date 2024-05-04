package com.biogin.myapplication.face_detection

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Environment
import android.util.Log
import com.biogin.myapplication.BaseImageAnalyzer
import com.biogin.myapplication.FaceContourGraphic
import com.biogin.myapplication.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FaceContourDetectionProcessor(
    private val context: Context,
    private val view: GraphicOverlay,
    private val originalImage: Bitmap
) : BaseImageAnalyzer<List<Face>>() {

    private val realTimeOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOptions)
    private val client = OkHttpClient()

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "Exception thrown while closing Face Detector")
        }
    }

    override fun onSuccess(results: List<Face>, graphicOverlay: GraphicOverlay, rect: Rect) {
        graphicOverlay.clear()
        results.forEach { face ->
            val faceGraphic = FaceContourGraphic(graphicOverlay, face, rect)
            graphicOverlay.add(faceGraphic)
            // Save the extracted face bitmap
            //saveExtractedFaceBitmap(face, rect)
            // Send face image to API for recognition
            val faceBitmap = extractFaceBitmap(face, rect)
            sendImageForRecognition(faceBitmap)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(ContentValues.TAG, "Face Detector failed")
    }

    private fun extractFaceBitmap(face: Face, rect: Rect): Bitmap {
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val right = rect.right.coerceAtMost(originalImage.width)
        val bottom = rect.bottom.coerceAtMost(originalImage.height)

        // Create a matrix for rotation
        val matrix = Matrix()
        // Rotate the matrix by -90 degrees to the right
        matrix.postRotate(90f)

        // Create the rotated bitmap
        val rotatedBitmap = Bitmap.createBitmap(originalImage, left, top, right - left, bottom - top, matrix, true)

        // Return the rotated bitmap
        return rotatedBitmap
    }

/*    private fun saveExtractedFaceBitmap(face: Face, rect: Rect) {
        val faceBitmap = extractFaceBitmap(face, rect)
        val fileName = "face_${System.currentTimeMillis()}.jpg"

        // Specify the absolute path to the Downloads directory
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)

        try {
            // Create the Downloads directory if it doesn't exist
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            // Create a File object with the specified directory and file name
            val file = File(downloadsDir, fileName)

            // Write the bitmap to the file
            FileOutputStream(file).use { outputStream ->
                faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            Log.d(TAG, "Face bitmap saved: ${file.absolutePath}")
        } catch (e: Exception) {
            // Handle any exceptions that occur during file operations
            Log.e(TAG, "Error saving face bitmap: ${e.message}")
        }
    }*/

    private fun sendImageForRecognition(faceBitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "face.jpg", imageBytes.toRequestBody(MultipartBody.FORM))
            .build()

        val request = Request.Builder()
            .url("https://Biogin.pythonanywhere.com/recognize")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to make request: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Log recognition result
                Log.d(TAG, "Recognition result: $responseBody")
                // You can parse the response here and handle the recognized names
            }
        })
    }

    companion object {
        private const val TAG = "FaceContourProcessor"
    }
}
