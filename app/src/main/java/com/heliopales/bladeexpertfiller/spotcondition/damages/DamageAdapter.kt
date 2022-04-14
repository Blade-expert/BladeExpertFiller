package com.heliopales.bladeexpertfiller.spotcondition.damages

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition

class DamageAdapter(
    private val damages: List<DamageSpotCondition>,
    private val damageListener: DamageItemListener
) : RecyclerView.Adapter<DamageAdapter.ViewHolder>(), View.OnClickListener {

    private val TAG = DamageAdapter::class.java.simpleName

    var scopeMode = false;

    interface DamageItemListener {
        fun onDamageSelected(damage: DamageSpotCondition)
        fun onCameraButtonClicked(damage: DamageSpotCondition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.damage_card_view)
        val textView1: TextView = itemView.findViewById(R.id.damage_text_1)
        val textView2: TextView = itemView.findViewById(R.id.damage_text_2)
        val fieldCode: TextView = itemView.findViewById(R.id.damage_field_code)
        val cameraButton: ImageButton = itemView.findViewById(R.id.damage_camera_button)
        val photoCount: TextView = itemView.findViewById(R.id.photo_count)
        val uncompleted: TextView = itemView.findViewById(R.id.damage_uncompleted)
        val scopeLayout: ConstraintLayout = itemView.findViewById(R.id.damage_scope_layout)
        val damageScope: TextView = itemView.findViewById(R.id.damage_scope)
        val damageScopeIcon: ImageView = itemView.findViewById(R.id.damage_scope_icon)
        val lock: ImageView = itemView.findViewById(R.id.damage_lock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val damageItem =
            LayoutInflater.from(parent.context).inflate(R.layout.item_damage, parent, false)
        return ViewHolder(damageItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dsc = damages[position]
        with(holder) {
            cardView.tag = dsc
            cardView.setOnClickListener(this@DamageAdapter)
            cameraButton.tag = dsc
            Log.d(
                TAG,
                "camera tag set for damage${(cameraButton.tag as DamageSpotCondition).localId}"
            )
            cameraButton.setOnClickListener(this@DamageAdapter)

            textView1.text =
                "R ${if (dsc.radialPosition != null) dsc.radialPosition else "--"}${
                    dsc.getDamagePositionAlias()             
                }${dsc.getDamageProfileDepthAlias()}"

            if (dsc.damageTypeId != null) {
                val dmt = App.database.damageTypeDao().getById(dsc.damageTypeId!!)
                textView2.text = "$dmt"
            } else {
                textView2.text = "No type selected"
            }

            fieldCode.text = dsc.fieldCode

            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_light
                )
            )

            val colorStateList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )

            damageScopeIcon.imageTintList = colorStateList
            lock.imageTintList = colorStateList
            cameraButton.foregroundTintList = colorStateList

            val textViews = listOf(textView1, textView2, fieldCode, photoCount, damageScope)

            textViews.forEach {
                it.setTextColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.bulma_black
                    )
                )
            }

            if (dsc.severityId != null) {
                val sev = App.database.severityDao().getById(dsc.severityId!!)
                if (sev != null) {
                    cardView.setCardBackgroundColor(Color.parseColor(sev.color))
                    textViews.forEach {
                        it.setTextColor(Color.parseColor(sev.fontColor))
                    }
                    cameraButton.foregroundTintList =
                        ColorStateList.valueOf(Color.parseColor(sev.fontColor))
                    damageScopeIcon.imageTintList =
                        ColorStateList.valueOf(Color.parseColor(sev.fontColor))
                    lock.imageTintList = ColorStateList.valueOf(Color.parseColor(sev.fontColor))
                }
            }

            if (dsc.id == null) {
                lock.visibility = View.GONE
            } else {
                lock.visibility = View.VISIBLE
            }

            val count = App.database.pictureDao().getDamageSpotPicturesByDamageId(dsc.localId).size
            if (count > 0) {
                photoCount.text = "$count"
                photoCount.visibility = View.VISIBLE
            } else {
                photoCount.visibility = View.GONE
            }

            if (dsc.radialPosition == null || dsc.position == null)
                uncompleted.visibility = View.VISIBLE
            else
                uncompleted.visibility = View.GONE

            if (scopeMode) {
                if (dsc.scope == null || dsc.scope?.uppercase()?.trim() == "NR") {
                    scopeLayout.visibility = View.GONE
                    holder.itemView.visibility = View.GONE
                    holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                } else {
                    scopeLayout.visibility = View.VISIBLE
                    var rem = ""
                    if (dsc.scopeRemark != null && dsc.scopeRemark!!.isNotBlank()) {
                        rem = "\n" + dsc.scopeRemark!!
                    }
                    damageScope.text = dsc.scope + rem;
                    holder.itemView.visibility = View.VISIBLE
                    holder.itemView.layoutParams =
                        RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                }
            } else {
                scopeLayout.visibility = View.GONE
                holder.itemView.visibility = View.VISIBLE
                holder.itemView.layoutParams =
                    RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            }
        }
    }



    override fun getItemCount(): Int = damages.size

    override fun onClick(view: View) {
        when (view.id) {
            R.id.damage_card_view -> damageListener.onDamageSelected(view.tag as DamageSpotCondition)
            R.id.damage_camera_button -> damageListener.onCameraButtonClicked(view.tag as DamageSpotCondition)
        }
    }
}