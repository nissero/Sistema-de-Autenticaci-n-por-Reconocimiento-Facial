package com.biogin.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.biogin.myapplication.databinding.ActivityPhotoRegisterBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoRegisterActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPhotoRegisterBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: CameraHelper
    private var photoCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPhotoRegisterBinding.inflate(layoutInflater)
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
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.switchCameraButton.setOnClickListener {
            camera.flipCamera()
        }
    }
    private fun initCamera() {
        camera = CameraHelper(
            null,
            this,
            null,
            viewBinding,
            viewBinding.viewFinder.surfaceProvider,
            viewBinding.graphicOverlayFinder,
            null,
            false
        )
        camera.startCamera()
    }

    private fun takePhoto() {
        if (photoCount < 3) {
            camera.takePhoto(TAG, FILENAME_FORMAT, this, intent) {
                photoCount++
                showOverlayMessage(photoCount)
                if (photoCount >= 3) {
                    Handler().postDelayed({
                        setResult(RESULT_OK, intent) // Seteamos el resultado a OK y devolvemos la data del intent originalmente enviado para subir el usuario a firebase en RegisterActivity
                        finish() // End the activity after the third photo
                    }, 5000)
                }
            }
        }
    }

    private fun showOverlayMessage(photoCount: Int) {
        val message = when (photoCount) {
            1 -> "Gire su cara a la derecha"
            2 -> "Gire su cara a la izquierda"
            3 -> "Usuario registrado exitosamente"
            else -> ""
        }
        if (message.isNotEmpty()) {
            viewBinding.overlayMessage.text = message
            fadeInOverlay()
            disableButtons()
            Handler().postDelayed({
                fadeOutOverlay()
                enableButtons()
            }, 5000)
        }
    }

    private fun fadeInOverlay() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        }
        viewBinding.overlay.visibility = View.VISIBLE
        viewBinding.overlay.startAnimation(fadeIn)
    }

    private fun fadeOutOverlay() {
        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 500
            fillAfter = true
        }
        viewBinding.overlay.startAnimation(fadeOut)
        Handler().postDelayed({
            viewBinding.overlay.visibility = View.GONE
        }, 500)
    }

    private fun disableButtons() {
        viewBinding.imageCaptureButton.isClickable = false
        viewBinding.switchCameraButton.isClickable = false
        viewBinding.imageCaptureButton.isEnabled = false
        viewBinding.switchCameraButton.isEnabled = false
    }

    private fun enableButtons() {
        viewBinding.imageCaptureButton.isClickable = true
        viewBinding.switchCameraButton.isClickable = true
        viewBinding.imageCaptureButton.isEnabled = true
        viewBinding.switchCameraButton.isEnabled = true
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        camera.shutdown()
    }

    companion object {
        private const val TAG = "PhotoRegisterActivity"
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
