package com.biogin.myapplication.face_detection

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Environment
import android.os.Handler
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
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.os.Looper
import androidx.camera.core.CameraSelector
import com.biogin.myapplication.CameraHelper
import java.io.File
import java.io.FileOutputStream

class FaceContourDetectionProcessor(
    private val context: Context,
    private val view: GraphicOverlay,
    private val originalImage: Bitmap,
    private val camera: CameraHelper,
    private val sendDataToAPI: Boolean,
    private val cameraSelector: CameraSelector
) : BaseImageAnalyzer<List<Face>>() {
    private val apiManager = APIManager(originalImage, cameraSelector)

    private val realTimeOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOptions)

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


            if (sendDataToAPI && isFaceComplete(face, rect)){
                val faceGraphic = FaceContourGraphic(graphicOverlay, face, rect)
                graphicOverlay.add(faceGraphic)

                //Log.d(TAG, "se detecto una cara")
                val faceBitmap = apiManager.extractFaceBitmap(rect)

                if (camera.canAnalize() && !camera.isAnalyzing()){
                    camera.analyzing()

                    Log.d(TAG, "SE LLAMO A LA API")
                    saveExtractedFaceBitmap(face, rect)
                    apiManager.sendImageForRecognition(faceBitmap) { faceDetected ->
                        if (faceDetected != "null") {
                            Log.d(TAG, "CARA DETECTADA: $faceDetected")
                            camera.verifyUser(faceDetected)
                            camera.wasAnalyzed()
                        } else {
                            Log.e(TAG, "ERROR EN LA API")
                        }
                    }

                    camera.setLasApiCallTime()
                    camera.stopAnalyzing()
                }
            }

        }
        graphicOverlay.postInvalidate()
    }

    private fun isFaceComplete(face: Face, roi: Rect): Boolean {
        val faceContours = face.allContours

        for (contour in faceContours){
            for (point in contour.points){
                if (!roi.contains(point.x.toInt(), point.y.toInt())) {
                    // Si alguno de los puntos del contorno facial está fuera de la ROI, la cara no está completa
                    return false
                }
            }
        }
        return true
    }

    private fun saveExtractedFaceBitmap(face: Face, rect: Rect) {
        val faceBitmap = apiManager.extractFaceBitmap(rect)
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
    }

    override fun onFailure(e: Exception) {
        Log.w(ContentValues.TAG, "Face Detector failed")
    }

    companion object {
        private const val TAG = "FaceContourProcessor"
    }
}
