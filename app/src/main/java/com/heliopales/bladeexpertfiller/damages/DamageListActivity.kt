package com.heliopales.bladeexpertfiller.damages

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
import com.heliopales.bladeexpertfiller.R
import kotlin.math.roundToInt
import kotlin.random.Random

class DamageListActivity : AppCompatActivity(), DamageAdapter.DamageItemListener,
    View.OnClickListener {
    val TAG = this.javaClass.simpleName

    companion object {
        val EXTRA_INTERVENTION_ID = "intervention_id";
        val EXTRA_BLADE_ID = "blade_id";
        val EXTRA_DAMAGE_INOUT = "damage_inout";
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DamageAdapter
    private var bladeId: Int = -1
    private var interventionId: Int = -1

    private lateinit var damages: MutableList<DamageSpotCondition>

    private lateinit var damageInheritType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_damage_list)

        recyclerView = findViewById<RecyclerView>(R.id.damages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bladeId = intent.getIntExtra(EXTRA_BLADE_ID, -1)
        interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        damageInheritType = intent.getStringExtra(EXTRA_DAMAGE_INOUT)!!


        if (damageInheritType == INHERIT_TYPE_DAMAGE_IN) {
            findViewById<TextView>(R.id.damage_list_label).text = "Indoor damages"
        } else if (damageInheritType == INHERIT_TYPE_DAMAGE_OUT) {
            findViewById<TextView>(R.id.damage_list_label).text = "Outdoor damages"
        } else {
            findViewById<TextView>(R.id.damage_list_label).text = "Error - extra value missing"
        }





        findViewById<ImageButton>(R.id.add_damage_button).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        damages = App.database.spotConditionDao().getDamagesByBladeAndInterventionAndInheritType(
            bladeId,
            interventionId,
            damageInheritType
        )
        damages.sortBy { it.radialPosition }

        adapter = DamageAdapter(damages, this)
        recyclerView.adapter = adapter
    }

    override fun onDamageSelected(damage: DamageSpotCondition) {
        Log.i(TAG, "onDamageSelected() : $damage")
        launchDamagePager(damage.localId ?: -1L)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_damage_button -> launchDamagePager(addNewDamage())

        }
    }


    private fun addNewDamage(): Long {
        Log.d(TAG, "addNewDamage()")
        var damage = DamageSpotCondition(damageInheritType)

        with(damage) {
            bladeId = this@DamageListActivity.bladeId
            interventionId = this@DamageListActivity.interventionId
            radialPosition = (Random.nextDouble(0.0, 450.0).roundToInt() / 10.0).toFloat()
            severityId = Random.nextInt(0, 6)
        }
        val newId = App.database.spotConditionDao().insertDamage(damage)

        damages.add(damage);
        damages.sortBy { it.radialPosition }
        adapter.notifyDataSetChanged()



        return newId
    }

    private fun launchDamagePager(damageLocalId: Long) {
        val intent = Intent(this, DamageViewPagerActivity::class.java)
        intent.putExtra(DamageViewPagerActivity.EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID, damageLocalId)
        startActivity(intent)
    }
}