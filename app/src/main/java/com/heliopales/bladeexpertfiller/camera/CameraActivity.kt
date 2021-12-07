package com.heliopales.bladeexpertfiller.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.utils.ANIMATION_FAST_MILLIS
import com.heliopales.bladeexpertfiller.utils.ANIMATION_SLOW_MILLIS
import com.heliopales.bladeexpertfiller.utils.dpToPx
import com.heliopales.bladeexpertfiller.utils.simulateClick
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    companion object {
        val EXTRA_OUTPUT_PATH = "output_path"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss_SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val TAG = CameraActivity::class.java.simpleName

    private var imageCapture: ImageCapture? = null
    lateinit var outputDirectoryPath: String
    lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        outputDirectoryPath = intent.getStringExtra(EXTRA_OUTPUT_PATH).toString()
        outputDirectory = File(outputDirectoryPath)
        outputDirectory.mkdirs()

        // In the background, load latest photo taken (if any) for gallery thumbnail
        outputDirectory.listFiles { file ->
            arrayOf("JPG").contains(file.extension.uppercase(Locale.ROOT))
        }?.maxOrNull()?.let {
            setGalleryThumbnail(Uri.fromFile(it))
        }

        camera_capture_button.setOnClickListener { takePhoto() }
        see_photos_button.setOnClickListener{openGallery()}
    }

    private fun openGallery() {
        val intent = Intent(this,GalleryActivity::class.java);
        intent.putExtra(GalleryActivity.EXTRA_DIRECTORY_PATH,outputDirectoryPath)
        startActivity(intent)
    }


    private fun setGalleryThumbnail(uri: Uri) {
        // Run the operations in the view's thread
        see_photos_button.let { seePhotosButton ->
            seePhotosButton.post {
                seePhotosButton.visibility = View.VISIBLE
                // Load thumbnail into circular button using Glide
                Glide.with(seePhotosButton)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(seePhotosButton)
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        if( outputDirectory.listFiles { file ->
                arrayOf("JPG").contains(file.extension.uppercase(Locale.ROOT))
            }?.size == 0 ){
            see_photos_button.visibility = View.INVISIBLE
        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(1440, 1080))
                .build()
                .also {
                    it.setSurfaceProvider(camera_preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(Size(1440, 1080))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            try {
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(applicationContext))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setGalleryThumbnail(savedUri)
                    }
                    val msg = "Photo capture succeeded: $savedUri"
                    Log.d(TAG, msg)
                }
            })
        // We can only change the foreground Drawable using API level 23+ API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val root = findViewById<View>(android.R.id.content).rootView
            // Display flash animation to indicate that photo was captured

            root.postDelayed({
                root.foreground = ColorDrawable(Color.WHITE)
                root.postDelayed(
                    { root.foreground = null }, ANIMATION_FAST_MILLIS
                )
            }, ANIMATION_SLOW_MILLIS)
        }
    }

    /** When key down event is triggered, relay it via local broadcast so fragments can handle it */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                camera_capture_button?.simulateClick()
                true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                camera_capture_button?.simulateClick()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}