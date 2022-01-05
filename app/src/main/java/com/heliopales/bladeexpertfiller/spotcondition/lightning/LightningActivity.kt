package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import android.widget.ArrayAdapter
import com.heliopales.bladeexpertfiller.intervention.ChangeTurbineSerialDialogFragment


class LightningActivity : AppCompatActivity(), View.OnClickListener,
    ReceptorMeasureAdapter.MeasureItemListener {

    private val TAG = LightningActivity::class.java.simpleName

    companion object {
        const val EXTRA_BLADE_ID = "BladeId"
        const val EXTRA_INTERVENTION_ID = "InterventionId"
    }

    var lightning: LightningSpotCondition? = null

    private val measures: MutableList<ReceptorMeasure> = mutableListOf()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceptorMeasureAdapter

    private lateinit var measureMethodSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lightning)

        val bladeId = intent.getIntExtra(LightningActivity.EXTRA_BLADE_ID, -1)
        val interventionId = intent.getIntExtra(LightningActivity.EXTRA_INTERVENTION_ID, -1)

        val receptors = App.database.receptorDao().getByBladeId(bladeId)
            .sortedWith(compareBy({ it.radius }, { it.position }))

        lightning = App.database.lightningDao().getByBladeAndIntervention(bladeId, interventionId)

        if (lightning == null) {
            lightning = LightningSpotCondition(interventionId, bladeId)
            val newId = App.database.lightningDao().upsert(lightning!!)
            lightning!!.localId = newId.toInt()

            receptors.forEach {
                val measure = ReceptorMeasure(
                    lightningSpotConditionId = lightning!!.localId,
                    receptorId = it.id
                )
                App.database.receptorMeasureDao().upsert(measure)
                measure.receptorLabel = "${it.radius} m  |  ${it.position}"
                measures.add(measure)
            }
        } else {
            receptors.forEach {
                val measure = App.database.receptorMeasureDao()
                    .getByReceptorIdAndLightningId(it.id, lightning!!.localId)
                measure.receptorLabel = "${it.radius} m  |  ${it.position}"
                measures.add(measure)
            }
        }

        measures.forEach {
            Log.d(TAG, it.toString())
        }
        adapter = ReceptorMeasureAdapter(measures, this)
        recyclerView = findViewById(R.id.receptors_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        measureMethodSpinner = findViewById(R.id.measure_method_spinner)
        measureMethodSpinner.adapter =
            ArrayAdapter<MeasureMethod>(
                this,
                R.layout.item_spinner,
                MeasureMethod.values()
            )

        findViewById<ImageButton>(R.id.lps_description_button).setOnClickListener(this)
        findViewById<ImageButton>(R.id.lps_picture_button).setOnClickListener(this)

    }

    override fun onPause() {
        Log.d(TAG, "onPause()")
        super.onPause()
        App.database.lightningDao().upsert(lightning!!)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.lps_description_button -> showChangeDescriptionDialog()
            R.id.lps_picture_button -> {}
        }
    }

    private fun showChangeDescriptionDialog() {
        val dialog = LightningDescriptionFragment(lightning?.description)
        dialog.show(supportFragmentManager, "LightningDescriptionFragment")
    }

    override fun onMearureSelected(measure: ReceptorMeasure) {
        AlertDialog.Builder(this)
            .setMessage("Not yet implemented...")
            .create()
            .show()
    }
}