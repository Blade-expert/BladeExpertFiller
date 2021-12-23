package com.heliopales.bladeexpertfiller.blade

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.damages.DamageListActivity
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.intervention.Intervention


class BladeActivity : AppCompatActivity(), View.OnClickListener {
    val TAG = this.javaClass.simpleName
    companion object {
        val EXTRA_INTERVENTION = "intervention"
        val EXTRA_BLADE = "blade"
    }

    private lateinit var intervention: Intervention;
    private lateinit var blade: Blade;
    private lateinit var database: AppDatabase;
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
        bladeSerialView.text = if (blade.serial == null) "-----" else blade.serial;

        findViewById<ImageButton>(R.id.edit_blade_serial).setOnClickListener(this)
        findViewById<ImageButton>(R.id.take_blade_serial_picture).setOnClickListener(this)
        findViewById<Button>(R.id.see_indoor_damages_button).setOnClickListener(this)
        findViewById<Button>(R.id.see_outdoor_damages_button).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.edit_blade_serial -> showChangeBladeSerialDialog()
            R.id.take_blade_serial_picture -> takeBladeSerialPicture()
            R.id.see_indoor_damages_button -> startIndoorActivity()
            R.id.see_outdoor_damages_button -> startOutdoorActivity()
        }
    }

    private fun startIndoorActivity() {
        Log.d(TAG,"startIndoorActivity()")
        val intent = Intent(this, DamageListActivity::class.java)
        intent.putExtra(DamageListActivity.EXTRA_INTERVENTION_ID, intervention.id)
        intent.putExtra(DamageListActivity.EXTRA_BLADE_ID, blade.id)
        intent.putExtra(DamageListActivity.EXTRA_DAMAGE_INOUT, INHERIT_TYPE_DAMAGE_IN)
        startActivity(intent)
    }

    private fun startOutdoorActivity() {
        val intent = Intent(this, DamageListActivity::class.java)
        intent.putExtra(DamageListActivity.EXTRA_INTERVENTION_ID, intervention.id)
        intent.putExtra(DamageListActivity.EXTRA_BLADE_ID, blade.id)
        intent.putExtra(DamageListActivity.EXTRA_DAMAGE_INOUT, INHERIT_TYPE_DAMAGE_OUT)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(BladeActivity.EXTRA_INTERVENTION, intervention)
        intent.putExtra(BladeActivity.EXTRA_BLADE, blade)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }


    private fun takeBladeSerialPicture() {
        val intent = Intent(this, CameraActivity::class.java)
        var path = "${App.getBladePath(intervention, blade)}/bladeSerialPicture"
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        startActivity(intent)
    }

    private fun showChangeBladeSerialDialog() {
        val dialog = ChangeBladeSerialDialogFragment(blade.serial)
        dialog.listener = object : ChangeBladeSerialDialogFragment.ChangeBladeSerialDialogListener {
            override fun onDialogPositiveClick(serial: String) {
                updateBladeSerialNumber(serial)
            }
            override fun onDialogNegativeClick() {
            }
        }
        dialog.show(supportFragmentManager, "ChangeBladeSerialDialogFragment")
    }

    private fun updateBladeSerialNumber(serial: String) {
        blade.serial = serial
        blade.state = EXPORTATION_STATE_NOT_EXPORTED
        intervention.state = EXPORTATION_STATE_NOT_EXPORTED
        bladeSerialView.text = serial
        database.bladeDao().updateBlade(blade)
        database.interventionDao().updateIntervention(intervention)
    }


}