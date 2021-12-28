package com.heliopales.bladeexpertfiller.intervention

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EXPORTED
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Response
import java.lang.Thread.sleep


class InterventionListActivity : AppCompatActivity(), InterventionAdapter.InterventionItemListener {

    val TAG = InterventionListActivity::class.java.simpleName

    private lateinit var interventions: MutableList<Intervention>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: InterventionAdapter


    private var deleteAllowed = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_list)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        recyclerView = findViewById<RecyclerView>(R.id.interventions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        interventions = mutableListOf()

        adapter = InterventionAdapter(interventions, this, this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(simpleCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        refreshLayout = findViewById(R.id.swipe_layout)
        refreshLayout.setOnRefreshListener { updateInterventionList() }

        updateInterventionList()
        updateSeverities()
        updateDamageTypes()
    }

    val simpleCallBack = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            preDeleteIntervention(interventions[position], position);
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val position = viewHolder.absoluteAdapterPosition
            if (!interventions[position].expired && !deleteAllowed) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            RecyclerViewSwipeDecorator.Builder(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
                .addBackgroundColor(getColor(R.color.bulma_danger))
                .addActionIcon(R.drawable.ic_baseline_delete_24)
                .create()
                .decorate()
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun preDeleteIntervention(deletedIntervention: Intervention, position: Int) {
        interventions.remove(deletedIntervention)
        adapter.notifyItemRemoved(position)
        Snackbar.make(
            recyclerView,
            "${deletedIntervention.turbineName} supprimé",
            Snackbar.LENGTH_SHORT
        )
            .setAction("Annuler") {
                interventions.add(position, deletedIntervention)
                adapter.notifyItemInserted(position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                        event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE
                    ) {
                        effectivelyDeleteIntervention(deletedIntervention)
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    private fun effectivelyDeleteIntervention(deletedIntervention: Intervention) {
        App.database.bladeDao().deleteBladesOfInterventionId(deletedIntervention.id)
        App.database.interventionDao().deleteIntervention(deletedIntervention)
    }


    private fun updateInterventionList() {
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
        interventions.clear()
        interventions.addAll(
            App.database.interventionDao().getAllInterventions().sortedBy { it.turbineName })

        App.bladeExpertService.getInterventions()
            .enqueue(object : retrofit2.Callback<Array<InterventionWrapper>> {
                override fun onResponse(
                    call: Call<Array<InterventionWrapper>>,
                    response: Response<Array<InterventionWrapper>>
                ) {
                    val newInterventions: MutableList<Intervention> = mutableListOf()

                    response?.body().let {
                        newInterventions.addAll(it?.map { itv -> mapBladeExpertIntervention(itv) } as MutableList)
                        it.forEach { itvw ->
                            App.database.bladeDao().insertNonExistingBlades(
                                itvw.blades!!.map { bw -> mapBladeExpertBlade(bw) }
                            )
                        }
                    }
                    interventions.forEach { it.expired = !newInterventions.contains(it) }
                    newInterventions.forEach { if (!interventions.contains(it)) interventions.add(it) }
                    interventions.forEach {
                        App.database.interventionDao().insertNonExistingIntervention(it)
                    }
                    interventions.sortBy { it.turbineName }
                    adapter.notifyDataSetChanged()
                    toast("La liste d'interventions est à jour")
                    refreshLayout.isRefreshing = false
                }

                override fun onFailure(call: Call<Array<InterventionWrapper>>, t: Throwable) {
                    toast("Impossible de mettre à jour les interventions")
                    refreshLayout.isRefreshing = false
                }
            })
    }

    override fun onInterventionSelected(intervention: Intervention) {
        val intent = Intent(this, InterventionDetailsActivity::class.java)
        intent.putExtra(InterventionDetailsActivity.EXTRA_INTERVENTION, intervention)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        interventions.forEach {
            with(it) {
                val dbitv = App.database.interventionDao().getById(id)
                turbineSerial = dbitv.turbineSerial
                it.exportationState = dbitv.exportationState
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onInterventionUploadClick(
        intervention: Intervention
    ) {

        intervention.exporting = true
        adapter.notifyDataSetChanged()

        var count = 0;
        val numberOfOperations = MutableLiveData<Int>(0)
        val realizedOperations = MutableLiveData<Int>(0)


        val export = Thread {
            sleep(1000)
            //EXPORT TURBINE SERIAL
            App.bladeExpertService.updateTurbineSerial(
                interventionWrapper = mapToBladeExpertIntervention(
                    intervention
                )
            )
                .enqueue(object : retrofit2.Callback<Boolean> {
                    override fun onResponse(
                        call: Call<Boolean>,
                        response: Response<Boolean>
                    ) {
                        if (response.isSuccessful)
                            Log.d(TAG, "Turbine serial updated with feedback = ${response.body()}")
                        realizedOperations.postValue(++count)
                    }

                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        realizedOperations.postValue(++count)
                    }
                })


            App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
                sleep(1000)
                App.bladeExpertService.updateBladeSerial(bladeWrapper = mapToBladeExpertBlade(bla))
                    .enqueue(object : retrofit2.Callback<Boolean> {
                        override fun onResponse(
                            call: Call<Boolean>,
                            response: Response<Boolean>
                        ) {
                            realizedOperations.postValue(++count)
                        }

                        override fun onFailure(call: Call<Boolean>, t: Throwable) {
                            realizedOperations.postValue(++count)
                        }
                    })

                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_IN
                ).forEach { dsc ->
                    App.bladeExpertService.saveIndoorDamageSpotCondition(
                        mapToBladeExpertDamageSpotCondition(dsc))
                }
            }

        }

        numberOfOperations.observe(this, Observer {
            Log.d(TAG, "numberOfOperationsObserver newVal = =$it")
            if (it == 0) return@Observer;
            export.start();
        })

        realizedOperations.observe(this, Observer { newVal ->
            Log.d(TAG, "realizedOperationObserver newVal=$newVal")
            if (newVal == 0) return@Observer;
            var percent = newVal.toFloat() / numberOfOperations.value!! * 100;
            Log.d(TAG, "percent $percent% done")
            intervention.progress.postValue(percent.toInt())
            if (newVal == numberOfOperations.value!!) {
                intervention.exporting = false
                intervention.exportationState = EXPORTATION_STATE_EXPORTED
                App.database.interventionDao()
                    .updateExportationState(intervention.id, EXPORTATION_STATE_EXPORTED)

                runOnUiThread { adapter.notifyDataSetChanged() }
            }
        })

        Thread {
            numberOfOperations.postValue(countOperationsForExport(intervention))
        }.start()


    }


    fun countOperationsForExport(intervention: Intervention): Int {
        Log.d(TAG, "countOperationsForExport()")
        var count = 0;

        if (intervention != null) {
            count++ //turbineSerial
            App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
                count++ // BladeSerial
                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_IN
                ).forEach { dsc ->
                    count++ //IndoorDamageSpotCondition
                    /*App.database.pictureDao().getDamageSpotPicturesByDamageId(dsc.localId!!.toInt()).forEach { pic->
                        if(pic.exportState == EXPORTATION_STATE_NOT_EXPORTED)
                            count++ //picture
                    }*/
                }
            }
        }
        Log.d(TAG, "There will be $count operations")
        return count;
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.i(TAG, "onCreateOptionsMenu()")
        menuInflater.inflate(R.menu.activity_intervention_list, menu)
        val menuItem = menu?.findItem(R.id.toogler_delete_ongoing_intervention)
        menuItem?.setActionView(R.layout.menu_switch)

        val switch = menuItem?.actionView?.findViewById<Switch>(R.id.switch_show_protected)
        switch?.isChecked = deleteAllowed
        switch?.setOnCheckedChangeListener { _, isChecked ->
            deleteAllowed = isChecked
            if (isChecked) {
                toast(
                    "La suppression de n'importe quelle intervention est maintenant permise",
                    Toast.LENGTH_LONG
                )
            } else {
                toast(
                    "La suppression est restreinte aux interventions qui ne sont plus 'ONGOING'",
                    Toast.LENGTH_LONG
                )
            }
        }
        return true;
    }


    private fun updateSeverities() {
        Log.d(TAG, "updateSeverities()")
        App.bladeExpertService.getSeverities()
            .enqueue(object : retrofit2.Callback<Array<SeverityWrapper>> {
                override fun onResponse(
                    call: Call<Array<SeverityWrapper>>,
                    response: Response<Array<SeverityWrapper>>
                ) {
                    response?.body().let {
                        it?.map { sevw -> mapBladeExpertSeverity(sevw) }
                            ?.let { sevs -> App.database.severityDao().insertAll(sevs) }

                        it?.map { sevw -> mapBladeExpertSeverity(sevw).id }?.let { sevIds ->
                            App.database.severityDao().deleteWhereIdsNotIn(sevIds)
                        }
                    }

                    App.database.severityDao().getAll()
                        .forEach { Log.d(TAG, "Severity in database : $it") }
                }

                override fun onFailure(call: Call<Array<SeverityWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to load severities", t)
                }
            })
    }

    private fun updateDamageTypes() {
        Log.d(TAG, "updateDamageTypes()")
        App.bladeExpertService.getDamageTypes()
            .enqueue(object : retrofit2.Callback<Array<DamageTypeWrapper>> {
                override fun onResponse(
                    call: Call<Array<DamageTypeWrapper>>,
                    response: Response<Array<DamageTypeWrapper>>
                ) {
                    response?.body().let {
                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw) }
                            ?.let { dmts -> App.database.damageTypeDao().insertAll(dmts) }

                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw).id }?.let { dmtIds ->
                            App.database.damageTypeDao().deleteWhereIdsNotIn(dmtIds)
                        }
                    }
                    Log.d(TAG, "DamageType Insert done, retrieving task")
                    App.database.damageTypeDao().getAllInner()
                        .forEach { Log.d(TAG, "Inner DamageType in database : $it") }
                    App.database.damageTypeDao().getAllOuter()
                        .forEach { Log.d(TAG, "Outer DamageType in database : $it") }
                }

                override fun onFailure(call: Call<Array<DamageTypeWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to load DamageTypes", t)
                }
            })
    }

}