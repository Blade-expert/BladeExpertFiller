package com.heliopales.bladeexpertfiller.weather

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.spotcondition.damages.editloop.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.intervention.InterventionActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener
import com.heliopales.bladeexpertfiller.weather.edit.BasicWeatherFragment
import com.heliopales.bladeexpertfiller.weather.edit.WeatherFiguresFragment


class WeatherViewPagerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_WEATHER = "Weather"
    }

    private val TAG = WeatherViewPagerActivity::class.java.simpleName

    lateinit var pager: ViewPager2
    lateinit var weather: Weather

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_view_pager)

        weather = intent.getParcelableExtra(EXTRA_WEATHER)!!
        Log.d(TAG,"weather loaded $weather")

        pager = findViewById(R.id.weather_view_pager)

        pager.adapter = WeatherPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.weather_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                INDEX_WEATHER_LOOP_BASE -> tab.text = "Basics"
                INDEX_WEATHER_LOOP_MEAS -> tab.text = "Measures"
            }
        }.attach()

    }

    fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(pager.windowToken, 0)
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart()")
        super.onRestart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        Log.d(TAG, "will upsert weather $weather")
        App.database.weatherDao().upsert(weather)
    }


}

class WeatherPagerAdapter(fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        when (position) {
            INDEX_WEATHER_LOOP_BASE -> return BasicWeatherFragment()
            INDEX_WEATHER_LOOP_MEAS -> return WeatherFiguresFragment();
        }
        return BasicWeatherFragment()

    }
}