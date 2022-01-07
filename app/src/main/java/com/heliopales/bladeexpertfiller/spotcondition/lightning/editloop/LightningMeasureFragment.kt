package com.heliopales.bladeexpertfiller.spotcondition.lightning.editloop

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningActivity
import com.heliopales.bladeexpertfiller.spotcondition.lightning.MeasureMethod
import com.heliopales.bladeexpertfiller.spotcondition.lightning.ReceptorMeasure
import com.heliopales.bladeexpertfiller.spotcondition.lightning.ReceptorMeasureAdapter
import android.widget.AdapterView

import android.widget.Toast




class LightningMeasureFragment : Fragment(), ReceptorMeasureAdapter.MeasureChangeListener {

    private val TAG =LightningMeasureFragment::class.java.simpleName

    private lateinit var lightning: LightningSpotCondition
    private lateinit var measures: List<ReceptorMeasure>

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceptorMeasureAdapter

    private lateinit var measureMethodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lightning = (activity as LightningActivity).lightning!!
        measures = (activity as LightningActivity).measures!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lightning_measure, container, false)
    }

    private var spinnerInitialized = false;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ReceptorMeasureAdapter(measures, this)
        recyclerView = view.findViewById(R.id.receptors_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        measureMethodSpinner = view.findViewById(R.id.measure_method_spinner)

        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            MeasureMethod.values()
        )
        measureMethodSpinner.adapter =arrayAdapter

        measureMethodSpinner.setSelection(arrayAdapter.getPosition(lightning.measureMethod))

        measureMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(!spinnerInitialized){
                    spinnerInitialized = true;
                    return
                }
                Log.d(TAG,"selected item $position ${measureMethodSpinner.adapter.getItem(position)}")
               lightning.measureMethod = measureMethodSpinner.adapter.getItem(position) as MeasureMethod
            }

        }
    }

    override fun OnMeasureChanged(position: Int, receptorValue: String) {
        if (receptorValue.isBlank())
            measures[position].value = null;
        else
            measures[position].value = receptorValue
        App.database.interventionDao().updateExportationState(lightning.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
    }


}