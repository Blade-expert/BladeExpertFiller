package com.heliopales.bladeexpertfiller.blade

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageListActivity
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.intervention.InterventionActivity
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.DrainholeActivity
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningActivity
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener


class BladeActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {
    val TAG = BladeActivity.javaClass.simpleName

    companion object {
        const val EXTRA_INTERVENTION = "intervention"
        const val EXTRA_BLADE = "blade"
    }

    private lateinit var intervention: Intervention
    private lateinit var blade: Blade
    private lateinit var database: AppDatabase
    private lateinit var turbineNameView: TextView
    private lateinit var bladeNameView: TextView
    private lateinit var bladeSerialView: TextView
    private lateinit var gestureDetector: GestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blade)

        database = App.database

        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!
        blade = intent.getParcelableExtra(EXTRA_BLADE)!!

        turbineNameView = findViewById(R.id.bla_turbine_name)
        turbineNameView.text = intervention.name

        bladeNameView = findViewById(R.id.blade_name)
        bladeNameView.text = blade.position

        bladeSerialView = findViewById(R.id.blade_serial_number)
        bladeSerialView.text = if (blade.serial == null) "-----" else blade.serial

            findViewById<ImageButton>(R.id.edit_blade_serial).setOnClickListener(this)

        findViewById<ImageButton>(R.id.take_blade_serial_picture).setOnClickListener(this)
        findViewById<Button>(R.id.see_indoor_damages_button).setOnClickListener(this)
        findViewById<Button>(R.id.see_outdoor_damages_button).setOnClickListener(this)
        findViewById<Button>(R.id.see_drainhole_button).setOnClickListener(this)
        findViewById<Button>(R.id.see_lightning_button).setOnClickListener(this)

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

        findViewById<ConstraintLayout>(R.id.blade_main_layout).setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch")
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun startInterventionActivity(){
        val intent = Intent(this, InterventionActivity::class.java)
        intent.putExtra(InterventionActivity.EXTRA_INTERVENTION, intervention)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_top, R.anim.no_anim)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.edit_blade_serial -> showChangeBladeSerialDialog()
            R.id.take_blade_serial_picture -> takeBladeSerialPicture()
            R.id.see_indoor_damages_button -> startIndoorActivity()
            R.id.see_outdoor_damages_button -> startOutdoorActivity()
            R.id.see_drainhole_button -> startDrainholeActivity()
            R.id.see_lightning_button -> startLightningActivity()
        }
    }

    private fun startLightningActivity() {
        val intent = Intent(this, LightningActivity::class.java)
        intent.putExtra(LightningActivity.EXTRA_INTERVENTION_ID, intervention.id)
        intent.putExtra(LightningActivity.EXTRA_BLADE, blade)
        startActivity(intent)
    }

    private fun startDrainholeActivity() {
        val intent = Intent(this, DrainholeActivity::class.java)
        intent.putExtra(DrainholeActivity.EXTRA_INTERVENTION_ID, intervention.id)
        intent.putExtra(DrainholeActivity.EXTRA_BLADE, blade)
        startActivity(intent)
    }

    private fun startIndoorActivity() {
        val intent = Intent(this, DamageListActivity::class.java)
        intent.putExtra(DamageListActivity.EXTRA_INTERVENTION, intervention)
        intent.putExtra(DamageListActivity.EXTRA_BLADE, blade)
        intent.putExtra(DamageListActivity.EXTRA_DAMAGE_INOUT, INHERIT_TYPE_DAMAGE_IN)
        startActivity(intent)
    }

    private fun startOutdoorActivity() {
        val intent = Intent(this, DamageListActivity::class.java)
        intent.putExtra(DamageListActivity.EXTRA_INTERVENTION, intervention)
        intent.putExtra(DamageListActivity.EXTRA_BLADE, blade)
        intent.putExtra(DamageListActivity.EXTRA_DAMAGE_INOUT, INHERIT_TYPE_DAMAGE_OUT)
        startActivity(intent)
    }

    private fun takeBladeSerialPicture() {
        val intent = Intent(this, CameraActivity::class.java)
        var path = App.getBladePicturePath(intervention, blade)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_BLADE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, blade.id)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
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
        bladeSerialView.text = serial
        database.bladeDao().updateBlade(blade)
        App.database.interventionDao()
            .updateExportationState(intervention.id, EXPORTATION_STATE_NOT_EXPORTED)
    }

    override fun onResume() {
        super.onResume()

        var count = 0
        count = App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(blade.id, intervention.id, INHERIT_TYPE_DAMAGE_IN).size
        if(count > 0)
            findViewById<Button>(R.id.see_indoor_damages_button).text = "INT\n($count)"
        else
            findViewById<Button>(R.id.see_indoor_damages_button).text = "INT"

        count = App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(blade.id, intervention.id, INHERIT_TYPE_DAMAGE_OUT).size
        if(count > 0)
            findViewById<Button>(R.id.see_outdoor_damages_button).text = "EXT\n($count)"
        else
            findViewById<Button>(R.id.see_outdoor_damages_button).text = "EXT"

        if(App.database.drainholeDao().getByBladeAndIntervention(blade.id, intervention.id)!=null)
            findViewById<Button>(R.id.see_drainhole_button).text = "DRAIN\n(done)"
        else
            findViewById<Button>(R.id.see_drainhole_button).text = "DRAIN"

        if(App.database.lightningDao().getByBladeAndIntervention(blade.id, intervention.id)!=null)
            findViewById<Button>(R.id.see_lightning_button).text = "LPS\n(done)"
        else
            findViewById<Button>(R.id.see_lightning_button).text = "LPS"


    }


}