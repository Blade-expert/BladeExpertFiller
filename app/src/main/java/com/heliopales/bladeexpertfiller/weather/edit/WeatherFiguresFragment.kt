package com.heliopales.bladeexpertfiller.weather.edit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.weather.Weather
import com.heliopales.bladeexpertfiller.weather.WeatherViewPagerActivity
import kotlin.math.roundToInt


class WeatherFiguresFragment : Fragment() {

    private lateinit var weather: Weather
    private lateinit var wind: EditText
    private lateinit var temp: EditText
    private lateinit var hum: EditText
    private lateinit var doneButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weather = (activity as WeatherViewPagerActivity).weather
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wind = view.findViewById(R.id.wx_wind_edit_text)
        temp = view.findViewById(R.id.wx_temp_edit_text)
        hum = view.findViewById(R.id.wx_hum_edit_text)
        doneButton = view.findViewById(R.id.wx_done_button)
        attachListeners()
    }

    private fun attachListeners() {
        wind.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (wind.text.isEmpty())
                    weather.windspeed = null
                else
                    weather.windspeed =
                        wind.text.toString().replace(",", ".").toFloat()
                App.database.interventionDao()
                    .updateExportationState(weather.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        temp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (temp.text.isEmpty())
                    weather.temperature = null
                else
                    weather.temperature =
                        temp.text.toString().replace(",", ".").toFloat()
                App.database.interventionDao()
                    .updateExportationState(weather.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        hum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (hum.text.isEmpty())
                    weather.humidity = null
                else
                    weather.humidity =
                        hum.text.toString().replace(",", ".").toFloat()
                App.database.interventionDao()
                    .updateExportationState(weather.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })


        doneButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        wind.setText("${weather.windspeed ?: ""}")
        temp.setText("${weather.temperature ?: ""}")
        hum.setText("${weather.humidity ?: ""}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather_figures, container, false)
    }

}