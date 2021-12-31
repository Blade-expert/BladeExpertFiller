package com.heliopales.bladeexpertfiller.spotcondition.damages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.spotcondition.damages.editloop.*
import android.view.inputmethod.InputMethodManager
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT


class DamageViewPagerActivity : AppCompatActivity() {

    companion object {
        val EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID = "DamageSpotConditionLocalId"
    }

    private val TAG = DamageViewPagerActivity::class.java.simpleName

    lateinit var pager: ViewPager2

    lateinit var damage: DamageSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_damage_view_pager)

        val damageLocalId = intent.getIntExtra(EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID, -1)
        damage = App.database.damageDao().getDamageByLocalId(damageLocalId)

        pager = findViewById(R.id.damage_view_pager)
        pager.offscreenPageLimit=4
        pager.adapter = DamagePagerAdapter(this, damage)

        Log.i(TAG, "Damage loaded : $damage")

        val tabLayout = findViewById<TabLayout>(R.id.damage_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                INDEX_DAMAGE_LOOP_BASE -> tab.text = "Base"
                INDEX_DAMAGE_LOOP_POSI -> tab.text = "Pos."
                INDEX_DAMAGE_LOOP_DEPT -> tab.text = "Prof."
                INDEX_DAMAGE_LOOP_TYPE -> tab.text = "Type."
                INDEX_DAMAGE_LOOP_SEVE -> tab.text = "Grav."
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
        App.database.damageDao().updateDamage(damage)
    }



}

class DamagePagerAdapter(val fa: FragmentActivity, val damage: DamageSpotCondition) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        when (damage.inheritType) {
            INHERIT_TYPE_DAMAGE_IN -> when (position) {
                INDEX_DAMAGE_LOOP_BASE -> return DamageBasicsFragment()
                INDEX_DAMAGE_LOOP_POSI -> return IndoorPositionFragment()
                INDEX_DAMAGE_LOOP_DEPT -> return IndoorProfileFragment()
                INDEX_DAMAGE_LOOP_TYPE -> return DamageTypeFragment()
                INDEX_DAMAGE_LOOP_SEVE -> return SeverityFragment()
            }
            INHERIT_TYPE_DAMAGE_OUT -> when (position) {
                INDEX_DAMAGE_LOOP_BASE -> return DamageBasicsFragment()
                INDEX_DAMAGE_LOOP_POSI -> return OutdoorPositionFragment()
                INDEX_DAMAGE_LOOP_DEPT -> return OutdoorProfileFragment()
                INDEX_DAMAGE_LOOP_TYPE -> return DamageTypeFragment()
                INDEX_DAMAGE_LOOP_SEVE -> return SeverityFragment()
            }
        }
        return DamageTypeFragment()
    }

}