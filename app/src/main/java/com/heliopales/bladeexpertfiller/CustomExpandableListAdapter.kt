package com.heliopales.bladeexpertfiller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageTypeCategory
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType

class CustomExpandableListAdapter internal constructor(
    private val context: Context,
    val titleList: List<DamageTypeCategory>,
    val dataList: HashMap<DamageTypeCategory, List<DamageType>>
) : BaseExpandableListAdapter() {

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return this.dataList[this.titleList[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val damageType = getChild(listPosition, expandedListPosition) as DamageType
        if (convertView == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_damage_type, null)
        }
        val expandedListTextView = convertView!!.findViewById<TextView>(R.id.damage_type_text_view)
        expandedListTextView.text = damageType.toString()
        expandedListTextView.tag = damageType
        if(damageType.selected){
            expandedListTextView.setBackgroundColor(context.getColor(R.color.bulma_link))
            expandedListTextView.setTextColor(context.getColor(R.color.bulma_white))
        }else{
            expandedListTextView.setBackgroundColor(context.getColor(R.color.bulma_white))
            expandedListTextView.setTextColor(context.getColor(R.color.bulma_black))
        }

        return convertView
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return this.dataList[this.titleList[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return this.titleList[listPosition]
    }

    override fun getGroupCount(): Int {
        return this.titleList.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as DamageTypeCategory
        if (convertView == null) {
            val layoutInflater =
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.item_damage_type_category, null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.damage_type_category_text_view)
        listTitleTextView.text = listTitle.toString()
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}