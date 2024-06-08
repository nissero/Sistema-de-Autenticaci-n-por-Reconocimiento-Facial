package com.biogin.myapplication.face_detection

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.CameraSelector
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
    private val originalImage: Bitmap,
    private val cameraSelector: CameraSelector
) {
    private val client = OkHttpClient()

    fun sendImageForRecognition(faceBitmap: Bitmap, onResult: (String) -> Unit) {
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
                onResult(null.toString()) // Indicar que no se detectó una cara
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    if (isValidRecognitionResult(responseBody)){
                        val cleanDNI = extractNameFromResponse(responseBody)

                        Log.d(TAG, "$responseBody")
                        Log.d(TAG, "RESULTADO LIMPIO: $cleanDNI")

                        Log.d("LOGIN", "API RESPONDIO")
                        onResult(cleanDNI) // Devolver el resultado de la detección de cara
                    }

                    else {
                        Log.e(TAG, "ERROR EN LA API: \n $responseBody")
                        onResult(null.toString())
                    }


                } else {
                    Log.d(TAG, "Response body is null")
                    onResult(null.toString()) // Indicar que no se detectó una cara
                }
            }
        })
    }

    fun extractNameFromResponse(responseBody: String): String {
        // If the response contains "Unknown", return "Unknown"
        if (responseBody.contains("Unknown")) {
            return "Unknown"
        }
        // If the response contains the names format, extract the content within the brackets
        else if (responseBody.contains("{\"names\":")) {
            val startIndex = responseBody.indexOf('[')
            val endIndex = responseBody.indexOf(']')
            // Extract the content within the brackets
            val nameContent = responseBody.substring(startIndex + 1, endIndex)
            // Split the names by comma and return the first name
            val names = nameContent.split(",")
            return names.first().replace(" ", "").replace("\"", "")
        }
        // If none of the conditions above are met, return "Unknown"
        return "Unknown"
    }

    private fun isValidRecognitionResult(responseBody: String): Boolean {
        return responseBody.contains("{\"names\":[\"") && !responseBody.contains("{\"names\":[]}")
    }
    fun extractFaceBitmap(rect: Rect): Bitmap {
        val left = rect.left.coerceAtLeast(0)
        val top = rect.top.coerceAtLeast(0)
        val right = rect.right.coerceAtMost(originalImage.width)
        val bottom = rect.bottom.coerceAtMost(originalImage.height)

        // Determine rotation angle based on camera facing direction
        val rotationAngle = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            -90f // Rotate -90 degrees for front camera
        } else {
            90f // Rotate 90 degrees for back camera
        }

        // Create a matrix for rotation
        val matrix = Matrix()
        // Rotate the matrix
        matrix.postRotate(rotationAngle)

        // Create the rotated bitmap
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