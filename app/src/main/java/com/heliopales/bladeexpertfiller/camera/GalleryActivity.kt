package com.heliopales.bladeexpertfiller.camera

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
        }?.sorted()?.toMutableList() ?: mutableListOf()


        println("====Il y a ${mediaList.size} fichiers")

        val viewPagerAdapter = ViewPagerAdapter(this@GalleryActivity, mediaList)

        photo_view_pager?.apply {
            offscreenPageLimit = 2
            adapter = viewPagerAdapter
        }

        delete_photo_button.setOnClickListener {
            AlertDialog.Builder(photo_view_pager.context, android.R.style.Theme_Material_Dialog)
                .setTitle("Confirmer")
                .setMessage("Etes vous sur de vouloir supprimer cette photo ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Oui") { _, _ ->
                    val position = photo_view_pager.currentItem

                    println("=== delete ${mediaList[position].name}")
                    mediaList[position].delete()
                    mediaList.removeAt(position)
                    viewPagerAdapter.notifyDataSetChanged()

                    // If all photos have been deleted, return to camera
                    if (mediaList.isEmpty()) {
                        finish()
                    }

                }
                .setNegativeButton("Non", null)
                .create().show()




            /*mediaList.getOrNull(photo_view_pager.currentItem)?.let { mediaFile ->
                println("=== delete ${mediaFile.name}")
                AlertDialog.Builder(photo_view_pager.context, android.R.style.Theme_Material_Dialog)
                    .setTitle("Confirmer")
                    .setMessage("Etes vous sur de vouloir supprimer cette photo ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Oui") { _, _ ->
                        // Delete current photo
                        mediaFile.delete()

                        // Notify our view pager
                        mediaList.removeAt(photo_view_pager.currentItem)
                        viewPagerAdapter.notifyDataSetChanged()

                        // If all photos have been deleted, return to camera
                        if (mediaList.isEmpty()) {
                            finish()
                        }

                    }
                    .setNegativeButton("Non", null)
                    .create().show()
            }*/
        }

    }


}