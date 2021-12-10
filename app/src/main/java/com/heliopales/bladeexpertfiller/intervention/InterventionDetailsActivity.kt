package com.heliopales.bladeexpertfiller.intervention

import android.app.Activity
import android.content.Intent
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
import androidx.core.view.setPadding
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.AppDatabase
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.utils.dpToPx
import com.heliopales.bladeexpertfiller.utils.spToPx
import com.heliopales.bladeexpertfiller.utils.toast

class InterventionDetailsActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val EXTRA_INTERVENTION = "intervention"
    }

    lateinit var currentPhotoPath: String

    private lateinit var intervention: Intervention;
    private lateinit var database: AppDatabase;
    private lateinit var turbineNameView: TextView;
    private lateinit var turbineSerialView: TextView;
    private lateinit var blades: MutableList<Blade>;
    private val bladeButtons = mutableListOf<Button>();

    val TAG = InterventionDetailsActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_details)

        database = App.database;

        intervention = intent.getParcelableExtra<Intervention>(EXTRA_INTERVENTION)!!
        blades = database.bladeDao().getBladesByInterventionId(intervention.id)
            .sortedBy { bla -> bla.position } as MutableList<Blade>

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
            val resources = applicationContext.resources
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(dpToPx(10f))

            blades.forEach { bla ->
                val button = Button(this)
                button.tag = bla
                button.text =
                    "${bla.position}" + if (bla.serial == null) "" else " - ${bla.serial}"
                button.setPadding(dpToPx(24f),dpToPx(32f),dpToPx(24f),dpToPx(32f))
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
        bladeLauncher.launch(intent)
    }

    var bladeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val itvResult =
                    data?.getParcelableExtra<Intervention>(BladeActivity.EXTRA_INTERVENTION)
                val blaResult =
                    data?.getParcelableExtra<Blade>(BladeActivity.EXTRA_BLADE)

                if (itvResult != null) intervention.state = itvResult.state

                bladeButtons.forEach { but ->
                    if (blaResult == but.tag) but.text =
                        "${blaResult?.position}" + if (blaResult?.serial == null) "" else " - ${blaResult?.serial}";
                }

                blades.forEach { bla ->
                    if (bla.equals(blaResult)) {
                        bla.serial = blaResult?.serial
                        bla.state = blaResult?.state
                    }
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

        var path = App.getOutputDirectory()
        path+="/${intervention.id}_${intervention.turbineName?.replace(" ","-")}/turbinePictures"
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)

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
        intervention.state = EXPORTATION_STATE_NOT_EXPORTED

        if (database.interventionDao().updateIntervention(intervention) == 1) {
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