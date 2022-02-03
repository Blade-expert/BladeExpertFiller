package com.heliopales.bladeexpertfiller.spotcondition.damages.editloop

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.spotcondition.damages.*
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.spotcondition.INHERIT_TYPE_DAMAGE_OUT

class DamageTypeFragment : Fragment(), View.OnClickListener {
    private val TAG = DamageTypeFragment::class.java.simpleName

    private lateinit var damageTypes: List<DamageType>

    private lateinit var list: ExpandableListView
    private lateinit var listAdapter: ExpandableListAdapter

    private lateinit var naButton: Button

    private lateinit var damage: DamageSpotCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate()")
        damage = (activity as DamageViewPagerActivity).damage
        when (damage.inheritType) {
            INHERIT_TYPE_DAMAGE_OUT -> damageTypes = App.database.damageTypeDao().getAllOuter()
                .sortedWith(compareBy({ it.category?.name }, { it.name }))

            INHERIT_TYPE_DAMAGE_IN -> damageTypes = App.database.damageTypeDao().getAllInner()
                .sortedWith(compareBy({ it.category?.name }, { it.name }))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated()")

        naButton = view.findViewById<Button>(R.id.button_dmt_na)
        naButton.setOnClickListener(this)

        list = view.findViewById(R.id.expandable_dmt)

        val data = HashMap<DamageTypeCategory, List<DamageType>>()

        DamageTypeCategory.values().forEach {
            val l = damageTypes.filter { dmt -> dmt.category == it }
            if (l.isNotEmpty())
                data[it] = l
        }
        listAdapter = CustomExpandableListAdapter(
            requireContext(),
            ArrayList(data.keys).sortedBy { it.name },
            data
        )
        list.setAdapter(listAdapter)

        list.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val selectedDamageType =
                listAdapter.getChild(groupPosition, childPosition) as DamageType
            damage.damageTypeId = selectedDamageType.id
            App.database.interventionDao().updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            naButton.foreground = null
            (listAdapter as CustomExpandableListAdapter).dataList.forEach { entry ->
                entry.value.forEach {
                    it.selected = it.id == selectedDamageType.id
                }
            }
            (listAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
            (activity as DamageViewPagerActivity).pager.currentItem = INDEX_DAMAGE_LOOP_SEVE
            true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        return inflater.inflate(R.layout.fragment_damage_type, container, false)
    }


    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume()")
        (requireActivity() as DamageViewPagerActivity).hideKeyboard()

        if (damage.damageTypeId != null) {
            naButton.foreground = null

            Log.d(TAG, "id not null, retrieveing")

            var groupToExpand = 0
            var indexOffset = 0
            (listAdapter as CustomExpandableListAdapter).titleList.forEachIndexed { index, damageTypeCategory ->
                (listAdapter as CustomExpandableListAdapter).dataList[damageTypeCategory]?.forEachIndexed { i, dmt ->
                    if (damage.damageTypeId == dmt.id) {
                        groupToExpand = index
                        dmt.selected = true
                        indexOffset = i
                    } else
                        dmt.selected = false
                }
            }
            (listAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
            if (!list.isGroupExpanded(groupToExpand)) list.expandGroup(groupToExpand)

            list.smoothScrollToPosition(groupToExpand + indexOffset + 1)


        } else {
            naButton.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
            (listAdapter as CustomExpandableListAdapter).titleList.forEachIndexed { index, damageTypeCategory ->
                (listAdapter as CustomExpandableListAdapter).dataList[damageTypeCategory]?.forEachIndexed { i, dmt ->
                    dmt.selected = false
                }
                if (list.isGroupExpanded(index)) list.collapseGroup(index)
            }
            (listAdapter as CustomExpandableListAdapter).notifyDataSetChanged()

        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_dmt_na -> {
                App.database.interventionDao().updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
                v.foreground = requireContext().getDrawable(R.drawable.ic_baseline_crop_din_24)
                damage.damageTypeId = null
                (listAdapter as CustomExpandableListAdapter).dataList.forEach { entry ->
                    entry.value.forEach { it.selected = false }
                }
                (listAdapter as CustomExpandableListAdapter).notifyDataSetChanged()
                (activity as DamageViewPagerActivity).pager.currentItem = INDEX_DAMAGE_LOOP_SEVE
            }
        }
    }

}