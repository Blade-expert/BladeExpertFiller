package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition

class ReceptorMeasureAdapter(
    private val measures: List<ReceptorMeasure>,
    private val listener: MeasureChangeListener
) : RecyclerView.Adapter<ReceptorMeasureAdapter.ViewHolder>(){

    private val TAG = ReceptorMeasureAdapter::class.java.simpleName


    interface MeasureChangeListener{
        fun OnMeasureChanged(position: Int, receptorValue: String)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById<CardView>(R.id.receptor_card_view)
        val receptorName: TextView = itemView.findViewById<TextView>(R.id.receptor_name)
        val receptorValue: EditText = itemView.findViewById<EditText>(R.id.receptor_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item =
            LayoutInflater.from(parent?.context).inflate(R.layout.item_receptor, parent, false)
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lrm = measures[position]
        with(holder) {
            receptorName.text = lrm.receptorLabel

            lrm.value?.let{
                receptorValue.setText(it)
            }
            receptorValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    Log.d(TAG,"absolutePosition ${holder.absoluteAdapterPosition}")
                    Log.d(TAG,"bindingPosition ${holder.bindingAdapterPosition}")
                    listener.OnMeasureChanged(holder.bindingAdapterPosition, receptorValue.text.toString())
                }
            })
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


        }
    }

    override fun getItemCount(): Int = measures.size

}