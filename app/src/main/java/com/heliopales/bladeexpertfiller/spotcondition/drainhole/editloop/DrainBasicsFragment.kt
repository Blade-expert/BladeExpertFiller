package com.heliopales.bladeexpertfiller.spotcondition.drainhole.editloop

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DRAIN
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.drainhole.DrainholeActivity


class DrainBasicsFragment : Fragment() {

    private lateinit var drain: DrainholeSpotCondition

    private lateinit var description: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drain = (activity as DrainholeActivity).drain
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        description = view.findViewById(R.id.drain_description)
        attachListeners()
    }

    private fun attachListeners() {

        description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (description.text.isEmpty())
                    drain.description = null
                else
                    drain.description = description.text.toString()
                App.database.interventionDao().updateExportationState(drain.interventionId, EXPORTATION_STATE_NOT_EXPORTED)

            }
        })

    }

    override fun onResume() {
        super.onResume()
        description.setText(drain.description ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drain_basics, container, false)
    }
}