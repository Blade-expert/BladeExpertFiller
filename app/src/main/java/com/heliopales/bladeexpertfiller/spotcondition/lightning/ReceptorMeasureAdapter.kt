package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.R

class ReceptorMeasureAdapter(
    private val measures: List<ReceptorMeasure>,
    private val listener: MeasureChangeListener
) : RecyclerView.Adapter<ReceptorMeasureAdapter.ViewHolder>() {

    private val TAG = ReceptorMeasureAdapter::class.java.simpleName


    interface MeasureChangeListener {
        fun onMeasureChanged(position: Int, receptorValue: Float?)
        fun onOverLimitChanged(position: Int, isOverLimit: Boolean)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.receptor_card_view)
        val receptorName: TextView = itemView.findViewById(R.id.receptor_name)
        val receptorValue: EditText = itemView.findViewById(R.id.receptor_value)
        val switchLabel: TextView = itemView.findViewById(R.id.lps_ol_switch_label)
        val switch: Switch = itemView.findViewById(R.id.lps_ol_switch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.item_receptor, parent, false)
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lrm = measures[position]
        with(holder) {
            receptorName.text = lrm.receptorLabel

            switch.isChecked = lrm.isOverLimit
            computeSwitchState(switch, switchLabel, lrm.isOverLimit)
            receptorValue.isEnabled = !lrm.isOverLimit

            if(lrm.value == null){
                receptorValue.text = null
            }else{
                receptorValue.setText(lrm.value.toString())
            }


            switch.setOnCheckedChangeListener { _, isChecked ->
                computeSwitchState(switch, switchLabel, isChecked)
                receptorValue.isEnabled = !isChecked
                listener.onOverLimitChanged(
                    holder.bindingAdapterPosition,
                    switch.isChecked
                )
            }

            receptorValue.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, cnt: Int, aft: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    listener.onMeasureChanged(
                        holder.bindingAdapterPosition,
                        receptorValue.text.toString().toFloatOrNull()
                    )
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

    fun computeSwitchState(switch: Switch, switchLabel: TextView, checked: Boolean){
        if(checked){
            switch.trackTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    switch.context,
                    R.color.bulma_danger
                )
            )
            switch.thumbTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    switch.context,
                    R.color.bulma_danger
                )
            )
            switchLabel.setTextColor(
                ContextCompat.getColor(
                    switchLabel.context,
                    R.color.bulma_danger
                )
            )
        }else{
            switch.trackTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    switch.context,
                    R.color.bulma_gray_light
                )
            )
            switch.thumbTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    switch.context,
                    R.color.bulma_gray_light
                )
            )
            switchLabel.setTextColor(
                ContextCompat.getColor(
                    switchLabel.context,
                    R.color.bulma_gray_light
                )
            )
        }

    }

    override fun getItemCount(): Int = measures.size



}