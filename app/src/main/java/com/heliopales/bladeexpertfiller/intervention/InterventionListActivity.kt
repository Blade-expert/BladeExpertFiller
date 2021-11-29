package com.heliopales.bladeexpertfiller.intervention

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.Database
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.bladeexpert.InterventionWrapper
import com.heliopales.bladeexpertfiller.bladeexpert.mapBladeExpertIntervention
import com.heliopales.bladeexpertfiller.utils.toast
import retrofit2.Call
import retrofit2.Response


class InterventionListActivity : AppCompatActivity(), InterventionAdapter.InterventionItemListener {

    val TAG = "InterventionListActivity"

    private lateinit var database: Database
    private lateinit var interventions: MutableList<Intervention>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: InterventionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_list)



        database = App.database

        recyclerView = findViewById<RecyclerView>(R.id.interventions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        interventions = getInitInterventions();
        adapter = InterventionAdapter(interventions, this)
        recyclerView.adapter = adapter

        refreshLayout = findViewById(R.id.swipe_layout)

        refreshLayout.setOnRefreshListener { updateInterventionList() }

        //updateInterventionList()
    }

    private fun getInitInterventions(): MutableList<Intervention> {
        var interventions = mutableListOf<Intervention>()

        interventions.add(Intervention(123, "Le Boutoinner E1"))
        interventions.add(Intervention(124, "Le Boutoinner E2"))
        interventions.add(Intervention(125, "Le Boutoinner E3"))
        interventions.add(Intervention(126, "Le Boutoinner E4"))

        return interventions;
    }

    private fun updateInterventionList() {

        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }

        App.bladeExpertService.getInterventions()
            .enqueue(object : retrofit2.Callback<Array<InterventionWrapper>> {
                override fun onResponse(
                    call: Call<Array<InterventionWrapper>>,
                    response: Response<Array<InterventionWrapper>>
                ) {
                    response?.body()?.let {
                        interventions.clear()
                        for (itvWrapper in it) {
                            interventions.add(mapBladeExpertIntervention(itvWrapper))
                        }
                    }
                    adapter.notifyDataSetChanged()
                    toast("La liste d'intervention est à jour")
                    refreshLayout.isRefreshing = false
                }

                override fun onFailure(call: Call<Array<InterventionWrapper>>, t: Throwable) {
                    toast("Impossible de mettre à jour les interventions")
                    Log.e(TAG, "Error while retrieving itvs", t)
                    refreshLayout.isRefreshing = false
                }
            })
    }


    override fun onInterventionSelected(intervention: Intervention) {
    }

    override fun onInterventionUploadClick(intervention: Intervention) {
    }


}