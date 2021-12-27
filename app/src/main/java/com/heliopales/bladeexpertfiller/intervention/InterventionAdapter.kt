package com.heliopales.bladeexpertfiller.intervention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EXPORTED
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R

class InterventionAdapter(
    private val interventions: List<Intervention>,
    private val interventionListener: InterventionItemListener
) :
    RecyclerView.Adapter<InterventionAdapter.ViewHolder>(), View.OnClickListener {

    interface InterventionItemListener {
        fun onInterventionSelected(intervention: Intervention)
        fun onInterventionUploadClick(intervention: Intervention)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.card_view)!!
        val turbineNameView = itemView.findViewById<TextView>(R.id.turbine_name)!!
        val helperView = itemView.findViewById<TextView>(R.id.helper)!!
        val uploadButton = itemView.findViewById<ImageButton>(R.id.upload_button)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val interventionItem =
            LayoutInflater.from(parent?.context).inflate(R.layout.item_intervention, parent, false)
        return ViewHolder(interventionItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itv = interventions[position];
        with(holder) {
            turbineNameView.text = itv.turbineName
            cardView.tag = itv
            cardView.setOnClickListener(this@InterventionAdapter)
            if (itv.expired) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.bulma_danger_light
                    )
                )
                helperView.visibility = View.VISIBLE
                helperView.text = "L'intervention n'est plus en 'EN GOING'"
                helperView.setTextColor(ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_danger_dark
                ))
                uploadButton.visibility = View.GONE
            }else {
                when(itv.exportationState){
                    EXPORTATION_STATE_EXPORTED -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                cardView.context,
                                R.color.bulma_success_light
                            )
                        )
                        helperView.visibility = View.VISIBLE
                        helperView.text = "Les données ont été exportées"
                        helperView.setTextColor(ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_success_dark
                        ))
                        uploadButton.visibility = View.GONE
                    }
                    EXPORTATION_STATE_NOT_EXPORTED -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                cardView.context,
                                R.color.bulma_warning_light
                            )
                        )
                        helperView.visibility = View.VISIBLE
                        helperView.text = "Les données ne sont pas toutes exportées"
                        helperView.setTextColor(ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_warning_dark
                        ))
                        uploadButton.visibility = View.VISIBLE
                    }
                    else -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                cardView.context,
                                R.color.cardview_light_background
                            )
                        )
                        helperView.visibility = View.GONE
                        uploadButton.visibility = View.GONE
                    }
                }
            }
            uploadButton.tag = itv
            uploadButton.setOnClickListener(this@InterventionAdapter)
        }
    }

    override fun getItemCount(): Int = interventions.size

    override fun onClick(view: View) {
        when (view.id) {
            R.id.upload_button -> {
                interventionListener.onInterventionUploadClick(view.tag as Intervention)
            }
            R.id.card_view -> {
                interventionListener.onInterventionSelected(view.tag as Intervention)
            }
        }
    }



}