package com.heliopales.bladeexpertfiller.intervention

import android.content.Intent
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_gallery.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody


class InterventionListActivity : AppCompatActivity(), InterventionAdapter.InterventionItemListener {

    val TAG = InterventionListActivity::class.java.simpleName

    private lateinit var interventions: MutableList<Intervention>
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var adapter: InterventionAdapter

    private var snackBarActive: Boolean = false

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
        snackBarActive = true
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
                snackBarActive = false
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                        event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE
                    ) {
                        snackBarActive = false
                        effectivelyDeleteIntervention(deletedIntervention)
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()

    }

    private fun effectivelyDeleteIntervention(deletedIntervention: Intervention) {
        //delete damages and related pictures
        App.database.damageDao().getDamagesByInterventionId(interventionId = deletedIntervention.id)
            .forEach {
                App.database.pictureDao().deleteDamagePictures(damageLocalId = it.localId)
                App.database.damageDao().delete(it)
            }
        //delete blade and related pictures
        App.database.bladeDao().getBladesByInterventionId(deletedIntervention.id).forEach {
            App.database.pictureDao().deleteBladePictures(bladeId = it.id)
            App.database.bladeDao().delete(it)
        }

        //Delete turbine picture and intervention
        App.database.pictureDao().deleteTurbinePictures(interventionId = deletedIntervention.id)
        App.database.interventionDao().deleteIntervention(deletedIntervention)

        //delete interventionFolder
        val file = File(App.getInterventionPath(deletedIntervention))
        if (file.exists() && file.isDirectory) {
            Log.w(TAG, "will delete directory ${file.absolutePath}")
            file.deleteRecursively()
            MediaScannerConnection.scanFile(
                this, arrayOf(file.absolutePath), null, null
            )
        }
    }


    private fun updateInterventionList() {
        if (snackBarActive) {
            if (refreshLayout.isRefreshing) {
                refreshLayout.isRefreshing = false
            }
            return
        }
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

    private val executor: ExecutorService = Executors.newFixedThreadPool(1)

    override fun onInterventionUploadClick(
        intervention: Intervention
    ) {
        if (intervention.exporting) return

        intervention.export_errorsInLastExport = false
        intervention.exporting = true
        intervention.progress.value = 0
        adapter.notifyDataSetChanged()

        intervention.export_realizedOperations.removeObservers(this)
        intervention.export_numberOfOperations.removeObservers(this)

        intervention.export_count = 0;
        intervention.export_realizedOperations.value = 0;
        intervention.export_numberOfOperations.value = 0;

        intervention.export_numberOfOperations.observe(this, Observer {
            if (it == 0) return@Observer;
            App.writeOnInterventionLogFile(intervention, "Starting exportation")
            executor.execute(exportTask(intervention))
        })

        intervention.export_realizedOperations.observe(this, Observer { newVal ->
            if (newVal == 0) return@Observer;
            var percent = newVal.toFloat() / intervention.export_numberOfOperations.value!! * 100;
            Log.d(TAG, "percent $percent% done")
            intervention.progress.postValue(percent.toInt())
            if (newVal == intervention.export_numberOfOperations.value!!) {
                intervention.exporting = false
                if (intervention.export_errorsInLastExport)
                    intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                else
                    intervention.exportationState = EXPORTATION_STATE_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                App.writeOnInterventionLogFile(intervention, "End of exportation")
                runOnUiThread { adapter.notifyDataSetChanged() }
            }
        })

        Thread {
            intervention.export_numberOfOperations.postValue(countOperationsForExport(intervention))
        }.start()

    }

    private fun exportTask(intervention: Intervention): Runnable {
        return Runnable {
            Log.i(TAG, "saveTurbine ${intervention.turbineName}")
            saveTurbine(intervention)

            App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
                Log.i(TAG, "saveBlade ${bla.position}")
                saveBlade(bla, intervention)

                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_IN
                ).forEach { dsc ->
                    Log.i(TAG, "saveDamage ${dsc.fieldCode}")
                    saveIndoorDamage(dsc, intervention)
                }

                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_OUT
                ).forEach { dsc ->
                    Log.i(TAG, "saveDamage ${dsc.fieldCode}")
                    saveOutdoorDamage(dsc, intervention)
                }

                App.database.drainholeDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                    ?.let { dhs ->
                        saveDrainhole(dhs, intervention)
                    }
            }
        }
    }

    private fun saveTurbine(intervention: Intervention) {
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
                    if (!response.isSuccessful) {
                        intervention.export_errorsInLastExport = true
                        App.writeOnInterventionLogFile(intervention, "Error while saving turbine | Http response code = ${response.code()} | ${response.message()}")
                    }
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $intervention")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
    }

    private fun saveBlade(bla: Blade, intervention: Intervention) {
        App.bladeExpertService.updateBladeSerial(bladeWrapper = mapToBladeExpertBlade(bla))
            .enqueue(object : retrofit2.Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                    if (!response.isSuccessful) {
                        intervention.export_errorsInLastExport = true
                        App.writeOnInterventionLogFile(intervention, "Error while saving blade $bla | Http response code = ${response.code()} | ${response.message()}")
                    }
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $bla")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
    }

    private fun saveIndoorDamage(dsc: DamageSpotCondition, intervention: Intervention) {
        App.bladeExpertService.saveIndoorDamageSpotCondition(
            damageSpotConditionWrapper =
            mapToBladeExpertDamageSpotCondition(dsc)
        )
            .enqueue(object : retrofit2.Callback<DamageSpotConditionWrapper> {
                override fun onResponse(
                    call: Call<DamageSpotConditionWrapper>,
                    response: Response<DamageSpotConditionWrapper>
                ) {
                    //Récupération de l'id de bladeExpert et sauvegarde
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    Log.i(TAG, "damage ${dsc.fieldCode} saved")
                    if (response.isSuccessful) {
                        Log.i(TAG, "damage ${dsc.fieldCode} saved successful")
                        dsc.id = response.body()?.id
                        App.database.damageDao().updateDamage(dsc)

                        App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId)
                            .forEach { pic ->
                                Log.i(TAG, "send damage picture $pic")
                                sendSpotConditionPicture(
                                    pic,
                                    intervention,
                                    spotConditionId = dsc.id!!
                                )
                            }
                    } else {
                        App.writeOnInterventionLogFile(intervention, "Error while saving damage $dsc | Http response code = ${response.code()} | ${response.message()}")
                        intervention.export_errorsInLastExport = true
                        intervention.export_count+=App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                        intervention.export_realizedOperations.postValue(intervention.export_count)
                    }
                }

                override fun onFailure(
                    call: Call<DamageSpotConditionWrapper>,
                    t: Throwable
                ) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dsc")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
    }

    private fun saveOutdoorDamage(dsc: DamageSpotCondition, intervention: Intervention) {
        App.bladeExpertService.saveOutdoorDamageSpotCondition(
            damageSpotConditionWrapper =
            mapToBladeExpertDamageSpotCondition(dsc)
        )
            .enqueue(object : retrofit2.Callback<DamageSpotConditionWrapper> {
                override fun onResponse(
                    call: Call<DamageSpotConditionWrapper>,
                    response: Response<DamageSpotConditionWrapper>
                ) {
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    if (response.isSuccessful) {
                        //Récupération de l'id de bladeExpert et sauvegarde
                        dsc.id = response.body()?.id
                        App.database.damageDao().updateDamage(dsc)
                        App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId)
                            .forEach { pic ->
                                sendSpotConditionPicture(
                                    pic,
                                    intervention,
                                    spotConditionId = dsc.id!!
                                )
                            }
                    } else {
                        App.writeOnInterventionLogFile(intervention, "Error while saving damage $dsc | Http response code = ${response.code()} | ${response.message()}")
                        intervention.export_errorsInLastExport = true
                        intervention.export_count+=App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                        intervention.export_realizedOperations.postValue(intervention.export_count)
                    }
                }

                override fun onFailure(call: Call<DamageSpotConditionWrapper>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dsc")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
    }

    private fun saveDrainhole(dhs: DrainholeSpotCondition, intervention: Intervention) {
        Log.d(TAG,"will send $dhs")
        App.bladeExpertService.saveDrainholeSpotCondition(
            drainholeSpotConditionWrapper =
            mapToBladeExpertDrainholeSpotCondition(dhs)
        )
            .enqueue(object : retrofit2.Callback<DrainholeSpotConditionWrapper> {
                override fun onResponse(
                    call: Call<DrainholeSpotConditionWrapper>,
                    response: Response<DrainholeSpotConditionWrapper>
                ) {
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    if (response.isSuccessful) {
                        //Récupération de l'id de bladeExpert et sauvegarde
                        dhs.id = response.body()?.id
                        Log.d(TAG,"DHS envoyé avec succés, id récupéré = ${dhs.id}")
                        App.database.drainholeDao().upsertDrainhole(dhs)
                        App.database.pictureDao()
                            .getNonExportedDrainholeSpotPicturesByDrainId(dhs.localId)
                            .forEach { pic ->
                                sendSpotConditionPicture(
                                    pic,
                                    intervention,
                                    spotConditionId = dhs.id!!
                                )
                            }
                    } else {
                        App.writeOnInterventionLogFile(intervention, "Error while saving Drainhole $dhs | Http response code = ${response.code()} | ${response.message()}")
                        intervention.export_errorsInLastExport = true
                        intervention.export_count+=App.database.pictureDao()
                            .getNonExportedDrainholeSpotPicturesByDrainId(dhs.localId).size
                        intervention.export_realizedOperations.postValue(intervention.export_count)
                    }
                }

                override fun onFailure(call: Call<DrainholeSpotConditionWrapper>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dhs")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
    }

    private fun sendSpotConditionPicture(
        pic: Picture,
        intervention: Intervention,
        spotConditionId: Int
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            Log.d(TAG,"Attempt to send picture on spotCondition id = $spotConditionId  $pic")
            val pictureFile = File(pic.absolutePath)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                pictureFile.name,
                pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            App.bladeExpertService.addPictureToSpotCondition(
                spotConditionId = spotConditionId,
                filePart = filePart
            ).enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    if (response.isSuccessful) {
                        App.database.pictureDao()
                            .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                    } else {
                        intervention.export_errorsInLastExport = true
                        App.writeOnInterventionLogFile(intervention, "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.export_realizedOperations.postValue(++intervention.export_count)
                    intervention.export_errorsInLastExport = true
                }
            })
        } else {
            intervention.export_realizedOperations.postValue(++intervention.export_count)
        }
    }

    private fun countOperationsForExport(intervention: Intervention): Int {
        Log.d(TAG, "countOperationsForExport()")
        var count = 0;


        count++ //turbineSerial
        Log.d(TAG, " - 1 turbine")
        App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
            count++ // BladeSerial
            Log.d(TAG, "   - 1 blade")
            App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                bla.id,
                bla.interventionId,
                INHERIT_TYPE_DAMAGE_IN
            ).forEach { dsc ->
                count++ //IndoorDamageSpotCondition
                Log.d(TAG, "     - indoor damage ${dsc.fieldCode}")
                App.database.pictureDao()
                    .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).forEach { pic ->
                        count++ //picture
                        Log.d(TAG, "       - pic : ${pic.fileName}")
                    }
            }
            App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                bla.id,
                bla.interventionId,
                INHERIT_TYPE_DAMAGE_OUT
            ).forEach { dsc ->
                count++ //OutdoorDamageSpotCondition
                Log.d(TAG, "     - outdoor damage ${dsc.fieldCode}")
                App.database.pictureDao()
                    .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).forEach { pic ->
                        count++ //picture
                        Log.d(TAG, "       - pic : ${pic.fileName}")
                    }

            }

            App.database.drainholeDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                ?.let {
                    count++ //drainholeSpotCondition
                    Log.d(TAG, "     - drainhole")
                    App.database.pictureDao()
                        .getNonExportedDrainholeSpotPicturesByDrainId(it.localId)
                        .forEach { pic ->
                            count++ //picture
                            Log.d(TAG, "       - pic : ${pic.fileName}")
                        }
                }

        }
        Log.d(TAG, "There will be $count operations")
        return count;
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