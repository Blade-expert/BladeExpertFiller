package com.heliopales.bladeexpertfiller.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.CameraCaptureMetaData
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_TURBINE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.utils.*
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OUTPUT_PATH = "output_path"
        const val EXTRA_PICTURE_TYPE = "pic_type"
        const val EXTRA_RELATED_ID = "pic_related_id"
        const val EXTRA_INTERVENTION_ID = "intervention_id"
        const val EXTRA_FLASH_MODE = "initial_flash_mode"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss_SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val TAG = CameraActivity::class.java.simpleName

    private var imageCapture: ImageCapture? = null
    lateinit var outputDirectoryPath: String
    lateinit var outputDirectory: File
    private var initialFlashMode:Int = ImageCapture.FLASH_MODE_OFF
    private var pictureType: Int = PICTURE_TYPE_TURBINE
    private var relatedId: Int = -1
    private var interventionId: Int = -1

    private var cameraControl: CameraControl? = null
    private var cameraAvailable = false

    private var linearZoom = 0f;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        initialFlashMode = intent.getIntExtra(EXTRA_FLASH_MODE, ImageCapture.FLASH_MODE_OFF)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        pictureType = intent.getIntExtra(EXTRA_PICTURE_TYPE, PICTURE_TYPE_TURBINE)
        relatedId = intent.getIntExtra(EXTRA_RELATED_ID, -1)

        interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)
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
        see_photos_button.setOnClickListener { openGallery() }

        flash_button.tag = initialFlashMode
        updateFlashButton()

        flash_button.setOnClickListener { changeFlashMode() }

        camera_preview.setOnTouchListener(View.OnTouchListener{view: View, motionEvent: MotionEvent ->
            when(motionEvent.action){
                MotionEvent.ACTION_DOWN -> {
                    return@OnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    focus_image.visibility = View.VISIBLE
                    focus_image.x = motionEvent.x - focus_image.width/2
                    focus_image.y = motionEvent.y - focus_image.height/2

                    // Get the MeteringPointFactory from PreviewView
                    val factory = camera_preview.meteringPointFactory

                    // Create a MeteringPoint from the tap coordinates
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)

                    // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                    val action = FocusMeteringAction
                        .Builder(point)
                        .build()

                    // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                    // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                    val focusListenableFuture = cameraControl?.startFocusAndMetering(action)
                    focusListenableFuture?.addListener( {
                        focus_image.visibility = View.INVISIBLE
                    }, ContextCompat.getMainExecutor(this))

                    view.performClick()
                    return@OnTouchListener true
                }
                else -> return@OnTouchListener false
            }
        })

        zoom_slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                linearZoom = progress.toFloat() / 100.toFloat()
                cameraControl?.setLinearZoom(linearZoom)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        cameraControl?.setLinearZoom(linearZoom)
    }

    private fun changeFlashMode() {
        var newState = ImageCapture.FLASH_MODE_OFF
        when (flash_button.tag as Int) {
            ImageCapture.FLASH_MODE_OFF -> newState = ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> newState = ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_AUTO -> newState = ImageCapture.FLASH_MODE_OFF
        }
        flash_button.tag = newState
        updateFlashButton()
        imageCapture?.flashMode = newState
    }

    private fun updateFlashButton(){
        with(flash_button) {
            when (tag as Int) {
                ImageCapture.FLASH_MODE_OFF -> background = ContextCompat.getDrawable(
                    this@CameraActivity,
                    R.drawable.ic_baseline_flash_off_24
                )
                ImageCapture.FLASH_MODE_ON -> background = ContextCompat.getDrawable(
                    this@CameraActivity,
                    R.drawable.ic_baseline_flash_on_24
                )
                ImageCapture.FLASH_MODE_AUTO -> background = ContextCompat.getDrawable(
                    this@CameraActivity,
                    R.drawable.ic_baseline_flash_auto_24
                )
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(this, GalleryActivity::class.java);
        intent.putExtra(GalleryActivity.EXTRA_DIRECTORY_PATH, outputDirectoryPath)
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
        if (outputDirectory.listFiles { file ->
                arrayOf("JPG").contains(file.extension.uppercase(Locale.ROOT))
            }?.size == 0) {
            see_photos_button.visibility = View.INVISIBLE
        }
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener( {
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
                .setFlashMode(initialFlashMode)
                .setTargetResolution(Size(1440, 1080))
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            try {
                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
                cameraAvailable = true
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(applicationContext))
    }

    private fun takePhoto() {
        if (!cameraAvailable) return
        cameraAvailable = false
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    toast("Can't take picture")
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cameraAvailable = true
                    val savedUri = Uri.fromFile(photoFile)
                    App.database.interventionDao()
                        .updateExportationState(interventionId, EXPORTATION_STATE_NOT_EXPORTED)
                    App.database.pictureDao().insertPicture(
                        Picture(
                            fileName = photoFile.name,
                            absolutePath = photoFile.absolutePath,
                            uri = savedUri.toString(),
                            type = pictureType,
                            relatedId = relatedId,
                            interventionId = interventionId
                        )
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setGalleryThumbnail(savedUri)
                    }
                    Log.d(TAG, "Photo capture succeeded: $savedUri")
                }
            })

        val root = findViewById<View>(android.R.id.content).rootView
        // Display flash animation to indicate that photo was captured
        root.postDelayed({
            root.foreground = ColorDrawable(Color.WHITE)
            root.postDelayed(
                { root.foreground = null }, ANIMATION_FAST_MILLIS
            )
        }, ANIMATION_SLOW_MILLIS)


    }

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