package com.heliopales.bladeexpertfiller.spotcondition.damages.editloop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity


class IndoorPositionFragment : Fragment(), View.OnClickListener {
    private val TAG = IndoorPositionFragment::class.java.simpleName

    private val buttons = mutableListOf<Button>()

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
        return inflater.inflate(R.layout.fragment_indoor_position, container, false)
    }

    override fun onClick(v: View) {
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
                R.id.button_ina -> damage.position = null
                else -> damage.position = v.tag as String
            }
            when (v.id) {
                R.id.button_ips, R.id.button_iss, R.id.button_iweb -> (activity as DamageViewPagerActivity).pager.currentItem =
                    INDEX_DAMAGE_LOOP_DEPT
                else -> (activity as DamageViewPagerActivity).pager.currentItem =
                    INDEX_DAMAGE_LOOP_TYPE
            }
        }


    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
        (requireActivity() as DamageViewPagerActivity).hideKeyboard()
        if (damage.position == null) {
            buttons.forEach {
                if (it.id == R.id.button_ina) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        } else {
            buttons.forEach {
                if (it.tag == damage.position) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        }
    }

}