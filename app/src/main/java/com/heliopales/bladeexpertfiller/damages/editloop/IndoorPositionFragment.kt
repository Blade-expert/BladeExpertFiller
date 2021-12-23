package com.heliopales.bladeexpertfiller.damages.editloop

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.heliopales.bladeexpertfiller.INDEX_DAMAGE_LOOP_DEPT
import com.heliopales.bladeexpertfiller.INDEX_DAMAGE_LOOP_TYPE
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
 * Use the [IndoorPositionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IndoorPositionFragment : Fragment(), View.OnClickListener {
    private val TAG = IndoorPositionFragment::class.java.simpleName

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_indoor_position, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IndoorPositionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IndoorPositionFragment().apply {
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
        activity?.closeKeyboard()
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