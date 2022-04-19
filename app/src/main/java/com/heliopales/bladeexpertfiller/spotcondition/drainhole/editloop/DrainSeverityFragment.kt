package com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop

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
import com.heliopales.bladeexpertfiller.INDEX_DRAIN_LOOP_BASE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.DrainholeActivity
import kotlinx.android.synthetic.main.fragment_drain_severity.*

class DrainSeverityFragment : Fragment(), View.OnClickListener {
    private val TAG = DrainSeverityFragment::class.java.simpleName

    private val buttons = mutableListOf<Button>()

    private lateinit var drain: DrainholeSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drain = (activity as DrainholeActivity).drain
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.touchables.forEach {
            if (it is Button) {
                buttons.add(it)
                it.setOnClickListener(this)
            }
        }

        val sevs = App.database.severityDao().getAll().sortedBy { it.id }

        view.findViewById<Button>(R.id.button_sev_1).backgroundTintList = ColorStateList.valueOf(Color.parseColor(sevs[0].color))
        view.findViewById<Button>(R.id.button_sev_1).setTextColor(Color.parseColor(sevs[0].fontColor))
        view.findViewById<Button>(R.id.button_sev_1).tag = sevs[0].id

        view.findViewById<Button>(R.id.button_sev_2).backgroundTintList = ColorStateList.valueOf(Color.parseColor(sevs[1].color))
        view.findViewById<Button>(R.id.button_sev_2).setTextColor(Color.parseColor(sevs[1].fontColor))
        view.findViewById<Button>(R.id.button_sev_2).tag = sevs[1].id

        view.findViewById<Button>(R.id.button_sev_4).backgroundTintList = ColorStateList.valueOf(Color.parseColor(sevs[3].color))
        view.findViewById<Button>(R.id.button_sev_4).setTextColor(Color.parseColor(sevs[3].fontColor))
        view.findViewById<Button>(R.id.button_sev_4).tag = sevs[3].id


    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as DrainholeActivity).hideKeyboard()
        if (drain.severityId == null) {
            buttons.forEach {
                if (it.id == R.id.button_sev_na) {
                    it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                } else {
                    it.foreground = null
                }
            }
        } else {
            buttons.forEach {
                if (it.tag == drain.severityId) {
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
        return inflater.inflate(R.layout.fragment_drain_severity, container, false)
    }

    override fun onClick(v: View) {
        buttons.forEach {
            if (it == v) {
                if (it.foreground == null) {
                    App.database.interventionDao().updateExportationState(
                        drain.interventionId,
                        EXPORTATION_STATE_NOT_EXPORTED
                    )
                }
                it.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
            } else {
                it.foreground = null
            }
        }
        if (v is Button) {
            if (v.id == R.id.button_sev_na)
                drain.severityId = null
            else
                drain.severityId = v.tag as Int?

            Log.d(TAG, "Severity id selected : ${drain.severityId}")
        }
        (activity as DrainholeActivity).pager.currentItem = INDEX_DRAIN_LOOP_BASE
    }


}