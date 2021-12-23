package com.heliopales.bladeexpertfiller.damages

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
import com.heliopales.bladeexpertfiller.damages.editloop.*

class DamageViewPagerActivity : AppCompatActivity() {

    companion object {
        val EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID = "DamageSpotConditionLocalId"
    }

    private val TAG = DamageViewPagerActivity::class.java.simpleName

    lateinit var pager: ViewPager2

    lateinit var damage: DamageSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_damage_view_pager)

        val damageLocalId = intent.getLongExtra(EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID, -1)
        damage = App.database.spotConditionDao().getDamageByLocalId(damageLocalId)

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

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        App.database.spotConditionDao().updateDamage(damage)
    }



}

class DamagePagerAdapter(val fa: FragmentActivity, val damage: DamageSpotCondition) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        when (damage.inheritType) {
            INHERIT_TYPE_DAMAGE_IN -> when (position) {
                INDEX_DAMAGE_LOOP_BASE -> return DamageBasicsFragment.newInstance("123", "125")
                INDEX_DAMAGE_LOOP_POSI -> return IndoorPositionFragment.newInstance("123", "123")
                INDEX_DAMAGE_LOOP_DEPT -> return IndoorProfileFragment.newInstance("456", "456")
                INDEX_DAMAGE_LOOP_TYPE -> return DamageTypeFragment.newInstance()
                INDEX_DAMAGE_LOOP_SEVE -> return SeverityFragment.newInstance("456", "456")
            }
            INHERIT_TYPE_DAMAGE_OUT -> when (position) {
                INDEX_DAMAGE_LOOP_BASE -> return DamageBasicsFragment.newInstance("123", "125")
                INDEX_DAMAGE_LOOP_POSI -> return OutdoorPositionFragment.newInstance("123", "123")
                INDEX_DAMAGE_LOOP_DEPT -> return OutdoorProfileFragment.newInstance("456", "456")
                INDEX_DAMAGE_LOOP_TYPE -> return DamageTypeFragment.newInstance()
                INDEX_DAMAGE_LOOP_SEVE -> return SeverityFragment.newInstance("456", "456")
            }
        }
        return DamageTypeFragment()
    }

}