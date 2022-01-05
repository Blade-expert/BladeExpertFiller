package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition

class ReceptorMeasureAdapter(
    private val measures: List<ReceptorMeasure>,
    private val measureListener: MeasureItemListener
) : RecyclerView.Adapter<ReceptorMeasureAdapter.ViewHolder>(), View.OnClickListener {

    private val TAG = ReceptorMeasureAdapter::class.java.simpleName

    interface MeasureItemListener {
        fun onMearureSelected(measure: ReceptorMeasure)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById<CardView>(R.id.receptor_card_view)
        val receptorName: TextView = itemView.findViewById<TextView>(R.id.receptor_name)
        val receptorValue: TextView = itemView.findViewById<TextView>(R.id.receptor_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item =
            LayoutInflater.from(parent?.context).inflate(R.layout.item_receptor, parent, false)
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lrm = measures[position]
        with(holder) {
            cardView.tag = lrm
            cardView.setOnClickListener(this@ReceptorMeasureAdapter)
            receptorName.text = lrm.receptorLabel
            receptorValue.text = lrm.value?:"--"+" \u03A9"


            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_light
                )
            )
            receptorName.setTextColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )
            receptorValue.setTextColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )

            if (lrm.severityId != null) {
                val sev = App.database.severityDao().getById(lrm.severityId!!)
                if (sev != null) {
                    cardView.setCardBackgroundColor(Color.parseColor(sev.color))
                    receptorName.setTextColor(Color.parseColor(sev.fontColor))
                    receptorValue.setTextColor(Color.parseColor(sev.fontColor))
                }
            }
        }
    }

    override fun getItemCount(): Int = measures.size

    override fun onClick(view: View) {
        when(view.id){
            R.id.receptor_card_view -> measureListener.onMearureSelected(view.tag as ReceptorMeasure)
        }

    }
}