package com.heliopales.bladeexpertfiller.spotcondition.damages.editloop

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.camera.GalleryActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity
import kotlin.math.roundToInt


class DamageBasicsFragment : Fragment() {

    private lateinit var damage: DamageSpotCondition
    private lateinit var radialPosition: EditText
    private lateinit var longitudinalLength: EditText
    private lateinit var radialLength: EditText
    private lateinit var repetition: EditText
    private lateinit var description: EditText
    private lateinit var spotButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        damage = (activity as DamageViewPagerActivity).damage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radialPosition = view.findViewById(R.id.damage_radial_position)
        longitudinalLength = view.findViewById(R.id.damage_longitudinal_length)
        radialLength = view.findViewById(R.id.damage_radial_length)
        repetition = view.findViewById(R.id.damage_repetition)
        description = view.findViewById(R.id.damage_description)
        spotButton = view.findViewById(R.id.damage_spot_button)
        spotButton.text = damage.spotCode
        if(damage.spotCode == null || damage.spotCode == "ORPHAN" || damage.spotCode == "")
            spotButton.visibility = View.INVISIBLE
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
                    damage.radialPosition =
                        radialPosition.text.toString().replace(",", ".").toFloat()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        longitudinalLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (longitudinalLength.text.isEmpty())
                    damage.longitudinalLength = null
                else
                    damage.longitudinalLength =
                        longitudinalLength.text.toString().replace(",", ".")
                            .toFloat().roundToInt()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        radialLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (radialLength.text.isEmpty())
                    damage.radialLength = null
                else
                    damage.radialLength =
                        radialLength.text.toString().replace(",", ".")
                            .toFloat().roundToInt()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        repetition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (repetition.text.isEmpty())
                    damage.repetition = null
                else
                    damage.repetition = repetition.text.toString().toIntOrNull()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
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
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        spotButton.setOnClickListener {
            val intent = Intent(requireContext(), GalleryActivity::class.java)
            val path =
                App.getDamagePath(damage.interventionId, damage.bladeId, damage.localId)+"/previous"
            intent.putExtra(GalleryActivity.EXTRA_DIRECTORY_PATH, path)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        radialPosition.setText("${damage.radialPosition ?: ""}")
        longitudinalLength.setText("${damage.longitudinalLength ?: ""}")
        radialLength.setText("${damage.radialLength ?: ""}")
        repetition.setText("${damage.repetition ?: ""}")
        description.setText(damage.description ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_damage_basics, container, false)
    }
}