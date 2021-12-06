package com.heliopales.bladeexpertfiller.camera

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.heliopales.bladeexpertfiller.R
import java.io.File
import java.util.*
import android.view.ViewGroup

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.SurfaceRequest
import kotlinx.android.synthetic.main.activity_gallery.*


class GalleryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DIRECTORY_PATH = "directory_path"
    }

    private lateinit var mediaList: MutableList<File>
    lateinit var rootDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        rootDirectory = File(intent.getStringExtra(CameraActivity.EXTRA_OUTPUT_PATH).toString())
        rootDirectory.mkdirs()

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            arrayOf("JPG").contains(file.extension.uppercase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()

        photo_view_pager?.apply {
            offscreenPageLimit = 2
            adapter = CustomPagerAdapter(context)
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

                        // Send relevant broadcast to notify other apps of deletion
                        MediaScannerConnection.scanFile(
                            applicationContext, arrayOf(mediaFile.absolutePath), null, null)

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
    inner class CustomPagerAdapter(val mContext: Context) : PagerAdapter() {
        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val media: File = mediaList.get(position)
            val inflater = LayoutInflater.from(mContext)
            val layout =
                inflater.inflate(position, collection, false) as ViewGroup
            collection.addView(layout)
            return layout;
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeViewAt(position)
        }

        override fun getCount(): Int {
            return mediaList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mediaList.get(position).name
        }

    }
}