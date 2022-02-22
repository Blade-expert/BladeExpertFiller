package com.heliopales.bladeexpertfiller.weather

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.R
import java.time.format.DateTimeFormatter

class WeatherAdapter(
    private val weathers: List<Weather>,
    private val weatherListener: WeatherItemListener
) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>(), View.OnClickListener {

    private val TAG = WeatherAdapter::class.java.simpleName

    interface WeatherItemListener {
        fun onWeatherSelected(weather: Weather)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.weather_card_view)
        val dayTextView: TextView = itemView.findViewById(R.id.iwx_day)
        val hourTextView: TextView = itemView.findViewById(R.id.iwx_hour)
        val weatherIcon: ImageView = itemView.findViewById(R.id.iwx_icon)
        val windTextView: TextView = itemView.findViewById(R.id.iwx_wind)
        val tempTextView: TextView = itemView.findViewById(R.id.iwx_temp)
        val humTextView: TextView = itemView.findViewById(R.id.iwx_hum)
        val lock: ImageView = itemView.findViewById(R.id.iwx_lock)
        val windLine: LinearLayout = itemView.findViewById(R.id.iwx_wind_line)
        val tempLine: LinearLayout = itemView.findViewById(R.id.iwx_temp_line)
        val humLine: LinearLayout = itemView.findViewById(R.id.iwx_hum_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val damageItem =
            LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return ViewHolder(damageItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wx = weathers[position]
        with(holder) {
            cardView.tag = wx
            cardView.setOnClickListener(this@WeatherAdapter)
            dayTextView.text = wx.dateTime?.format(DateTimeFormatter.ofPattern("dd")) ?: "--"
            hourTextView.text = wx.dateTime?.format(DateTimeFormatter.ofPattern("HH'h'")) ?: "--"

            windTextView.text = wx.windspeed?.toString() ?: "--"
            tempTextView.text = wx.temperature?.toString() ?: "--"
            humTextView.text = wx.humidity?.toString() ?: "--"

            for (i in 0 until windLine.childCount) {
                val v: TextView = windLine.getChildAt(i) as TextView
                if (wx.windspeed == null)
                    v.setTextColor(ContextCompat.getColor(cardView.context, R.color.bulma_danger))
                else
                    v.setTextColor(
                        ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_link_dark
                        )
                    )
            }

            for (i in 0 until tempLine.childCount) {
                val v: TextView = tempLine.getChildAt(i) as TextView
                if (wx.temperature == null)
                    v.setTextColor(ContextCompat.getColor(cardView.context, R.color.bulma_danger))
                else
                    v.setTextColor(
                        ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_link_dark
                        )
                    )
            }

            for (i in 0 until humLine.childCount) {
                val v: TextView = humLine.getChildAt(i) as TextView
                if (wx.humidity == null)
                    v.setTextColor(ContextCompat.getColor(cardView.context, R.color.bulma_danger))
                else
                    v.setTextColor(
                        ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_link_dark
                        )
                    )
            }


            when (wx.type) {
                "SUN" -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_sun_solid_24
                    )
                )
                "CLD" -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_cloud_solid_24
                    )
                )
                "RAI" -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_cloud_showers_heavy_solid_24
                    )
                )
                "FOG" -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_smog_solid_24
                    )
                )
                "WND" -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_wind_solid_24
                    )
                )
                else -> weatherIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        cardView.context,
                        R.drawable.ic_circle_question_regular_24
                    )
                )

            }
            if (wx.id == null) {
                lock.visibility = View.GONE
            } else {
                lock.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = weathers.size

    override fun onClick(view: View) {
        when (view.id) {
            R.id.weather_card_view -> weatherListener.onWeatherSelected(view.tag as Weather)
            else -> Log.d(TAG, "unhandled click")
        }

    }
}