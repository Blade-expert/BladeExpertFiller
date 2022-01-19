package com.heliopales.bladeexpertfiller.intervention

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Camera
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.utils.dpToPx
import com.heliopales.bladeexpertfiller.utils.spToPx
import com.heliopales.bladeexpertfiller.utils.toast
import kotlinx.android.synthetic.main.activity_intervention_details.*

class InterventionDetailsActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val EXTRA_INTERVENTION = "intervention"
    }

    private lateinit var intervention: Intervention;
    private lateinit var turbineNameView: TextView;
    private lateinit var turbineSerialView: TextView;
    private val bladeLayouts = mutableListOf<LinearLayout>();

    val TAG = InterventionDetailsActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_details)

        intervention = intent.getParcelableExtra<Intervention>(EXTRA_INTERVENTION)!!
        turbineNameView = findViewById(R.id.turbineName)
        turbineNameView.text = intervention.turbineName

        turbineSerialView = findViewById(R.id.turbineSerialNumber)
        turbineSerialView.text =
            if (intervention.turbineSerial == null) "-----" else intervention.turbineSerial;

        findViewById<ImageButton>(R.id.editTurbineSerialNumber).setOnClickListener(this)

        findViewById<ImageButton>(R.id.take_turbine_serial_picture).setOnClickListener(this)

        addBladeButtons()
    }

    private fun addBladeButtons() {
        findViewById<LinearLayout>(R.id.bladeButtonLayout).let {
            App.database.bladeDao().getBladesByInterventionId(intervention.id)
                .sortedBy { bla -> bla.position }.forEach { bla ->

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
        intent.putExtra(BladeActivity.EXTRA_BLADE, blade)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        bladeLayouts.forEach { lay ->
            val dbBla = App.database.bladeDao().getById(lay.tag as Int)
            lay.findViewById<Button>(R.id.blade_button).text =
                "${dbBla?.position}${if (dbBla?.serial == null) "" else "\n${dbBla?.serial}"}";

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
        }
    }

    private fun takeTurbineSerialPicture() {
        val intent = Intent(this, CameraActivity::class.java)
        var path = "${App.getInterventionPath(intervention)}/turbineSerialPicture"
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_TURBINE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, intervention.id)
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