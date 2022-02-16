package com.heliopales.bladeexpertfiller.intervention

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_TURBINE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.turbine.WindfarmActivity
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener
import com.heliopales.bladeexpertfiller.utils.toast

class InterventionDetailsActivity : AppCompatActivity(), View.OnClickListener,
    View.OnTouchListener {

    companion object {
        const val EXTRA_INTERVENTION = "intervention"
    }

    private lateinit var intervention: Intervention
    private lateinit var turbineNameView: TextView
    private lateinit var turbineSerialView: TextView
    private lateinit var gestureDetector: GestureDetector
    private val bladeLayouts = mutableListOf<LinearLayout>()

    val TAG: String = InterventionDetailsActivity::class.java.simpleName

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_details)

        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!
        turbineNameView = findViewById(R.id.turbineName)
        turbineNameView.text = intervention.name

        turbineSerialView = findViewById(R.id.turbineSerialNumber)
        turbineSerialView.text =
            if (intervention.turbineSerial == null) "-----" else intervention.turbineSerial

        findViewById<ImageButton>(R.id.windfarm_button).setOnClickListener(this)
        findViewById<ImageButton>(R.id.editTurbineSerialNumber).setOnClickListener(this)
        findViewById<ImageButton>(R.id.take_turbine_serial_picture).setOnClickListener(this)

        gestureDetector = GestureDetector(this, object : OnSwipeListener() {
            override fun onSwipe(direction: Direction?): Boolean {
                Log.d(TAG, "OnSwipe")
                when (direction) {
                    Direction.up -> Log.d(TAG, "Swiped UP")
                    Direction.down -> startInterventionActivity()
                    Direction.left -> Log.d(TAG, "Swiped LEFT")
                    Direction.right -> Log.d(TAG, "Swiped RIGHT")
                    else -> Log.d(TAG, "No direction found for Swipe")
                }
                return true
            }
        })

        findViewById<ConstraintLayout>(R.id.intervention_detail_layout).setOnTouchListener(this)


        addBladeButtons()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch")
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun startInterventionActivity(){
        val intent = Intent(this, InterventionActivity::class.java)
        intent.putExtra(InterventionActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_top, R.anim.no_anim)
    }

    private fun addBladeButtons() {
        findViewById<LinearLayout>(R.id.bladeButtonLayout).let {
            App.database.bladeDao().getBladesByInterventionId(intervention.id)
                .sortedBy { it.position }.forEach { bla ->

                    val bladeLayout: LinearLayout =
                        View.inflate(this, R.layout.pattern_blade_button, null) as LinearLayout

                    bladeLayout.tag = bla.id
                    bladeLayout.findViewById<Button>(R.id.blade_button).setOnClickListener {
                        startBladeActivity(bla)
                    }

                    bladeLayouts.add(bladeLayout)
                    it.addView(bladeLayout)
                }
        }
    }

    private fun startBladeActivity(blade: Blade) {
        val intent = Intent(this, BladeActivity::class.java)
        intent.putExtra(BladeActivity.EXTRA_INTERVENTION, intervention)
        intent.putExtra(BladeActivity.EXTRA_BLADE_ID, blade.id)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        bladeLayouts.forEach { lay ->
            val dbBla = App.database.bladeDao().getById(lay.tag as Int)
            lay.findViewById<Button>(R.id.blade_button).text =
                "${dbBla.position}${if (dbBla.serial == null) "" else "\n${dbBla.serial}"}"

            var count = App.database.damageDao()
                .countDamageByBladeId(dbBla.id, intervention.id, INHERIT_TYPE_DAMAGE_OUT)
            if (count > 0) {
                lay.findViewWithTag<TextView>("outer_count")
                    .setTextColor(getColor(R.color.bulma_dark))
                lay.findViewWithTag<TextView>("outer_count").text = "E: $count"
                lay.findViewWithTag<TextView>("outer_count").setTypeface(null, Typeface.BOLD)
            } else {
                lay.findViewWithTag<TextView>("outer_count")
                    .setTextColor(getColor(R.color.bulma_gray_light))
                lay.findViewWithTag<TextView>("outer_count").text = "E: --"
                lay.findViewWithTag<TextView>("outer_count").setTypeface(null, Typeface.NORMAL)
            }

            count = App.database.damageDao()
                .countDamageByBladeId(dbBla.id, intervention.id, INHERIT_TYPE_DAMAGE_IN)
            if (count > 0) {
                lay.findViewWithTag<TextView>("inner_count")
                    .setTextColor(getColor(R.color.bulma_dark))
                lay.findViewWithTag<TextView>("inner_count").text = "I: $count"
                lay.findViewWithTag<TextView>("inner_count").setTypeface(null, Typeface.BOLD)
            } else {
                lay.findViewWithTag<TextView>("inner_count")
                    .setTextColor(getColor(R.color.bulma_gray_light))
                lay.findViewWithTag<TextView>("inner_count").text = "I: --"
                lay.findViewWithTag<TextView>("inner_count").setTypeface(null, Typeface.NORMAL)
            }

            count = App.database.drainholeDao()
                .countByBladeId(dbBla.id, intervention.id)
            if (count > 0) {
                lay.findViewWithTag<ImageView>("drain_count")
                    .imageTintList = ColorStateList.valueOf(getColor(R.color.bulma_success))
            } else {
                lay.findViewWithTag<ImageView>("drain_count")
                    .imageTintList = ColorStateList.valueOf(getColor(R.color.bulma_gray_light))
            }

            count = App.database.lightningDao()
                .countByBladeId(dbBla.id, intervention.id)
            if (count > 0) {
                lay.findViewWithTag<ImageView>("lps_count")
                    .imageTintList = ColorStateList.valueOf(getColor(R.color.bulma_success))
            } else {
                lay.findViewWithTag<ImageView>("lps_count")
                    .imageTintList = ColorStateList.valueOf(getColor(R.color.bulma_gray_light))
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.editTurbineSerialNumber -> showChangeTurbineSerialDialog()
            R.id.take_turbine_serial_picture -> takeTurbineSerialPicture()
            R.id.windfarm_button -> startWindfarmActivity()
        }
    }

    private fun startWindfarmActivity() {
        val intent = Intent(this, WindfarmActivity::class.java)
        intent.putExtra(WindfarmActivity.EXTRA_INTERVENTION, intervention)
        startActivity(intent)
    }

    private fun takeTurbineSerialPicture() {
        val intent = Intent(this, CameraActivity::class.java)
        val turbine = App.database.turbineDao().getTurbinesById(intervention.turbineId)!!
        var path = App.getTurbinePath(intervention, turbine)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_TURBINE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, turbine.id)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
    }

    private fun showChangeTurbineSerialDialog() {
        val dialog = ChangeTurbineSerialDialogFragment(intervention.turbineSerial)
        dialog.listener =
            object : ChangeTurbineSerialDialogFragment.ChangeTurbineSerialDialogListener {
                override fun onDialogPositiveClick(serial: String) {
                    updateTurbineSerialNumber(serial)
                }

                override fun onDialogNegativeClick() {
                }
            }
        dialog.show(supportFragmentManager, "ChangeTurbineSerialDialogFragment")
    }

    private fun updateTurbineSerialNumber(serial: String) {
        intervention.turbineSerial = serial
        intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED

        if (App.database.interventionDao().updateIntervention(intervention) == 1) {
            turbineSerialView.text = serial
        } else {
            toast("Impossible de mettre à jour ce numéro")
        }
    }

    override fun onBackPressed() {
        intent = Intent()
        intent.putExtra(EXTRA_INTERVENTION, intervention)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart()")
        super.onRestart()
    }


}