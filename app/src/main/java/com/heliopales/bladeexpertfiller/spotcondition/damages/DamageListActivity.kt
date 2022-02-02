package com.heliopales.bladeexpertfiller.spotcondition.damages

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.blade.BladeActivity
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.intervention.InterventionActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener

class DamageListActivity : AppCompatActivity(), DamageAdapter.DamageItemListener,
    View.OnClickListener, View.OnTouchListener {
    val TAG = DamageListActivity::class.java.simpleName

    companion object {
        val EXTRA_INTERVENTION = "intervention";
        val EXTRA_BLADE = "blade";
        val EXTRA_DAMAGE_INOUT = "damage_inout";
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DamageAdapter
    private lateinit var blade: Blade;
    private lateinit var intervention: Intervention
    private lateinit var gestureDetector: GestureDetector
    private lateinit var damages: MutableList<DamageSpotCondition>

    private lateinit var damageInheritType: String

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_damage_list)

        recyclerView = findViewById(R.id.damages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        blade = intent.getParcelableExtra(EXTRA_BLADE)!!
        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!

        damageInheritType = intent.getStringExtra(EXTRA_DAMAGE_INOUT)!!

        findViewById<TextView>(R.id.damage_blade_title).text = "${blade.position} - ${blade.serial?:"na"}"

        when(damageInheritType){
            INHERIT_TYPE_DAMAGE_IN -> findViewById<TextView>(R.id.damage_list_label).text = "INTERNAL"
            INHERIT_TYPE_DAMAGE_OUT ->  findViewById<TextView>(R.id.damage_list_label).text = "EXTERNAL"
            else -> findViewById<TextView>(R.id.damage_list_label).text = "ERROR"
        }

        findViewById<ImageButton>(R.id.add_damage_button).setOnClickListener(this)

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
                return true;
            }
        });

        findViewById<RelativeLayout>(R.id.damage_list_main_layout).setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch")
        gestureDetector.onTouchEvent(event)
        return true;
    }

    fun startInterventionActivity(){
        val intent = Intent(this, InterventionActivity::class.java)
        intent.putExtra(InterventionActivity.EXTRA_INTERVENTION, intervention)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_top, R.anim.no_anim);
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        damages = App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
            blade.id,
            intervention.id,
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
        var path = "${App.getDamagePath(intervention, blade, damage.localId)}"
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_DAMAGE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, damage.localId)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
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

        val prefix = if(damageInheritType == INHERIT_TYPE_DAMAGE_OUT) "D" else "i"

        var damage = DamageSpotCondition(
            damageInheritType,
            "$prefix${adapter.itemCount + 1}",
            intervention.id,
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