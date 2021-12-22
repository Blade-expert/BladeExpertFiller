package com.heliopales.bladeexpertfiller.damages.editloop

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.damages.DamageViewPagerActivity


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DamageBasicsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DamageBasicsFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var damage: DamageSpotCondition

    private lateinit var radialPosition: EditText
    private lateinit var longitudinalLength: EditText
    private lateinit var radialLength: EditText
    private lateinit var repetition: EditText
    private lateinit var description: EditText

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

        view.findViewById<EditText>(R.id.damage_radial_position)

        radialPosition = view.findViewById<EditText>(R.id.damage_radial_position)
        longitudinalLength = view.findViewById<EditText>(R.id.damage_longitudinal_length)
        radialLength = view.findViewById<EditText>(R.id.damage_radial_length)
        repetition = view.findViewById<EditText>(R.id.damage_repetition)
        description = view.findViewById<EditText>(R.id.damage_description)

        attachListeners()
    }

    private fun attachListeners() {
        radialPosition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (radialPosition.text.isEmpty())
                    damage.radialPosition = null
                else
                    damage.radialPosition = radialPosition.text.toString().toFloat()
            }
        })

        longitudinalLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (longitudinalLength.text.isEmpty())
                    damage.longitudinalLength = null
                else
                    damage.longitudinalLength = longitudinalLength.text.toString().toInt()
            }
        })

        radialLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (radialLength.text.isEmpty())
                    damage.radialLength = null
                else
                    damage.radialLength = radialLength.text.toString().toInt()
            }
        })

        repetition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (repetition.text.isEmpty())
                    damage.repetition = null
                else
                    damage.repetition = repetition.text.toString().toInt()
            }
        })

        description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (description.text.isEmpty())
                    damage.description = null
                else
                    damage.description = description.text.toString()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        radialPosition.setText("${damage.radialPosition ?: ""}")
        longitudinalLength.setText("${damage.longitudinalLength ?: ""}")
        radialLength.setText("${damage.radialLength ?: ""}")
        repetition.setText("${damage.repetition ?: ""}")
        description.setText("${damage.description ?: ""}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_damage_basics, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DamageBasicsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DamageBasicsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}