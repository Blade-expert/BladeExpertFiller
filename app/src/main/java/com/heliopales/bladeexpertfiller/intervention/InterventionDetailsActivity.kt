package com.heliopales.bladeexpertfiller.intervention

import android.app.Activity
import android.content.Intent
import android.graphics.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
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
    private val bladeButtons = mutableListOf<Button>();

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

        if (intervention.changeTurbineSerialAllowed) {
            findViewById<ImageButton>(R.id.editTurbineSerialNumber).setOnClickListener(this)
        } else {
            findViewById<ImageButton>(R.id.editTurbineSerialNumber).visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.take_turbine_serial_picture).setOnClickListener(this)

        addBladeButtons()
    }

    private fun addBladeButtons() {
        findViewById<LinearLayout>(R.id.bladeButtonLayout).let {
            val resources = applicationContext.resources
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(dpToPx(10f))

            App.database.bladeDao().getBladesByInterventionId(intervention.id)
                .sortedBy { bla -> bla.position }.forEach { bla ->
                    val button = Button(this)
                    button.tag = bla.id
                    button.text =
                        "${bla.position}" + if (bla.serial == null) "" else " - ${bla.serial}"
                    button.setPadding(dpToPx(24f), dpToPx(32f), dpToPx(24f), dpToPx(32f))
                    button.textSize = spToPx(8f)
                    button.layoutParams = layoutParams
                    button.setBackgroundColor(getColor(R.color.bulma_link))
                    button.setTextColor(getColor(R.color.bulma_white))
                    button.setOnClickListener {
                        startBladeActivity(bla)
                    }
                    bladeButtons.add(button)
                    it.addView(button)
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
        bladeButtons.forEach { but ->
            val dbBla = App.database.bladeDao().getById(but.tag as Int)
            but.text =
                "${dbBla?.position}" + if (dbBla?.serial == null) "" else " - ${dbBla?.serial}";
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