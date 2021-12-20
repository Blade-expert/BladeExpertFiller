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

    private lateinit var inout: String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_damage_list)

        recyclerView = findViewById<RecyclerView>(R.id.damages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bladeId = intent.getIntExtra(EXTRA_BLADE_ID, -1)
        interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        inout = intent.getStringExtra(EXTRA_DAMAGE_INOUT)!!


        if (inout == INHERIT_TYPE_DAMAGE_IN) {
            findViewById<TextView>(R.id.damage_list_label).text = "Indoor damages"
        } else if (inout == INHERIT_TYPE_DAMAGE_OUT) {
            findViewById<TextView>(R.id.damage_list_label).text = "Outdoor damages"
        } else {
            findViewById<TextView>(R.id.damage_list_label).text = "Error - extra value missing"
        }

        val damage1 = DamageSpotCondition(INHERIT_TYPE_DAMAGE_IN)
        with(damage1) {
            id = 102
            radialPosition = 14f
        }

        val damage2 = DamageSpotCondition(INHERIT_TYPE_DAMAGE_IN)
        with(damage2) {
            id = 103
            radialPosition = 28f
            severityId = 3
        }

        val damage3 = DamageSpotCondition(INHERIT_TYPE_DAMAGE_IN)
        with(damage3) {
            id = 105
            radialPosition = 20f
            severityId = 4
        }

        val damage4 = DamageSpotCondition(INHERIT_TYPE_DAMAGE_IN)
        with(damage4) {
            id = 112
            severityId = 1
        }

        damages = mutableListOf(damage1, damage4, damage2, damage3)
        damages.sortBy { it.radialPosition }

        adapter = DamageAdapter(damages, this)
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.add_damage_button).setOnClickListener(this)
    }

    override fun onDamageSelected(damage: DamageSpotCondition) {
        Log.i(TAG, "onDamageSelected() : $damage")
        launchDamagePager(damage)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_damage_button -> launchDamagePager(addNewDamage())

        }
    }


    private fun addNewDamage(): DamageSpotCondition {
        Log.d(TAG, "addNewDamage()")
        var damage = DamageSpotCondition(inout)

        with(damage) {
            bladeId = this@DamageListActivity.bladeId
            interventionId = this@DamageListActivity.interventionId
            radialPosition = (Random.nextDouble(0.0, 450.0).roundToInt() / 10.0).toFloat()
        }

        damages.add(damage);
        damages.sortBy { it.radialPosition }
        adapter.notifyDataSetChanged()

        return damage
    }

    private fun launchDamagePager(damage: DamageSpotCondition) {
        val intent = Intent(this, DamageViewPagerActivity::class.java)
        startActivity(intent)
    }
}