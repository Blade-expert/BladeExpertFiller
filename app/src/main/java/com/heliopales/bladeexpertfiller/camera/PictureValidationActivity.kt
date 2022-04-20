package com.heliopales.bladeexpertfiller.camera

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.heliopales.bladeexpertfiller.R
import kotlinx.android.synthetic.main.activity_picture_validation.*

class PictureValidationActivity : AppCompatActivity() {

    companion object{
        val EXTRA_PICTURE_URI = "picture_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_validation)

        val pictureUri = intent.getParcelableExtra<Uri>(EXTRA_PICTURE_URI)

        Glide.with(val_image_view).load(pictureUri).fitCenter().into(val_image_view)

        val_ok_btn.setOnClickListener {
            val response =  Intent()
            setResult(Activity.RESULT_OK, response)
            finish()
        }
        val_nok_btn.setOnClickListener {
            finish()
        }

    }
}