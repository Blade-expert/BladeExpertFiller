package com.heliopales.bladeexpertfiller.turbine

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_TURBINE
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_WINDFARM
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.intervention.InterventionDetailsActivity

class WindfarmActivity : AppCompatActivity() {

    val TAG = WindfarmActivity::class.java.simpleName

    companion object {
        const val EXTRA_INTERVENTION = "intervention"
    }

    lateinit var intervention: Intervention;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_windfarm)

        intervention = intent.getParcelableExtra(InterventionDetailsActivity.EXTRA_INTERVENTION)!!

        findViewById<TextView>(R.id.windfarm_name).text = intervention.windfarmName

        findViewById<Button>(R.id.wfm_pic_button).setOnClickListener {
            startWindfarmPictureActivity()
        }

        addTurbineButtons()

    }



    private fun addTurbineButtons() {
        Log.d(TAG, "addTurbineButtons()")
        findViewById<GridLayout>(R.id.turbine_buttons_list).let {
            App.database.turbineDao().getTurbinesByWindfarmId(intervention.windfarmId)
                .sortedBy { it.numInWindfarm }.forEach { trb ->
                    Log.d(TAG, trb.toString())

                    val turbineLayout: ConstraintLayout =
                        View.inflate(
                            this,
                            R.layout.pattern_turbine_button,
                            null
                        ) as ConstraintLayout



                    val button = turbineLayout.findViewById<Button>(R.id.turbine_button);
                    button.tag = trb
                    button.text = "${trb.alias}\n${trb.serial ?: ""}"
                    button.setOnClickListener {
                        startTurbinePictureActivity(it.tag as Turbine)
                    }
                    it.addView(turbineLayout)
                }
        }
    }

    private fun startTurbinePictureActivity(turbine: Turbine) {
        val intent = Intent(this, CameraActivity::class.java)
        val path = App.getTurbinePath(intervention, turbine)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_TURBINE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, turbine.id)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
    }

    private fun startWindfarmPictureActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        val path = App.getWindfarmPath(intervention)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_WINDFARM)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, intervention.windfarmId)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
    }
}