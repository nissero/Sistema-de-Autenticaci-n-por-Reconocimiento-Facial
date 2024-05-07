package com.biogin.myapplication.face_detection

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
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
import android.os.Handler
import android.os.Looper

class FaceContourDetectionProcessor(
    private val context: Context,
    private val view: GraphicOverlay,
    private val originalImage: Bitmap
) : BaseImageAnalyzer<List<Face>>() {

    private val apiCallIntervalMillis = 17L
    private var lastApiCallTimeMillis = System.currentTimeMillis()
    private var isApiCallInProgress = false
    private val apiLock = Any()
    private val currentThreadName: String
        get() = Thread.currentThread().name

    private val apiManager = APIManager(originalImage)

    private val realTimeOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(realTimeOptions)

    private val handler = Handler(Looper.getMainLooper())
    private val DELAY_MILLISECONDS = 1000 // Adjust the delay time as needed

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
            val currentTimeMillis = System.currentTimeMillis()

            if (isFaceComplete(face, rect)){
                synchronized(apiLock){
                    if (!isApiCallInProgress && currentTimeMillis - lastApiCallTimeMillis >= apiCallIntervalMillis) {
                        isApiCallInProgress = true

                        val faceBitmap = apiManager.extractFaceBitmap(rect)
                        Log.d(TAG, "SE LLAMO A LA API")
                        apiManager.sendImageForRecognition(faceBitmap) { faceDetected ->
                            if (faceDetected != "null") {
                                Log.d(TAG, "CARA DETECTADA: $faceDetected")
                            } else {
                                Log.e(TAG, "ERROR EN LA API")
                            }
                        }

                        lastApiCallTimeMillis = currentTimeMillis
                        isApiCallInProgress = false
                    }
                }
            }

            val faceBitmap = extractFaceBitmap(face, rect)
            sendImageForRecognition(faceBitmap)
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

    override fun onFailure(e: Exception) {
        Log.w(ContentValues.TAG, "Face Detector failed")
    }

    companion object {
        private const val TAG = "FaceContourProcessor"
    }
}
