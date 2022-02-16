package com.heliopales.bladeexpertfiller.spotcondition.damages

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.camera.CameraActivity
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.intervention.InterventionActivity
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.utils.OnSwipeListener
import com.heliopales.bladeexpertfiller.utils.toast
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import retrofit2.Call
import retrofit2.Response
import java.io.File

class DamageListActivity : AppCompatActivity(), DamageAdapter.DamageItemListener,
    View.OnClickListener, View.OnTouchListener {
    val TAG = DamageListActivity::class.java.simpleName

    companion object {
        val EXTRA_INTERVENTION = "intervention"
        val EXTRA_BLADE = "blade"
        val EXTRA_DAMAGE_INOUT = "damage_inout"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DamageAdapter
    private lateinit var blade: Blade;
    private lateinit var intervention: Intervention
    private lateinit var gestureDetector: GestureDetector
    private lateinit var damages: MutableList<DamageSpotCondition>

    private lateinit var scopeSwitch: Switch
    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var damageInheritType: String

    private var snackBarActive: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate()")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_damage_list)

        recyclerView = findViewById(R.id.damages_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        scopeSwitch = findViewById(R.id.damage_switch_scope)
        scopeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                scopeSwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        scopeSwitch.context,
                        R.color.bulma_link
                    )
                )
                scopeSwitch.thumbTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        scopeSwitch.context,
                        R.color.bulma_link
                    )
                )
            } else {
                scopeSwitch.trackTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        scopeSwitch.context,
                        R.color.bulma_gray_light
                    )
                )
                scopeSwitch.thumbTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        scopeSwitch.context,
                        R.color.bulma_gray_light
                    )
                )
            }
            adapter.scopeMode = isChecked
            adapter.notifyDataSetChanged();
        }

        blade = intent.getParcelableExtra(EXTRA_BLADE)!!
        intervention = intent.getParcelableExtra(EXTRA_INTERVENTION)!!

        damageInheritType = intent.getStringExtra(EXTRA_DAMAGE_INOUT)!!

        findViewById<TextView>(R.id.damage_blade_title).text = "${blade.position} - ${blade.serial?:"na"}"

        when(damageInheritType){
            INHERIT_TYPE_DAMAGE_IN -> findViewById<TextView>(R.id.damage_list_label).text = "INTERNAL"
            INHERIT_TYPE_DAMAGE_OUT ->  findViewById<TextView>(R.id.damage_list_label).text = "EXTERNAL"
            else -> findViewById<TextView>(R.id.damage_list_label).text = "ERROR"
        }

        val itemTouchHelper = ItemTouchHelper(simpleCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        refreshLayout = findViewById(R.id.damage_swipe_layout)
        refreshLayout.setOnRefreshListener { updateDamageList() }

        findViewById<ImageButton>(R.id.add_damage_button).setOnClickListener(this)

        gestureDetector = GestureDetector(this, object : OnSwipeListener() {
            override fun onSwipe(direction: Direction?): Boolean {
                Log.d(TAG, "OnSwipe")
                when (direction) {
                    Direction.up -> Log.d(TAG, "Swiped UP")
                    Direction.down -> startInterventionActivity()
                    Direction.left -> Log.d(TAG, "Swiped LEFT")
                    Direction.right -> Log.d(TAG, "Swiped RIGHT")
                    else -> Log.d(TAG, "No direction found for Swipe")
                }
                return true
            }
        })

        findViewById<RelativeLayout>(R.id.damage_list_main_layout).setOnTouchListener(this)
    }

    private fun updateDamageList() {
        if (snackBarActive) {
            if (refreshLayout.isRefreshing)
                refreshLayout.isRefreshing = false
            return
        }
        if (!refreshLayout.isRefreshing) {
            refreshLayout.isRefreshing = true
        }

        /* UPDATE database with damages from BladeExpert */
        val inout = if(damageInheritType == INHERIT_TYPE_DAMAGE_IN) "indoorDamage" else "outdoorDamage"
        App.bladeExpertService.getDamages(interventionId = intervention.id, bladeId=blade.id, inoutdoorDamage = inout)
        .enqueue(object : retrofit2.Callback<Array<DamageSpotConditionWrapper>> {
            override fun onResponse(
                call: Call<Array<DamageSpotConditionWrapper>>,
                response: Response<Array<DamageSpotConditionWrapper>>
            ) {
                if (response.isSuccessful) {
                    response.body().let {
                        it?.forEach { dscw ->
                            Log.d(TAG, dscw.toString())
                            val dsc =
                                App.database.damageDao().getDamageByRemoteId(remoteId = dscw.id!!)
                            var d =
                                mapBladeExpertDamageSpotCondition(dscw, damageInheritType)
                            if (dsc == null) {
                                App.database.damageDao().insertDamage(d)
                            } else {
                                d.localId = dsc.localId
                                if(d.severityId == null) d.severityId = dsc.severityId
                                if(d.description == null) d.description = dsc.description
                                if(d.damageTypeId == null) d.damageTypeId = dsc.damageTypeId
                                if(d.radialPosition == null) d.radialPosition = dsc.radialPosition
                                if(d.radialLength == null) d.radialLength = dsc.radialLength
                                if(d.longitudinalLength == null) d.longitudinalLength = dsc.longitudinalLength
                                if(d.repetition == null) d.repetition = dsc.repetition
                                if(d.position == null) d.position = dsc.position
                                if(d.profileDepth == null) d.profileDepth = dsc.profileDepth

                                App.database.damageDao().updateDamage(d)
                            }
                        }
                    }
                }
                loadDamagesFromDb()
                refreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<Array<DamageSpotConditionWrapper>>, t: Throwable) {
                toast("Impossible to update damage list")
                loadDamagesFromDb()
                refreshLayout.isRefreshing = false
            }
        })
    }

    private fun preDeleteDamage(deletedDamage: DamageSpotCondition, position: Int) {
        snackBarActive = true
        damages.remove(deletedDamage)
        adapter.notifyItemRemoved(position)
        Snackbar.make(
            recyclerView,
            "Damage deleted",
            Snackbar.LENGTH_SHORT
        )
            .setAction("Cancel") {
                damages.add(position, deletedDamage)
                adapter.notifyItemInserted(position)
                snackBarActive = false
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_TIMEOUT ||
                        event == DISMISS_EVENT_CONSECUTIVE
                    ) {
                        snackBarActive = false
                        effectivelyDeleteDamage(deletedDamage)
                    }
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .show()
    }

    private fun effectivelyDeleteDamage(deletedDamage: DamageSpotCondition) {

        //delete damageFolder
        val file = File(App.getDamagePath(intervention = intervention, blade = blade, damageSpotConditionLocalId = deletedDamage.localId))
        if (file.exists() && file.isDirectory) {
            Log.w(TAG, "will delete directory ${file.absolutePath}")
            file.deleteRecursively()
            MediaScannerConnection.scanFile(
                this, arrayOf(file.absolutePath), null, null
            )
        }

        //delete pictures in DB
        App.database.pictureDao().getDamageSpotPicturesByDamageId(deletedDamage.localId).forEach {
            App.database.pictureDao().deletePictureByFileName(it.fileName)
        }

        //delete damage itself
        App.database.damageDao().delete(deletedDamage)

    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true;
    }

    fun startInterventionActivity(){
        val intent = Intent(this, InterventionActivity::class.java)
        intent.putExtra(InterventionActivity.EXTRA_INTERVENTION_ID, intervention.id)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_top, R.anim.no_anim)
    }

    override fun onResume() {
        Log.i(TAG, "onResume()")
        super.onResume()
        loadDamagesFromDb()
        updateDamageList()
    }

    fun loadDamagesFromDb(){
        damages = App.database.damageDao().getDamagesByBladeAndInterventionAndInheritType(
            blade.id,
            intervention.id,
            damageInheritType
        )
        damages.sortBy { it.fieldCode}

        adapter = DamageAdapter(damages, this)
        adapter.scopeMode = scopeSwitch.isChecked
        recyclerView.adapter = adapter
    }

    override fun onDamageSelected(damage: DamageSpotCondition) {
        launchDamagePager(damage.localId)
    }

    override fun onCameraButtonClicked(damage: DamageSpotCondition) {
        val intent = Intent(this, CameraActivity::class.java)
        var path = App.getDamagePath(intervention, blade, damage.localId)
        intent.putExtra(CameraActivity.EXTRA_PICTURE_TYPE, PICTURE_TYPE_DAMAGE)
        intent.putExtra(CameraActivity.EXTRA_RELATED_ID, damage.localId)
        intent.putExtra(CameraActivity.EXTRA_INTERVENTION_ID, intervention.id)
        intent.putExtra(CameraActivity.EXTRA_OUTPUT_PATH, path)
        startActivity(intent)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_damage_button -> launchDamagePager(addNewDamage())
        }
    }


    private fun addNewDamage(): Int {
        Log.d(TAG, "addNewDamage()")

        val prefix = if(damageInheritType == INHERIT_TYPE_DAMAGE_OUT) "D" else "i"

        var num:Int = 1;
        damages.forEach{
            try{
                num = num.coerceAtLeast(it.fieldCode.replace("[^0-9]".toRegex(), "").toInt() + 1)
            }catch (exception:Exception){}
        }
        num = num.coerceAtLeast(damages.size+1)

        var numStr:String = if(num<10)"0$num" else num.toString()

        var damage = DamageSpotCondition(
            damageInheritType,
            "$prefix$numStr",
            intervention.id,
            blade.id
        )

        val newId = App.database.damageDao().insertDamage(damage).toInt()
        damage.localId = newId
        damages.add(damage)
        damages.sortBy { it.radialPosition }

        adapter.notifyDataSetChanged()

        return newId
    }

    private fun launchDamagePager(damageLocalId: Int) {
        val intent = Intent(this, DamageViewPagerActivity::class.java)
        intent.putExtra(DamageViewPagerActivity.EXTRA_DAMAGE_SPOT_CONDITION_LOCAL_ID, damageLocalId)
        startActivity(intent)
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
            preDeleteDamage(damages[position], position)
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
}