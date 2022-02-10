package com.heliopales.bladeexpertfiller.intervention

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_INTERVENTION
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener
import com.heliopales.bladeexpertfiller.weather.WeatherActivity

class InterventionActivity : AppCompatActivity(), View.OnClickListener,  View.OnTouchListener {

    val TAG = InterventionActivity::class.java.simpleName

    private lateinit var gestureDetector: GestureDetector

    companion object{
        const val EXTRA_INTERVENTION = "intervention"
    }

    private lateinit var intervention: Intervention

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention)

        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!

        findViewById<TextView>(R.id.itv_turbine_name).text = intervention.name

        findViewById<Button>(R.id.itv_pic_button).setOnClickListener(this)
        findViewById<Button>(R.id.itv_wx_button).setOnClickListener(this)

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
        }
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