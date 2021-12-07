package com.heliopales.bladeexpertfiller.camera

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import java.io.File

class ViewPagerAdapter(val context: Context, val mediaList: MutableList<File>) : PagerAdapter(){
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        Glide.with(imageView).load(mediaList[position]).fitCenter().into(imageView)
        container.addView(imageView)
        return imageView
    }

    override fun getCount(): Int {
        return mediaList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
       return view==`object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeViewAt(position)
    }

   /* override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }*/
}