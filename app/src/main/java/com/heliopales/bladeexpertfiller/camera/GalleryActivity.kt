package com.heliopales.bladeexpertfiller.camera

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.heliopales.bladeexpertfiller.R
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.item_image_view.*
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

        photo_view_pager?.apply {
            offscreenPageLimit = 2
            adapter =  ViewPagerAdapter(this@GalleryActivity, mediaList)
        }

        delete_photo_button.setOnClickListener {
            mediaList.getOrNull(photo_view_pager.currentItem)?.let { mediaFile ->

                AlertDialog.Builder(photo_view_pager.context, android.R.style.Theme_Material_Dialog)
                    .setTitle("Confirmer")
                    .setMessage("Etes vous sur de vouloir supprimer cette photo ?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Oui") { _, _ ->
                        // Delete current photo
                        mediaFile.delete()

                        /*// Send relevant broadcast to notify other apps of deletion
                        MediaScannerConnection.scanFile(
                            applicationContext, arrayOf(mediaFile.absolutePath), null, null)*/

                        // Notify our view pager
                        mediaList.removeAt(photo_view_pager.currentItem)
                        photo_view_pager.adapter?.notifyDataSetChanged()

                        // If all photos have been deleted, return to camera
                        if (mediaList.isEmpty()) {
                            finish()
                        }

                    }
                    .setNegativeButton("Non", null)
                    .create().show()
            }
        }

    }


    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class CustomPagerAdapter(private val mContext: Context) : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val media: File = mediaList[position]
           /* val imageView = ImageView(mContext)
            Glide.with(imageView)
                .load(media.toURI())
                .apply(RequestOptions.fitCenterTransform())
                .into(imageView)*/

            val inflater = LayoutInflater.from(mContext)
            val layout = inflater.inflate(R.layout.item_image_view, container, false)
            val imageView = layout.findViewById<ImageView>(R.id.image_view_gallery)
            Glide.with(imageView)
                .load(media.toURI())
                .apply(RequestOptions.fitCenterTransform())
                .into(imageView)
            container.addView(layout)
            return layout

        }



        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeViewAt(position)
        }

        override fun getCount(): Int {
            return mediaList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view==`object`;
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mediaList[position].name
        }

    }
}