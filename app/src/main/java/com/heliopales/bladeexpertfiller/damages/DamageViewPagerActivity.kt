package com.heliopales.bladeexpertfiller.damages

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.editloop.*

class DamageViewPagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_damage_view_pager)

        val pager = findViewById<ViewPager2>(R.id.damage_view_pager)
        pager.adapter = DamagePagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.damage_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                0 -> tab.text = "Base"
                1 -> tab.text = "Pos."
                2 -> tab.text = "Prof."
                3 -> tab.text = "Type."
                4 -> tab.text = "Grav."
            }
        }.attach()
    }
}

class DamagePagerAdapter(val fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return DamageBasicsFragment.newInstance("123", "125")
            1 -> return IndoorPositionFragment.newInstance("123", "123")
            2 -> return IndoorProfileFragment.newInstance("456", "456")
            3 -> return DamageTypeFragment.newInstance("ert", "seg")
            4 -> return SeverityFragment.newInstance("456", "456")
            else -> return DamageTypeFragment.newInstance("ert", "seg")
        }
    }

}