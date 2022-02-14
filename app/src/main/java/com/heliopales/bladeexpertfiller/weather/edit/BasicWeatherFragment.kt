package com.heliopales.bladeexpertfiller.weather.edit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.NumberPicker
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity
import com.heliopales.bladeexpertfiller.spotcondition.damages.editloop.IndoorPositionFragment
import com.heliopales.bladeexpertfiller.weather.Weather
import com.heliopales.bladeexpertfiller.weather.WeatherViewPagerActivity
import android.R.string.no
import java.time.LocalDateTime
import kotlin.math.roundToInt


class BasicWeatherFragment : Fragment(), View.OnClickListener {

    private val TAG = BasicWeatherFragment::class.java.simpleName

    private val buttons = mutableListOf<Button>()

    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: NumberPicker

    private lateinit var weather: Weather

    private var initCall = true;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weather = (activity as WeatherViewPagerActivity).weather
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated()")
        super.onViewCreated(view, savedInstanceState)
        view.touchables.forEach {
            if (it is Button) {
                buttons.add(it)
                it.setOnClickListener(this)
            }
        }
        datePicker = view.findViewById(R.id.wx_date_picker)
        timePicker = view.findViewById(R.id.wx_time_picker)
        timePicker.displayedValues = arrayOf("9h", "12h", "15h", "18h")
        timePicker.maxValue = 3;
        timePicker.minValue = 0;

        datePicker.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            if (initCall) {
                initCall = false
                return@setOnDateChangedListener
            }

            updateDate(
                year,
                monthOfYear,
                dayOfMonth,
                9 + timePicker.value * 3
            )
        }

        timePicker.setOnValueChangedListener { _, _, newVal ->
            updateDate(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                9 + newVal * 3
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_basics, container, false)
    }

    override fun onClick(v: View) {
        buttons.forEach {
            if (it == v) {
                if (it.foreground == null) {
                    App.database.interventionDao().updateExportationState(
                        weather.interventionId,
                        EXPORTATION_STATE_NOT_EXPORTED
                    )
                }
                it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
            } else {
                it.foreground = null
            }
        }

        if (v is Button) {
            when (v.id) {
                R.id.wx_button_na -> weather.type = null
                else -> weather.type = v.tag as String
            }
            (activity as WeatherViewPagerActivity).pager.currentItem =
                INDEX_WEATHER_LOOP_MEAS
        }
    }

    private fun updateDate(year: Int, monthOfYear: Int, dayOfMonth: Int, time: Int) {
        Log.d(TAG, "updateDate( $year, $monthOfYear, $dayOfMonth, $time)")
        weather.dateTime = LocalDateTime.of(
            year,
            monthOfYear+1,
            dayOfMonth,
            time,
            0
        )
        App.database.interventionDao().updateExportationState(
            weather.interventionId,
            EXPORTATION_STATE_NOT_EXPORTED
        )

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
        (requireActivity() as WeatherViewPagerActivity).hideKeyboard()
        if (weather.type == null) {
            buttons.forEach {
                if (it.id == R.id.wx_button_na) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        } else {
            buttons.forEach {
                if (it.tag == weather.type) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        }

        if (weather.dateTime != null) {
            initCall = true
            datePicker.updateDate(
                weather.dateTime!!.year,
                weather.dateTime!!.monthValue - 1,
                weather.dateTime!!.dayOfMonth
            )

            var hr: Float = weather.dateTime!!.hour.toFloat()
            hr -= 9
            if (hr < 0) hr = 0f
            hr /= 3f
            if (hr > 3) hr = 3f

            timePicker.value = hr.roundToInt()
        } else {
        }
    }


}