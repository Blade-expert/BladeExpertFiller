package com.heliopales.bladeexpertfiller.damages.editloop

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.damages.DamageViewPagerActivity

class SeverityFragment : Fragment(), View.OnClickListener {
    private val TAG = SeverityFragment::class.java.simpleName

    private var param1: String? = null
    private var param2: String? = null

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
        val severities =
            App.database.severityDao().getAll().sortedBy { it.id }.forEachIndexed { i, sev ->
                buttons[i].backgroundTintList = ColorStateList.valueOf(Color.parseColor(sev.color))
                buttons[i].setTextColor(Color.parseColor(sev.fontColor))
                buttons[i].tag = sev.id
            }


    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as DamageViewPagerActivity).hideKeyboard()
        if (damage.severityId == null) {
            buttons.forEach {
                if (it.id == R.id.button_sev_na) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        } else {
            buttons.forEach {
                if (it.tag == damage.severityId) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_severity, container, false)
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
        if (v is Button) {
            if (v.id == R.id.button_sev_na)
                damage.severityId = null
            else
                damage.severityId = v.tag as Int?

            Log.d(TAG, "Severity id selected : ${damage.severityId}")
        }

        activity?.finish()

    }


}