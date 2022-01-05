package com.heliopales.bladeexpertfiller.spotcondition.drainhole

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.INDEX_DRAIN_LOOP_BASE
import com.heliopales.bladeexpertfiller.INDEX_DRAIN_LOOP_SEVE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop.DrainBasicsFragment
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop.DrainSeverityFragment

class DrainholeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BLADE = "blade"
        const val EXTRA_INTERVENTION_ID = "InterventionId"
    }

    private val TAG = DrainholeActivity::class.java.simpleName

    lateinit var pager: ViewPager2

    var drain: DrainholeSpotCondition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drainhole)
        val blade = intent.getParcelableExtra<Blade>(EXTRA_BLADE)!!
        val interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        findViewById<TextView>(R.id.drainhole_blade_label).text =  "${blade.position} - ${blade.serial?:"na"}"

        drain = App.database.drainholeDao().getByBladeAndIntervention(blade.id,interventionId)

        Log.d(TAG, "Drain form database : $drain")

        if(drain == null){
            drain = DrainholeSpotCondition(interventionId, blade.id)
            val newId = App.database.drainholeDao().upsertDrainhole(drain!!)
            drain!!.localId = newId.toInt()
        }

        pager = findViewById(R.id.drain_view_pager)
        pager.adapter = DrainPagerAdapter(this)

        Log.d(TAG, "Drain loaded : $drain")

        val tabLayout = findViewById<TabLayout>(R.id.drain_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                INDEX_DRAIN_LOOP_BASE -> tab.text = "Basics"
                INDEX_DRAIN_LOOP_SEVE -> tab.text = "Severity"
            }
        }.attach()

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
        App.database.drainholeDao().upsertDrainhole(drain!!)
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