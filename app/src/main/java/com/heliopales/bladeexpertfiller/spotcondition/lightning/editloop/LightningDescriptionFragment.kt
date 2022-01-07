package com.heliopales.bladeexpertfiller.spotcondition.lightning.editloop

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningActivity

class LightningDescriptionFragment() : Fragment() {

    private val TAG =  LightningDescriptionFragment::class.java.simpleName

    private lateinit var description: EditText

    private lateinit var lightning: LightningSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate()")
        super.onCreate(savedInstanceState)
        lightning = (activity as LightningActivity).lightning!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lightning_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG,"onViewCreated()")
        super.onViewCreated(view, savedInstanceState)

        description = view.findViewById(R.id.lps_description)
        attachListeners()
    }

    private fun attachListeners() {
        description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (description.text.isBlank())
                    lightning.description = null
                else
                    lightning.description = description.text.toString()
                App.database.interventionDao().updateExportationState(lightning.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })
    }

    override fun onResume() {
        Log.d(TAG,"onResume()")
        super.onResume()
        description.setText("${lightning.description ?: ""}")
        description.postDelayed(Runnable { description.showKeyboard()} , 50)
    }

    fun EditText.showKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        requestFocus()
        inputMethodManager.showSoftInput(this, 0)
        setSelection(length())
    }
}