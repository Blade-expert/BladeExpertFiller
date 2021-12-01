package com.heliopales.bladeexpertfiller.intervention

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.Database
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.bladeexpert.InterventionWrapper
import com.heliopales.bladeexpertfiller.bladeexpert.mapBladeExpertBlade
import com.heliopales.bladeexpertfiller.bladeexpert.mapBladeExpertIntervention
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Response


class InterventionListActivity : AppCompatActivity(), InterventionAdapter.InterventionItemListener {

    val TAG = "InterventionListActivity"

    private lateinit var database: Database
    private lateinit var interventions: MutableList<Intervention>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: InterventionAdapter

    private lateinit var deletedIntervention: Intervention

    private var deleteAllowed = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_list)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
       setSupportActionBar(toolbar)

        database = App.database

        recyclerView = findViewById<RecyclerView>(R.id.interventions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        interventions = mutableListOf()

        interventions.sortBy { it.turbineName }
        adapter = InterventionAdapter(interventions, this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(simpleCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        refreshLayout = findViewById(R.id.swipe_layout)
        refreshLayout.setOnRefreshListener { updateInterventionList() }

        updateInterventionList()

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
            deletedIntervention = interventions[position]
            deleteIntervention(deletedIntervention, position);
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val position = viewHolder.absoluteAdapterPosition
            if (!interventions[position].expired && false) return 0
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

    private fun deleteIntervention(deletedIntervention: Intervention, position: Int) {
        interventions.remove(deletedIntervention)

        adapter.notifyItemRemoved(position)
        Snackbar.make(
            recyclerView,
            "${deletedIntervention.turbineName} supprimé",
            Snackbar.LENGTH_LONG
        )
            .setAction("Annuler") {
                interventions.add(position, deletedIntervention)
                adapter.notifyItemInserted(position)
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        database.deleteIntervention(deletedIntervention)
                        database.deleteBladesOfIntervention(deletedIntervention)
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }


    private fun updateInterventionList() {
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
        interventions.clear()
        interventions.addAll(database.getAllInterventions().sortedBy { it.turbineName })

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
                            itvw.blades.forEach { bw ->
                                database.insertNonExistingBlade(
                                    mapBladeExpertBlade(bw),
                                    itvw.id
                                )
                            }
                        }
                    }
                    interventions.removeAll(newInterventions)
                    interventions.forEach { it.expired = true }
                    interventions.addAll(newInterventions)
                    interventions.forEach { database.insertNonExistingIntervention(it) }
                    interventions.sortBy { it.turbineName }
                    adapter.notifyDataSetChanged()
                    toast("La liste d'intervention est à jour")
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

    override fun onInterventionUploadClick(intervention: Intervention) {
        toast("${intervention.turbineName} upload selected")
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.toogler_delete_ongoing_intervention)?.isChecked = deleteAllowed;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.toogler_delete_ongoing_intervention -> {
                deleteAllowed = !item.isChecked
                item.isChecked = deleteAllowed
                return true
            }
            else -> false
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_intervention_list, menu)
        return true;
    }

}