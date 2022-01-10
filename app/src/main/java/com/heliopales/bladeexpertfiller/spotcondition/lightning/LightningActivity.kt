package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.lightning.editloop.LightningDescriptionFragment
import com.heliopales.bladeexpertfiller.spotcondition.lightning.editloop.LightningMeasureFragment


class LightningActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = LightningActivity::class.java.simpleName

    companion object {
        const val EXTRA_BLADE = "Blade"
        const val EXTRA_INTERVENTION_ID = "InterventionId"
    }

    lateinit var lightning: LightningSpotCondition

    val measures: MutableList<ReceptorMeasure> = mutableListOf()

    lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning)

        val blade = intent.getParcelableExtra<Blade>(EXTRA_BLADE)!!
        val interventionId = intent.getIntExtra(EXTRA_INTERVENTION_ID, -1)

        findViewById<TextView>(R.id.lps_blade_label).text =
            "${blade.position} - ${blade.serial ?: "na"}"

        val receptors = App.database.receptorDao().getByBladeId(blade.id)
            .sortedWith(compareBy({ it.radius }, { it.position }))

        val lps = App.database.lightningDao().getByBladeAndIntervention(blade.id, interventionId)

        if (lps == null) {
            lightning = LightningSpotCondition(interventionId, blade.id)
            lightning.measureMethod = MeasureMethod.RCPT_TO_NAC_200MA
            val newId = App.database.lightningDao().upsert(lightning!!)
            lightning.localId = newId.toInt()

            receptors.forEach {
                val measure = ReceptorMeasure(
                    lightningSpotConditionLocalId = lightning!!.localId,
                    receptorId = it.id
                )
                App.database.receptorMeasureDao().upsert(measure)
                measure.receptorLabel = "${it.radius} m  |  ${it.position}"
                measures.add(measure)
            }
        } else {
            lightning = lps
            receptors.forEach {
                val measure = App.database.receptorMeasureDao()
                    .getByReceptorIdAndLightningId(it.id, lightning!!.localId)
                measure.receptorLabel = "${it.radius} m  |  ${it.position}"
                measures.add(measure)
            }
        }

        measures.forEach {
            Log.d(TAG, it.toString())
        }

        findViewById<ImageButton>(R.id.lps_picture_button).setOnClickListener(this)

        pager = findViewById(R.id.lps_view_pager)
        pager.adapter = LightningPagerAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.lps_tab_layout)
        TabLayoutMediator(tabLayout, pager) { tab, position ->
            when (position) {
                INDEX_LIGHTNING_LOOP_MEAS -> tab.text = "Measures"
                INDEX_LIGHTNING_LOOP_DESC -> tab.text = "Remark"
            }
        }.attach()

    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        App.database.lightningDao().upsert(lightning!!)
        App.database.receptorMeasureDao().upsert(measures)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lps_picture_button -> {
                val intent = Intent(this, CameraActivity::class.java)
                var path =
                    "${App.getLPSPath(lightning.interventionId, lightning.bladeId)}"
                intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
                intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, lightning.interventionId)
                intent.putExtra(CameraActivity.EXTRA_RELATED_ID, lightning.localId)
                intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_LPS)
                startActivity(intent)
            }
        }
    }

}

class LightningPagerAdapter(fa: FragmentActivity) :
    FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        when (position) {
            INDEX_LIGHTNING_LOOP_MEAS -> return LightningMeasureFragment()
            INDEX_LIGHTNING_LOOP_DESC -> return LightningDescriptionFragment()
        }
        return LightningMeasureFragment()

    }
}