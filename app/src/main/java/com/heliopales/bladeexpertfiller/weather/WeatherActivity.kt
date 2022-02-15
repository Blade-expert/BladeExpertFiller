package com.heliopales.bladeexpertfiller.weather

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.bladeexpert.WeatherWrapper
import com.heliopales.bladeexpertfiller.bladeexpert.mapBladeExpertWeather
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDateTime
import kotlin.math.roundToInt

class WeatherActivity : AppCompatActivity(), View.OnClickListener,
    WeatherAdapter.WeatherItemListener {

    val TAG = WeatherActivity::class.java.simpleName

    companion object {
        val EXTRA_INTERVENTION = "intervention"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WeatherAdapter
    private lateinit var intervention: Intervention
    private lateinit var weathers: MutableList<Weather>

    private lateinit var refreshLayout: SwipeRefreshLayout
    private var snackBarActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        recyclerView = findViewById(R.id.weather_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(simpleCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!

        findViewById<TextView>(R.id.weather_intervention_title).text = "${intervention.name}"


        refreshLayout = findViewById(R.id.weather_swipe_layout)
        refreshLayout.setOnRefreshListener { updateWeatherList() }

        findViewById<ImageButton>(R.id.add_weather_button).setOnClickListener(this)

    }

    private fun updateWeatherList() {
        if (snackBarActive) {
            if (refreshLayout.isRefreshing)
                refreshLayout.isRefreshing = false
            return
        }
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
        /* UPDATE database with weathers from BladeExpert */
        App.bladeExpertService.getWeathers(interventionId = intervention.id)
            .enqueue(object : retrofit2.Callback<Array<WeatherWrapper>> {
                override fun onResponse(
                    call: Call<Array<WeatherWrapper>>,
                    response: Response<Array<WeatherWrapper>>
                ) {
                    if (response.isSuccessful) {
                        response.body().let {
                            it?.forEach { ww ->
                                Log.d(TAG, ww.toString())
                                val wx =
                                    App.database.weatherDao()
                                        .getWeatherByRemoteId(remoteId = ww.id!!)
                                var w = mapBladeExpertWeather(ww)

                                if (wx != null){
                                    w.localId = wx.localId
                                    if(w.dateTime == null) w.dateTime = wx.dateTime
                                    if( w.type == null) w.type = wx.type
                                    if( w.windspeed == null) w.windspeed = wx.windspeed
                                    if( w.temperature == null) w.temperature = wx.temperature
                                    if( w.humidity == null) w.humidity = wx.humidity
                                }
                                App.database.weatherDao().upsert(w)
                            }
                        }
                    }
                    loadWeatherFromDb()
                    refreshLayout.isRefreshing = false
                }

                override fun onFailure(call: Call<Array<WeatherWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to update weather list", t)
                    toast("Impossible to update weather list")
                    loadWeatherFromDb()
                    refreshLayout.isRefreshing = false
                }
            })
    }

    private fun preDeleteWeather(deletedWeather: Weather, position: Int) {
        snackBarActive = true
        weathers.remove(deletedWeather)
        adapter.notifyItemRemoved(position)
        Snackbar.make(
            recyclerView,
            "Weather deleted",
            Snackbar.LENGTH_SHORT
        )
            .setAction("Cancel") {
                weathers.add(position, deletedWeather)
                adapter.notifyItemInserted(position)
                snackBarActive = false
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_TIMEOUT ||
                        event == DISMISS_EVENT_CONSECUTIVE
                    ) {
                        snackBarActive = false
                        effectivelyDeleteWeather(deletedWeather)
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    private fun effectivelyDeleteWeather(deletedWeather: Weather) {
        App.database.weatherDao().delete(deletedWeather)
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        loadWeatherFromDb()
        updateWeatherList()
    }

    fun loadWeatherFromDb() {
        weathers = App.database.weatherDao().getWeathersByInterventionId(intervention.id)
        weathers.sortBy { it.dateTime }
        weathers.reverse()
        adapter = WeatherAdapter(weathers, this)
        recyclerView.adapter = adapter
    }

    override fun onWeatherSelected(weather: Weather) {
        launchWeatherPager(weather)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_weather_button -> launchWeatherPager(addNewWeather())
        }
    }

    private fun addNewWeather(): Weather {
        Log.d(TAG, "addNewWeather()")

        var weather = Weather(intervention.id)

        val now = LocalDateTime.now()

        var hr: Float = now.hour.toFloat() + now.minute.toFloat() / 60f
        hr -= 9
        if (hr < 0) hr = 0f
        hr /= 3f
        if (hr > 3) hr = 3f


        weather.dateTime = LocalDateTime.of(
            now.year,
            now.monthValue,
            now.dayOfMonth,
            hr.roundToInt() * 3 + 9,
            0
        )

        val newId = App.database.weatherDao().upsert(weather).toInt()
        weather.localId = newId

        weathers.add(weather)
        weathers.sortBy { it.dateTime }

        adapter.notifyDataSetChanged()

        return weather
    }

    private fun launchWeatherPager(weather: Weather) {
        val intent = Intent(this, WeatherViewPagerActivity::class.java)
        intent.putExtra(WeatherViewPagerActivity.EXTRA_WEATHER, weather)
        startActivity(intent)
    }

    private val simpleCallBack = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            preDeleteWeather(weathers[position], position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addBackgroundColor(getColor(R.color.bulma_danger))
                .addActionIcon(R.drawable.ic_baseline_delete_24)
                .create()
                .decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }
}