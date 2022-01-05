package com.heliopales.bladeexpertfiller.spotcondition.damages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT

class DamageListActivity : AppCompatActivity(), DamageAdapter.DamageItemListener,
    View.OnClickListener {
    val TAG = DamageListActivity::class.java.simpleName

    companion object {
        val EXTRA_INTERVENTION_ID = "intervention_id";
        val EXTRA_BLADE = "blade";
        val EXTRA_DAMAGE_INOUT = "damage_inout";
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DamageAdapter
    private lateinit var blade: Blade;
    private var interventionId: Int = -1

    private lateinit var damages: MutableList<DamageSpotCondition>

    private lateinit var damageInheritType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_damage_list)

        recyclerView = findViewById(R.id.damages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        blade = intent.getParcelableExtra<Blade>(EXTRA_BLADE)!!
        interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        damageInheritType = intent.getStringExtra(EXTRA_DAMAGE_INOUT)!!

        findViewById<TextView>(R.id.damage_blade_title).text = "${blade.position} - ${blade.serial?:"na"}"

        when(damageInheritType){
            INHERIT_TYPE_DAMAGE_IN -> findViewById<TextView>(R.id.damage_list_label).text = "INTERIOR"
            INHERIT_TYPE_DAMAGE_OUT ->  findViewById<TextView>(R.id.damage_list_label).text = "EXTERIOR"
            else -> findViewById<TextView>(R.id.damage_list_label).text = "ERROR"
        }

        findViewById<ImageButton>(R.id.add_damage_button).setOnClickListener(this)
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        damages = App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
            blade.id,
            interventionId,
            damageInheritType
        )
        damages.sortBy { it.radialPosition }

        adapter = DamageAdapter(damages, this)
        recyclerView.adapter = adapter
    }

    override fun onDamageSelected(damage: DamageSpotCondition) {
        launchDamagePager(damage.localId ?: -1)
    }

    override fun onCameraButtonClicked(damage: DamageSpotCondition) {
        val intent = Intent(this, CameraActivity::class.java)
        var path = "${App.getDamagePath(interventionId, blade.id, damage.localId)}"
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_DAMAGE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, damage.localId)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, interventionId)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        startActivity(intent)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_damage_button -> launchDamagePager(addNewDamage())

        }
    }


    private fun addNewDamage(): Int {
        Log.d(TAG, "addNewDamage()")

        var damage = DamageSpotCondition(
            damageInheritType,
            "D${adapter.itemCount + 1}",
            interventionId,
            blade.id
        )
        val newId = App.database.damageDao().insertDamage(damage).toInt()
        damage.localId = newId
        damages.add(damage)
        damages.sortBy { it.radialPosition }

        damages.forEach {
            Log.d(TAG,"damage in list : id = ${damage.localId}")
        }

        adapter.notifyDataSetChanged()

        return newId
    }

    private fun launchDamagePager(damageLocalId: Int) {
        val intent = Intent(this, DamageViewPagerActivity::class.java)
        intent.putExtra(DamageViewPagerActivity.EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID, damageLocalId)
        startActivity(intent)
    }
}