package com.heliopales.bladeexpertfiller.intervention

import android.content.Intent
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.settings.UserSettings
import com.heliopales.bladeexpertfiller.spotcondition.*
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.item_intervention.*
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

    private var deleteAllowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_list)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "BladeExpertFiller V${BuildConfig.VERSION_NAME}"
        setSupportActionBar(toolbar)

        recyclerView = findViewById<RecyclerView>(R.id.interventions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        interventions = mutableListOf()

        adapter = InterventionAdapter(interventions, this, this)
        recyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(simpleCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        refreshLayout = findViewById(R.id.itv_swipe_layout)
        refreshLayout.setOnRefreshListener { updateInterventionList() }

        updateInterventionList()
        updateSeverities()
        updateDamageTypes()
    }

    private val simpleCallBack = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            preDeleteIntervention(interventions[position], position)
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
            "${deletedIntervention.name} supprimée",
            Snackbar.LENGTH_SHORT
        )
            .setAction("Annuler") {
                interventions.add(position, deletedIntervention)
                adapter.notifyItemInserted(position)
                snackBarActive = false
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_TIMEOUT ||
                        event == DISMISS_EVENT_CONSECUTIVE
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
        //delete weathers
        App.database.weatherDao()
            .getWeathersByInterventionId(interventionId = deletedIntervention.id)
            .forEach {
                App.database.weatherDao().delete(it)
            }
        //delete damages and related pictures
        App.database.damageDao().getDamagesByInterventionId(interventionId = deletedIntervention.id)
            .forEach {
                App.database.damageDao().delete(it)
            }
        //Delete drainhole, related pictures
        App.database.drainholeDao().getByInterventionId(interventionId = deletedIntervention.id)
            .forEach {
                App.database.drainholeDao().delete(it)
            }

        //Delete lightning, related pictures and related measures
        App.database.lightningDao().getByInterventionId(interventionId = deletedIntervention.id)
            .forEach {
                App.database.receptorMeasureDao()
                    .deleteByLightningLocalId(lightningLocalId = it.localId)
                App.database.lightningDao().delete(it)
            }

        //delete blade and related pictures
        App.database.bladeDao().getBladesByInterventionId(deletedIntervention.id).forEach {
            App.database.bladeDao().delete(it)
        }

        //Delete pictures and intervention
        App.database.pictureDao()
            .deletePicturesLinkedToIntervention(interventionId = deletedIntervention.id)
        App.database.interventionDao().deleteIntervention(deletedIntervention)

        //delete interventionFolder
        val file = File(App.getMainInterventionPath(deletedIntervention))
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
            if (refreshLayout.isRefreshing)
                refreshLayout.isRefreshing = false
            return
        }
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }
        interventions.clear()
        interventions.addAll(
            App.database.interventionDao().getAllInterventions().sortedBy { it.name })

        App.bladeExpertService.getInterventions()
            .enqueue(object : retrofit2.Callback<Array<InterventionWrapper>> {
                override fun onResponse(
                    call: Call<Array<InterventionWrapper>>,
                    response: Response<Array<InterventionWrapper>>
                ) {

                    val newInterventions: MutableList<Intervention> = mutableListOf()

                    if (response.isSuccessful) {
                        response.body().let {
                            newInterventions.addAll(it?.map { itv -> mapBladeExpertIntervention(itv) } as MutableList)
                            it.forEach { itvw ->
                                App.database.turbineDao().upsertTurbines(
                                    itvw.turbines!!.map { tw -> mapBladeExpertTurbine(tw) }
                                )
                                App.database.bladeDao().insertNonExistingBlades(
                                    itvw.blades!!.map { bw -> mapBladeExpertBlade(bw) }
                                )
                                itvw.blades.forEach { blaw ->
                                    App.database.receptorDao()
                                        .upsertLightningReceptors(
                                            blaw.receptors!!.map { ltrw ->
                                                mapBladeExpertLightningReceptor(ltrw)
                                            })
                                    blaw.receptors.forEach { ltrw ->
                                        Log.d(TAG, "Receptor added $ltrw")
                                    }
                                }
                            }
                        }
                    }

                    interventions.forEach { it.expired = !newInterventions.contains(it) }
                    newInterventions.forEach { if (!interventions.contains(it)) interventions.add(it) }
                    interventions.forEach {
                        App.database.interventionDao().insertNonExistingIntervention(it)
                    }
                    interventions.sortBy { it.name }
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
        menuInflater.inflate(R.menu.activity_intervention_list, menu)

        val menuItem = menu?.findItem(R.id.toogler_delete_ongoing_intervention)
        menuItem?.setActionView(R.layout.menu_switch)

        val switch = menuItem?.actionView?.findViewById<Switch>(R.id.switch_show_protected)
        switch?.isChecked = deleteAllowed
        switch?.setOnCheckedChangeListener { _, isChecked ->
            deleteAllowed = isChecked
            if (isChecked) {
                toast(
                    "You can now delete any intervention",
                    Toast.LENGTH_LONG
                )
            } else {
                toast(
                    "You can now delete 'ONGOING' interventions only",
                    Toast.LENGTH_LONG
                )
            }
        }

        val editUserKeyButton = menu?.findItem(R.id.change_user_key)
        editUserKeyButton?.setOnMenuItemClickListener {
            showChangeUserKeyDialog()
            true
        }
        return true
    }

    private fun showChangeUserKeyDialog() {
        var settings = App.database.userSettingsDao().getUserSettings()
        val dialog = ChangeUserKeyDialogFragment(settings?.userApiKey)
        dialog.listener =
            object : ChangeUserKeyDialogFragment.ChangeUserKeyDialogListener {
                override fun onDialogPositiveClick(key: String) {
                    if (settings == null) {
                        settings = UserSettings(userApiKey = key)
                    } else {
                        settings!!.userApiKey = key
                    }
                    App.database.userSettingsDao().upsert(settings!!)
                }

                override fun onDialogNegativeClick() {
                }
            }
        dialog.show(supportFragmentManager, "ChangeTurbineSerialDialogFragment")
    }


    private val executor: ExecutorService = Executors.newFixedThreadPool(1)

    override fun onInterventionUploadClick(
        intervention: Intervention
    ) {
        if (intervention.exporting) return
        else exportAfterDamageUncompleteCheck(intervention)
    }

    private fun exportAfterDamageUncompleteCheck(intervention: Intervention) {
        var damageUncomplete = false

        App.database.damageDao().getDamagesByInterventionId(intervention.id).forEach { dsc ->
            if (dsc.radialPosition == null || dsc.position == null)
                damageUncomplete = true
        }

        if (damageUncomplete) {
            AlertDialog.Builder(this)
                .setMessage(
                    "There is at least 1 uncompleted damage. " +
                            "Proceed exportation anyway ? Uncompleted damage(s) will no be sent."
                )
                .setPositiveButton("Yes") { _, _ ->
                    uploadIntervention(intervention)
                }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
        } else {
            uploadIntervention(intervention)
        }

    }

    private fun uploadIntervention(intervention: Intervention) {
        intervention.exportErrorsInLastExport = false
        intervention.exporting = true
        intervention.progress.value = 0
        adapter.notifyDataSetChanged()

        intervention.exportRealizedOperations.removeObservers(this)
        intervention.exportNumberOfOperations.removeObservers(this)

        intervention.exportCount = 0
        intervention.exportRealizedOperations.value = 0
        intervention.exportNumberOfOperations.value = 0

        intervention.exportNumberOfOperations.observe(this, Observer {
            if (it == 0) return@Observer
            App.writeOnInterventionLogFile(intervention, "Starting exportation")
            executor.execute(exportTask(intervention))
        })

        intervention.exportRealizedOperations.observe(this, Observer { newVal ->
            if (newVal == 0) return@Observer
            var percent = newVal.toFloat() / intervention.exportNumberOfOperations.value!! * 100
            Log.d(TAG, "percent $percent% done")
            intervention.progress.postValue(percent.toInt())
            if (newVal == intervention.exportNumberOfOperations.value!!) {
                intervention.exporting = false
                if (intervention.exportErrorsInLastExport)
                    intervention.exportationState = EXPORTATION_STATE_NOT_EXPORTED
                else
                    intervention.exportationState = EXPORTATION_STATE_EXPORTED
                App.database.interventionDao().updateIntervention(intervention)
                App.writeOnInterventionLogFile(intervention, "End of exportation")
                runOnUiThread { adapter.notifyDataSetChanged() }
            }
        })

        Thread {
            intervention.exportNumberOfOperations.postValue(
                countOperationsForExport(
                    intervention
                )
            )
        }.start()
    }


    private fun exportTask(intervention: Intervention): Runnable {
        return Runnable {
            Log.i(TAG, "exporting Intervention ${intervention.id} ${intervention.name}")

            //Weather
            saveWeathers(intervention)

            //Intervention Pictures
            App.database.pictureDao().getNonExportedPicturesByTypeAndInterventionId(
                PICTURE_TYPE_INTERVENTION, intervention.id
            ).forEach { pic ->
                Log.d(TAG, "exporting intervention picture $pic")
                sendInterventionPicture(pic, intervention)
            }

            //Blade Pictures
            App.database.pictureDao().getNonExportedPicturesByTypeAndInterventionId(
                PICTURE_TYPE_BLADE, intervention.id
            ).forEach { pic ->
                sendBladePicture(pic, intervention)
            }

            //Turbine Pictures
            App.database.pictureDao().getNonExportedPicturesByTypeAndInterventionId(
                PICTURE_TYPE_TURBINE, intervention.id
            ).forEach { pic ->
                sendTurbinePicture(pic, intervention)
            }

            //Windfarm Pictures
            App.database.pictureDao().getNonExportedPicturesByTypeAndInterventionId(
                PICTURE_TYPE_WINDFARM, intervention.id
            ).forEach { pic ->
                sendWindfarmPicture(pic, intervention)
            }

            saveTurbineSerial(intervention)

            App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
                Log.i(TAG, "saveBlade ${bla.position}")
                saveBladeSerial(bla, intervention)

                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_IN
                ).forEach { dsc ->
                    if (dsc.radialPosition != null && dsc.position != null) {
                        Log.d(TAG, "saveDamage ${dsc.fieldCode}")
                        saveIndoorDamage(dsc, intervention)
                    } else {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).forEach { _ ->
                                intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                            }
                    }
                }

                App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                    bla.id,
                    bla.interventionId,
                    INHERIT_TYPE_DAMAGE_OUT
                ).forEach { dsc ->
                    if (dsc.radialPosition != null && dsc.position != null) {
                        Log.d(TAG, "saveDamage ${dsc.fieldCode}")
                        saveOutdoorDamage(dsc, intervention)
                    } else {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).forEach { _ ->
                                intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                            }
                    }

                }

                App.database.drainholeDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                    ?.let { dhs ->
                        saveDrainhole(dhs, intervention)
                    }

                App.database.lightningDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                    ?.let { lps ->
                        saveLightning(lps, intervention)
                    }
            }
        }
    }

    private fun saveTurbineSerial(intervention: Intervention) {
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
                        intervention.exportErrorsInLastExport = true
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving turbine | Http response code = ${response.code()} | ${response.message()}"
                        )
                    }
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    App.writeOnInterventionLogFile(
                        intervention,
                        "Failure while exporting $intervention"
                    )
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })

    }

    private fun saveBladeSerial(bla: Blade, intervention: Intervention) {
        App.bladeExpertService.updateBladeSerial(bladeWrapper = mapToBladeExpertBlade(bla))
            .enqueue(object : retrofit2.Callback<Boolean> {
                override fun onResponse(
                    call: Call<Boolean>,
                    response: Response<Boolean>
                ) {
                    if (!response.isSuccessful) {
                        intervention.exportErrorsInLastExport = true
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving blade $bla | Http response code = ${response.code()} | ${response.message()}"
                        )
                    }
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $bla")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })
    }

    private fun saveWeathers(intervention: Intervention) {
        val wrappers: List<WeatherWrapper> =
            App.database.weatherDao().getWeathersByInterventionId(intervention.id)
                .map { weather ->
                    Log.d(TAG, "weather to be uploaded $weather")
                    mapToBladeExpertWeather(weather) }

        App.bladeExpertService.saveWeather(
            weatherWrappers = wrappers
        ).enqueue(object : retrofit2.Callback<List<WeatherWrapper>> {
            override fun onResponse(
                call: Call<List<WeatherWrapper>>,
                response: Response<List<WeatherWrapper>>
            ) {
                intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                //Récupération de l'id de bladeExpert et sauvegarde
                if (response.isSuccessful) {
                    response.body()?.forEach { ww ->
                        val wx = mapBladeExpertWeather(ww)
                        App.database.weatherDao().upsert(wx)
                        Log.d(TAG, "weather upserted after import $wx")
                    }

                } else {
                    App.writeOnInterventionLogFile(
                        intervention,
                        "Error while saving weather | Http response code = ${response.code()} | ${response.message()}"
                    )
                    intervention.exportErrorsInLastExport = true
                    intervention.exportRealizedOperations.postValue(intervention.exportCount)
                }
            }

            override fun onFailure(
                call: Call<List<WeatherWrapper>>,
                t: Throwable
            ) {
                App.writeOnInterventionLogFile(intervention, "Failure while exporting weather")
                App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                intervention.exportErrorsInLastExport = true
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
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
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
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving damage $dsc | Http response code = ${response.code()} | ${response.message()}"
                        )
                        intervention.exportErrorsInLastExport = true
                        intervention.exportCount += App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                        intervention.exportRealizedOperations.postValue(intervention.exportCount)
                    }
                }

                override fun onFailure(
                    call: Call<DamageSpotConditionWrapper>,
                    t: Throwable
                ) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dsc")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportCount += App.database.pictureDao()
                        .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
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
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
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
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving damage $dsc | Http response code = ${response.code()} | ${response.message()}"
                        )
                        intervention.exportErrorsInLastExport = true
                        intervention.exportCount += App.database.pictureDao()
                            .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                        intervention.exportRealizedOperations.postValue(intervention.exportCount)
                    }
                }

                override fun onFailure(call: Call<DamageSpotConditionWrapper>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dsc")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportCount += App.database.pictureDao()
                        .getNonExportedDamageSpotPicturesByDamageId(dsc.localId).size
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })
    }

    private fun saveDrainhole(dhs: DrainholeSpotCondition, intervention: Intervention) {
        Log.d(TAG, "will send $dhs")
        App.bladeExpertService.saveDrainholeSpotCondition(
            drainholeSpotConditionWrapper =
            mapToBladeExpertDrainholeSpotCondition(dhs)
        )
            .enqueue(object : retrofit2.Callback<DrainholeSpotConditionWrapper> {
                override fun onResponse(
                    call: Call<DrainholeSpotConditionWrapper>,
                    response: Response<DrainholeSpotConditionWrapper>
                ) {
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    if (response.isSuccessful) {
                        //Récupération de l'id de bladeExpert et sauvegarde
                        dhs.id = response.body()?.id
                        Log.d(TAG, "DHS envoyé avec succés, id récupéré = ${dhs.id}")
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
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving Drainhole $dhs | Http response code = ${response.code()} | ${response.message()}"
                        )
                        intervention.exportErrorsInLastExport = true
                        intervention.exportCount += App.database.pictureDao()
                            .getNonExportedDrainholeSpotPicturesByDrainId(dhs.localId).size
                        intervention.exportRealizedOperations.postValue(intervention.exportCount)
                    }
                }

                override fun onFailure(call: Call<DrainholeSpotConditionWrapper>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $dhs")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportCount += App.database.pictureDao()
                        .getNonExportedDrainholeSpotPicturesByDrainId(dhs.localId).size
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })
    }

    private fun saveLightning(lps: LightningSpotCondition, intervention: Intervention) {
        Log.d(TAG, "will send $lps")

        val measures =
            App.database.receptorMeasureDao().getByLightningSpotConditionLocalId(lps.localId)

        val wrapper = mapToBladeExpertLightningSpotCondition(
            lps,
            measures.map { lrm -> mapToBladeExpertLightningMeasure(lrm) })

        App.bladeExpertService.saveLightningSpotCondition(
            lightningSpotConditionWrapper = wrapper
        )
            .enqueue(object : retrofit2.Callback<LightningSpotConditionWrapper> {
                override fun onResponse(
                    call: Call<LightningSpotConditionWrapper>,
                    response: Response<LightningSpotConditionWrapper>
                ) {
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    if (response.isSuccessful) {
                        //Récupération de l'id de bladeExpert et sauvegarde
                        lps.id = response.body()?.id
                        Log.d(TAG, "LPS envoyé avec succés, id récupéré = ${lps.id}")
                        App.database.lightningDao().upsert(lps)
                        App.database.pictureDao()
                            .getNonExportedLightningSpotPicturesByLightningId(lps.localId)
                            .forEach { pic ->
                                sendSpotConditionPicture(
                                    pic,
                                    intervention,
                                    spotConditionId = lps.id!!
                                )
                            }
                    } else {
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving Drainhole $lps | Http response code = ${response.code()} | ${response.message()}"
                        )
                        intervention.exportErrorsInLastExport = true
                        intervention.exportCount += App.database.pictureDao()
                            .getNonExportedDrainholeSpotPicturesByDrainId(lps.localId).size
                        intervention.exportRealizedOperations.postValue(intervention.exportCount)
                    }
                }

                override fun onFailure(call: Call<LightningSpotConditionWrapper>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $lps")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportCount += App.database.pictureDao()
                        .getNonExportedDrainholeSpotPicturesByDrainId(lps.localId).size
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })
    }

    private fun sendSpotConditionPicture(
        pic: Picture,
        intervention: Intervention,
        spotConditionId: Int
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            Log.d(TAG, "Attempt to send picture on spotCondition id = $spotConditionId  $pic")
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
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    if (response.isSuccessful) {
                        App.database.pictureDao()
                            .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                    } else {
                        intervention.exportErrorsInLastExport = true
                        App.writeOnInterventionLogFile(
                            intervention,
                            "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                    App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                    intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                    intervention.exportErrorsInLastExport = true
                }
            })
        } else {
            intervention.exportRealizedOperations.postValue(++intervention.exportCount)
        }
    }

    private fun sendTurbinePicture(
        pic: Picture,
        intervention: Intervention,
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            val pictureFile = File(pic.absolutePath)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                pictureFile.name,
                pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            App.bladeExpertService.addPictureToTurbine(
                turbineId = pic.relatedId,
                interventionId = intervention.id,
                filePart = filePart
            )
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        if (response.isSuccessful) {
                            App.database.pictureDao()
                                .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                        } else {
                            intervention.exportErrorsInLastExport = true
                            App.writeOnInterventionLogFile(
                                intervention,
                                "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                        App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        intervention.exportErrorsInLastExport = true
                    }
                })
        } else {
            intervention.exportRealizedOperations.postValue(++intervention.exportCount)
        }
    }

    private fun sendInterventionPicture(
        pic: Picture,
        intervention: Intervention,
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            val pictureFile = File(pic.absolutePath)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                pictureFile.name,
                pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            App.bladeExpertService.addPictureToIntervention(
                interventionId = intervention.id,
                filePart = filePart
            )
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        if (response.isSuccessful) {
                            App.database.pictureDao()
                                .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                        } else {
                            intervention.exportErrorsInLastExport = true
                            App.writeOnInterventionLogFile(
                                intervention,
                                "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                        App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        intervention.exportErrorsInLastExport = true
                    }
                })
        } else {
            intervention.exportRealizedOperations.postValue(++intervention.exportCount)
        }
    }

    private fun sendBladePicture(
        pic: Picture,
        intervention: Intervention,
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            val pictureFile = File(pic.absolutePath)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                pictureFile.name,
                pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            App.bladeExpertService.addPictureToBlade(
                bladeId = pic.relatedId,
                interventionId = intervention.id,
                filePart = filePart
            )
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        if (response.isSuccessful) {
                            App.database.pictureDao()
                                .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                        } else {
                            intervention.exportErrorsInLastExport = true
                            App.writeOnInterventionLogFile(
                                intervention,
                                "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                        App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        intervention.exportErrorsInLastExport = true
                    }
                })
        } else {
            intervention.exportRealizedOperations.postValue(++intervention.exportCount)
        }
    }

    private fun sendWindfarmPicture(
        pic: Picture,
        intervention: Intervention,
    ) {
        if (pic.exportState == EXPORTATION_STATE_NOT_EXPORTED) {
            val pictureFile = File(pic.absolutePath)
            val filePart = MultipartBody.Part.createFormData(
                "file",
                pictureFile.name,
                pictureFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            App.bladeExpertService.addPictureToWindfarm(
                windfarmId = pic.relatedId,
                interventionId = intervention.id,
                filePart = filePart
            )
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        if (response.isSuccessful) {
                            App.database.pictureDao()
                                .updateExportationState(pic.fileName, EXPORTATION_STATE_EXPORTED)
                        } else {
                            intervention.exportErrorsInLastExport = true
                            App.writeOnInterventionLogFile(
                                intervention,
                                "Error while saving picture $pic | Http response code = ${response.code()} | ${response.message()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        App.writeOnInterventionLogFile(intervention, "Failure while exporting $pic")
                        App.writeOnInterventionLogFile(intervention, t.stackTraceToString())
                        intervention.exportRealizedOperations.postValue(++intervention.exportCount)
                        intervention.exportErrorsInLastExport = true
                    }
                })
        } else {
            intervention.exportRealizedOperations.postValue(++intervention.exportCount)
        }
    }

    private fun countOperationsForExport(intervention: Intervention): Int {
        Log.d(TAG, "countOperationsForExport()")
        var count = 0

        //count pictures
        count += App.database.pictureDao().countNonExportedPicturesByInterventionId(intervention.id)
        Log.d(TAG, " - $count pictures")

        //turbineSerial
        count++
        Log.d(TAG, " - 1 turbine")

        //Weather
        if (App.database.weatherDao().getWeathersByInterventionId(intervention.id).size > 0) {
            count++
            Log.d(TAG, " - Weather")
        }

        App.database.bladeDao().getBladesByInterventionId(intervention.id).forEach { bla ->
            count++ // BladeSerial
            Log.d(TAG, "   - 1 blade")

            App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                bla.id,
                bla.interventionId,
                INHERIT_TYPE_DAMAGE_IN
            ).forEach { dsc ->
                if (dsc.radialPosition != null && dsc.position != null) {
                    count++ //IndoorDamageSpotCondition
                    Log.d(TAG, "     - indoor damage ${dsc.fieldCode}")
                }
            }
            App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
                bla.id,
                bla.interventionId,
                INHERIT_TYPE_DAMAGE_OUT
            ).forEach { dsc ->
                if (dsc.radialPosition != null && dsc.position != null) {
                    count++ //OutdoorDamageSpotCondition
                    Log.d(TAG, "     - outdoor damage ${dsc.fieldCode}")
                }
            }
            App.database.drainholeDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                ?.let {
                    count++ //drainholeSpotCondition
                    Log.d(TAG, "     - drainhole")
                }
            App.database.lightningDao().getByBladeAndIntervention(bla.id, bla.interventionId)
                ?.let {
                    count++ //lightningSpotCondition
                    Log.d(TAG, "     - lightning")
                }

        }
        Log.d(TAG, "There will be $count operations")
        return count
    }

    private fun updateSeverities() {
        App.bladeExpertService.getSeverities()
            .enqueue(object : retrofit2.Callback<Array<SeverityWrapper>> {
                override fun onResponse(
                    call: Call<Array<SeverityWrapper>>,
                    response: Response<Array<SeverityWrapper>>
                ) {
                    response.body().let {
                        it?.map { sevw -> mapBladeExpertSeverity(sevw) }
                            ?.let { sevs -> App.database.severityDao().insertAll(sevs) }

                        it?.map { sevw -> mapBladeExpertSeverity(sevw).id }?.let { sevIds ->
                            App.database.severityDao().deleteWhereIdsNotIn(sevIds)
                        }
                    }

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
                    response.body().let {
                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw) }
                            ?.let { dmts -> App.database.damageTypeDao().insertAll(dmts) }

                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw).id }?.let { dmtIds ->
                            App.database.damageTypeDao().deleteWhereIdsNotIn(dmtIds)
                        }
                    }
                }

                override fun onFailure(call: Call<Array<DamageTypeWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to load DamageTypes", t)
                }
            })
    }


}