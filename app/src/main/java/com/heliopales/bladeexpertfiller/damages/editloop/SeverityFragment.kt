package com.heliopales.bladeexpertfiller.damages.editloop

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.AppDatabase
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.damages.DamageViewPagerActivity
import com.heliopales.bladeexpertfiller.utils.closeKeyboard

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SeverityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SeverityFragment : Fragment(), View.OnClickListener {
    private val TAG = SeverityFragment::class.java.simpleName

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val buttons = mutableListOf<Button>();

    private lateinit var damage: DamageSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        damage = (activity as DamageViewPagerActivity).damage
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        activity?.closeKeyboard()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SeverityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SeverityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View) {
        buttons.forEach {
            if (it == v) {
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