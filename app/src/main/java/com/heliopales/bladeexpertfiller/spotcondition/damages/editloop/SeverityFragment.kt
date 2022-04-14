package com.heliopales.bladeexpertfiller.spotcondition.damages.editloop

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity

class SeverityFragment : Fragment(), View.OnClickListener {
    private val TAG = SeverityFragment::class.java.simpleName

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
                if (it.foreground == null) {
                    App.database.interventionDao().updateExportationState(
                        damage.interventionId,
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
                damage.severityId = null
            else
                damage.severityId = v.tag as Int?

            Log.d(TAG, "Severity id selected : ${damage.severityId}")
        }

        activity?.finish()
        startCameraActivity();

    }

    private fun startCameraActivity() {
        val intent = Intent(requireContext(), CameraActivity::class.java)
        val path =
            App.getDamagePath(damage.interventionId, damage.bladeId, damage.localId)

        intent.putExtra(CameraActivity.EXTRA_TEXT, damage.getSummarizedFieldCodeRadiusPosition())
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, damage.interventionId)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, damage.localId)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_DAMAGE)
        if(damage.inheritType == INHERIT_TYPE_DAMAGE_IN)
            intent.putExtra(CameraActivity.EXTRA_FLASH_MODE, ImageCapture.FLASH_MODE_ON)
        startActivity(intent)
    }


}