package com.heliopales.bladeexpertfiller.intervention

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.AppDatabase
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Response


class InterventionListActivity : AppCompatActivity(), InterventionAdapter.InterventionItemListener {

    val TAG = InterventionListActivity::class.java.simpleName

    private lateinit var database: AppDatabase
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

        database = App.database

        recyclerView = findViewById<RecyclerView>(R.id.interventions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        interventions = mutableListOf()

        adapter = InterventionAdapter(interventions, this)
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
        database.bladeDao().deleteBladesOfInterventionId(deletedIntervention.id)
        database.interventionDao().deleteIntervention(deletedIntervention)
    }

    private fun updateInterventionList() {
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
        interventions.clear()
        interventions.addAll(
            database.interventionDao().getAllInterventions().sortedBy { it.turbineName })

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
                            database.bladeDao().insertNonExistingBlades(
                                itvw.blades.map { bw -> mapBladeExpertBlade(bw) }
                            )
                        }
                    }
                    interventions.forEach { it.expired = !newInterventions.contains(it) }
                    newInterventions.forEach { if (!interventions.contains(it)) interventions.add(it) }
                    interventions.forEach {
                        database.interventionDao().insertNonExistingIntervention(it)
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
        interventionDetailsLauncher.launch(intent)
    }

    private var interventionDetailsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val itvResult =
                    data?.getParcelableExtra<Intervention>(InterventionDetailsActivity.EXTRA_INTERVENTION)
                interventions.forEach {
                    if (it.equals(itvResult)) {
                        it.turbineSerial = itvResult?.turbineSerial
                        it.state = itvResult.state
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }

    override fun onInterventionUploadClick(intervention: Intervention) {
        toast("${intervention.turbineName} upload selected")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.i(TAG, "onCreateOptionsMenu()")
        menuInflater.inflate(R.menu.activity_intervention_list, menu)
        val menuItem = menu?.findItem(R.id.toogler_delete_ongoing_intervention)
        menuItem?.setActionView(R.layout.menu_switch)

        val switch = menuItem?.actionView?.findViewById<Switch>(R.id.switch_show_protected)
        switch?.isChecked = deleteAllowed
        switch?.setOnCheckedChangeListener { buttonView, isChecked ->
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
                            ?.let {dmts -> App.database.damageTypeDao().insertAll(dmts) }

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