package com.heliopales.bladeexpertfiller.damages

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R

class DamageAdapter(
    private val damages: List<DamageSpotCondition>,
    private val damageListener: DamageItemListener
) : RecyclerView.Adapter<DamageAdapter.ViewHolder>(), View.OnClickListener {

    interface DamageItemListener {
        fun onDamageSelected(damage: DamageSpotCondition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.damage_card_view)
        val textView1 = itemView.findViewById<TextView>(R.id.damage_text_1)
        val textView2 = itemView.findViewById<TextView>(R.id.damage_text_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val damageItem =
            LayoutInflater.from(parent?.context).inflate(R.layout.item_damage, parent, false)
        return ViewHolder(damageItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dsc = damages[position]
        with(holder) {
            cardView.tag = dsc
            cardView.setOnClickListener(this@DamageAdapter)
            textView1.text = "R = ${if (dsc.radialPosition != null) dsc.radialPosition else "--"} m"
            textView2.text = "test ligne 2"

            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_light
                )
            )
            textView1.setTextColor(ContextCompat.getColor(
                cardView.context,
                R.color.bulma_black
            ))
            textView2.setTextColor(ContextCompat.getColor(
                cardView.context,
                R.color.bulma_black
            ))

            if (dsc.severityId != null) {
                val sev = App.database.severityDao().getById(dsc.severityId!!)
                if (sev != null) {
                    cardView.setCardBackgroundColor(Color.parseColor(sev.color))
                    textView1.setTextColor(Color.parseColor(sev.fontColor))
                    textView2.setTextColor(Color.parseColor(sev.fontColor))
                }
            }
        }
    }

    override fun getItemCount(): Int = damages.size

    override fun onClick(view: View) {
        damageListener.onDamageSelected(view.tag as DamageSpotCondition)
    }
}