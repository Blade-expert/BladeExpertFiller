package com.heliopales.bladeexpertfiller.spotcondition.drainhole

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop.DrainBasicsFragment
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop.DrainSeverityFragment

class DrainholeActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val EXTRA_BLADE = "blade"
        const val EXTRA_INTERVENTION_ID = "InterventionId"
    }

    private val TAG = DrainholeActivity::class.java.simpleName

    lateinit var pager: ViewPager2

    lateinit var drain: DrainholeSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drainhole)
        val blade = intent.getParcelableExtra<Blade>(EXTRA_BLADE)!!
        val interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        findViewById<TextView>(R.id.drainhole_blade_label).text =  "${blade.position} - ${blade.serial?:"na"}"

        val dhs = App.database.drainholeDao().getByBladeAndIntervention(blade.id,interventionId)

        if(dhs == null){
            drain = DrainholeSpotCondition(interventionId, blade.id)
            val newId = App.database.drainholeDao().upsertDrainhole(drain)
            drain.localId = newId.toInt()
        }else{
            drain = dhs
        }

        pager = findViewById(R.id.drain_view_pager)
        pager.adapter = DrainPagerAdapter(this)

        Log.d(TAG, "Drain loaded : $drain")

        val tabLayout = findViewById<TabLayout>(R.id.drain_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                INDEX_DRAIN_LOOP_BASE -> tab.text = "Remark"
                INDEX_DRAIN_LOOP_SEVE -> tab.text = "Severity"
            }
        }.attach()

        findViewById<ImageButton>(R.id.drainhole_picture_button).setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.drainhole_picture_button -> {
                val intent = Intent(this, CameraActivity::class.java)
                val path =
                    App.getDrainPath(drain.interventionId, drain.bladeId)
                intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
                intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, drain.interventionId)
                intent.putExtra(CameraActivity.EXTRA_RELATED_ID, drain.localId)
                intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_DRAIN)
                startActivity(intent)
            }
        }
    }

    fun hideKeyboard(){
        val imm: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(pager.windowToken, 0)
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart()")
        super.onRestart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        App.database.drainholeDao().upsertDrainhole(drain)
    }
}



class DrainPagerAdapter( fa: FragmentActivity) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
            when (position) {
                INDEX_DRAIN_LOOP_BASE -> return DrainBasicsFragment()
                INDEX_DRAIN_LOOP_SEVE -> return DrainSeverityFragment()
            }
        return DrainBasicsFragment()

    }
}