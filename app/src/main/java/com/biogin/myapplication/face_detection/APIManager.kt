package com.biogin.myapplication.face_detection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class APIManager(
    private val originalImage: Bitmap
) {
    private val client = OkHttpClient()

    fun sendImageForRecognition(faceBitmap: Bitmap, onResult: (Boolean) -> Unit) {
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
                onResult(false) // Indicar que no se detectó una cara
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    Log.d(TAG, "Recognition result: $responseBody")
                    val faceDetected = isValidRecognitionResult(responseBody)
                    onResult(faceDetected) // Devolver el resultado de la detección de cara
                } else {
                    Log.d(TAG, "Response body is null")
                    onResult(false) // Indicar que no se detectó una cara
                }
            }
        })
    }

    private fun isValidRecognitionResult(responseBody: String): Boolean {
        return responseBody.contains("Unknown") || responseBody.contains("{\"names\":")
    }
    fun extractFaceBitmap(rect: Rect): Bitmap {
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val right = rect.right.coerceAtMost(originalImage.width)
        val bottom = rect.bottom.coerceAtMost(originalImage.height)

        // Create a matrix for rotation
        val matrix = Matrix()
        // Rotate the matrix by -90 degrees to the right
        matrix.postRotate(90f)

        // Create the rotated bitmap

        // Return the rotated bitmap
        return Bitmap.createBitmap(
            originalImage,
            left,
            top,
            right - left,
            bottom - top,
            matrix,
            true
        )
    }
    companion object {
        private const val TAG = "APIManager"
    }
}