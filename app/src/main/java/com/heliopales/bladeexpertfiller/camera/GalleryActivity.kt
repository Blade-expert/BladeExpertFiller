package com.heliopales.bladeexpertfiller.camera

import android.media.MediaScannerConnection
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File
import java.util.*


class GalleryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DIRECTORY_PATH = "directory_path"
    }

    private lateinit var mediaList: MutableList<File>
    lateinit var rootDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)


        rootDirectory = File(intent.getStringExtra(EXTRA_DIRECTORY_PATH).toString())
        rootDirectory.mkdirs()

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            arrayOf("JPG").contains(file.extension.uppercase(Locale.ROOT))
        }?.sorted()?.reversed()?.toMutableList() ?: mutableListOf()


        println("====Il y a ${mediaList.size} fichiers")

        val viewPagerAdapter = ViewPagerAdapter(this@GalleryActivity, mediaList)

        photo_view_pager?.apply {
            offscreenPageLimit = 2
            adapter = viewPagerAdapter
        }

        delete_photo_button.setOnClickListener {
            AlertDialog.Builder(photo_view_pager.context, android.R.style.Theme_Material_Dialog)
                .setTitle("Confirm")
                .setMessage("Are you sure ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes") { _, _ ->
                    val position = photo_view_pager.currentItem
                    val mediaFile = mediaList[position]
                    println("=== delete ${mediaList[position].name}")
                    mediaFile.delete()
                    App.database.pictureDao().deletePictureByFileName(mediaFile.name)

                    MediaScannerConnection.scanFile(
                        photo_view_pager.context, arrayOf(mediaFile.absolutePath), null, null)

                    mediaList.removeAt(position)
                    viewPagerAdapter.notifyDataSetChanged()

                    // If all photos have been deleted, return to camera
                    if (mediaList.isEmpty()) {
                        finish()
                    }

                }
                .setNegativeButton("No", null)
                .create().show()

        }

    }


}