package com.heliopales.bladeexpertfiller.damages.editloop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.INDEX_DAMAGE_LOOP_TYPE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.damages.DamageViewPagerActivity

class OutdoorProfileFragment : Fragment(), View.OnClickListener {

    private val buttons = mutableListOf<Button>();

    private lateinit var damage: DamageSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        damage = (activity as DamageViewPagerActivity).damage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.touchables.forEach {
            if (it is Button) {
                buttons.add(it)
                it.setOnClickListener(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_outdoor_profile, container, false)
    }


    override fun onClick(v: View?) {
        buttons.forEach {
            if (it == v) {
                if(it.foreground == null){
                    App.database.interventionDao().updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
                }
                it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
            } else {
                it.foreground = null
            }
        }

        if(v is Button){
            when (v.id) {
                R.id.button_opna -> damage.profileDepth = null
                else -> damage.profileDepth = v.tag as String
            }
            (activity as DamageViewPagerActivity).pager.currentItem = INDEX_DAMAGE_LOOP_TYPE
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as DamageViewPagerActivity).hideKeyboard()
        if (damage.profileDepth == null) {
            buttons.forEach {
                if (it.id == R.id.button_opna) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        } else {
            buttons.forEach {
                if (it.tag == damage.profileDepth) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        }
    }
}