package com.heliopales.bladeexpertfiller.intervention

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_INTERVENTION
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener
import com.heliopales.bladeexpertfiller.weather.WeatherActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class InterventionActivity : AppCompatActivity(), View.OnClickListener,  View.OnTouchListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    val TAG = InterventionActivity::class.java.simpleName

    private lateinit var gestureDetector: GestureDetector

    companion object{
        const val EXTRA_INTERVENTION_ID = "interventionId"
        const val TAG_START = "start"
        const val TAG_END = "end"
    }

    private lateinit var intervention: Intervention

    private lateinit var startButton: Button
    private lateinit var endButton: Button

    private var currentTag:String = TAG_START

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention)

        intervention = App.database.interventionDao().getById(intent.getIntExtra(
            EXTRA_INTERVENTION_ID, -1)!!)

        findViewById<TextView>(R.id.itv_turbine_name).text = intervention.name

        findViewById<Button>(R.id.itv_pic_button).setOnClickListener(this)
        findViewById<Button>(R.id.itv_wx_button).setOnClickListener(this)

        startButton = findViewById(R.id.itv_start_button)
        startButton.setOnClickListener(this)

        endButton = findViewById(R.id.itv_end_button)
        endButton.setOnClickListener(this)

        gestureDetector = GestureDetector(this, object : OnSwipeListener() {
            override fun onSwipe(direction: Direction?): Boolean {
                when (direction) {
                    Direction.up ->  onBackPressed()
                }
                return true
            }
        })

        findViewById<ConstraintLayout>(R.id.itv_main_layout).setOnTouchListener(this)

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true;
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.itv_pic_button-> startInterventionCamera()
            R.id.itv_wx_button-> startWeatherActivity()
            R.id.itv_start_button-> manageStartClick()
            R.id.itv_end_button-> manageEndClick()
        }
    }

    private fun manageStartClick() {
        if(intervention.startTime == null){
            intervention.startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
            App.database.interventionDao().updateIntervention(intervention)
            updateDateButtons()
        }else{
            val year = intervention.startTime!!.year
            val month = intervention.startTime!!.monthValue-1
            val day = intervention.startTime!!.dayOfMonth

            val datePickerDialog = DatePickerDialog(this,this, year, month, day)
            currentTag = TAG_START
            datePickerDialog.show()
        }
    }

    private fun manageEndClick() {
        if(intervention.endTime == null){
            intervention.endTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
            App.database.interventionDao().updateIntervention(intervention)
            updateDateButtons()
        }else{
            val year = intervention.endTime!!.year
            val month = intervention.endTime!!.monthValue-1
            val day = intervention.endTime!!.dayOfMonth

            val datePickerDialog = DatePickerDialog(this,this, year, month, day)
            currentTag = TAG_END
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        when(currentTag){
            TAG_START -> {
                intervention.startTime=LocalDateTime.of(year, month+1, dayOfMonth, intervention.startTime!!.hour, intervention.startTime!!.minute)
                intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                updateDateButtons()
                val hour = intervention.startTime!!.hour
                val minute = intervention.startTime!!.minute
                val timePickerDialog = TimePickerDialog(this,this, hour, minute, DateFormat.is24HourFormat(this))
                timePickerDialog.show()
            }
            TAG_END ->{
                intervention.endTime=LocalDateTime.of(year, month+1, dayOfMonth, intervention.endTime!!.hour, intervention.endTime!!.minute)
                intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                updateDateButtons()
                val hour = intervention.endTime!!.hour
                val minute = intervention.endTime!!.minute
                val timePickerDialog = TimePickerDialog(this,this, hour, minute, DateFormat.is24HourFormat(this))
                timePickerDialog.show()
            }
        }


    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        when(currentTag){
            TAG_START -> {
                intervention.startTime=LocalDateTime.of(intervention.startTime!!.year, intervention.startTime!!.monthValue, intervention.startTime!!.dayOfMonth, hourOfDay, minute)
                intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                updateDateButtons()
            }
            TAG_END -> {
                intervention.endTime=LocalDateTime.of(intervention.endTime!!.year, intervention.endTime!!.monthValue, intervention.endTime!!.dayOfMonth, hourOfDay, minute)
                intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                updateDateButtons()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        intervention = App.database.interventionDao().getById(intervention.id)
        updateDateButtons()
    }

    private fun updateDateButtons(){
        startButton.text = if(intervention.startTime == null) "Start intervention" else "Start : ${intervention.startTime!!.format(
            DateTimeFormatter.ofPattern("dd/MM 'at' HH:mm"))}"

        endButton.text = if(intervention.endTime == null) "Stop intervention" else "Stop : ${intervention.endTime!!.format(
            DateTimeFormatter.ofPattern("dd/MM 'at' HH:mm"))}"
    }

    private fun startInterventionCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        var path = App.getInterventionPicturePath(intervention)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_INTERVENTION)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, intervention.id)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
    }

    private fun startWeatherActivity(){
        val intent = Intent(this, WeatherActivity::class.java)
        intent.putExtra(WeatherActivity.EXTRA_INTERVENTION, intervention)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0, R.anim.out_to_top)
    }



}