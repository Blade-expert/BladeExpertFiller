package com.heliopales.bladeexpertfiller.blade

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.Database
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.intervention.ChangeTurbineSerialDialogFragment
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.intervention.InterventionDetailsActivity
import com.heliopales.bladeexpertfiller.utils.toast


class BladeActivity : AppCompatActivity(), View.OnClickListener {
    companion object{
        val EXTRA_INTERVENTION = "intervention"
        val EXTRA_BLADE = "blade"
    }

    private lateinit var intervention: Intervention;
    private lateinit var blade: Blade;
    private lateinit var database: Database;
    private lateinit var bladeNameView: TextView;
    private lateinit var bladeSerialView: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blade)

        database = App.database;

        intervention = intent.getParcelableExtra<Intervention>(BladeActivity.EXTRA_INTERVENTION)!!
        blade = intent.getParcelableExtra<Blade>(BladeActivity.EXTRA_BLADE)!!

        bladeNameView = findViewById(R.id.blade_name)
        bladeNameView.text = blade.position

        bladeSerialView = findViewById(R.id.blade_serial_number)
        bladeSerialView.text = if(blade.serial == null) "-----" else blade.serial;

        findViewById<ImageButton>(R.id.edit_blade_serial).setOnClickListener(this)
        findViewById<ImageButton>(R.id.take_blade_serial_picture).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.edit_blade_serial -> showChangeBladeSerialDialog()
            R.id.take_blade_serial_picture -> takeBladeSerialPicture()
        }
    }

    override fun onBackPressed() {
        intent = Intent()
        intent.putExtra(BladeActivity.EXTRA_INTERVENTION, intervention)
        intent.putExtra(BladeActivity.EXTRA_BLADE, blade)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }


    private fun takeBladeSerialPicture() {
        TODO("Not yet implemented")
    }

    private fun showChangeBladeSerialDialog() {
        val dialog = ChangeBladeSerialDialogFragment(blade.serial)
        dialog.listener = object: ChangeBladeSerialDialogFragment.ChangeBladeSerialDialogListener{
            override fun onDialogPositiveClick(serial: String) {
                updateBladeSerialNumber(serial)
            }
            override fun onDialogNegativeClick() {
            }
        }
        dialog.show(supportFragmentManager, "ChangeBladeSerialDialogFragment")
    }

    private fun updateBladeSerialNumber(serial: String) {
        if(database.updateBladeSerialNumber(blade, serial)){
            bladeSerialView.text = serial
            blade.serial = serial
            blade.state = EXPORTATION_STATE_NOT_EXPORTED
            intervention.state = EXPORTATION_STATE_NOT_EXPORTED
            database.updateInterventionState(intervention)
            database.updateBladeState(blade)
        }else{
            toast("Impossible de mettre à jour ce numéro")
        }
    }


}