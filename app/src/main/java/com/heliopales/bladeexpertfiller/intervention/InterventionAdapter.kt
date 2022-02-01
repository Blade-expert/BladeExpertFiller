package com.heliopales.bladeexpertfiller.intervention

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EXPORTED
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.R

class InterventionAdapter(
    private val interventions: List<Intervention>,
    private val interventionListener: InterventionItemListener,
private val lifecycleOwner: LifecycleOwner
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
        val indeterminateProgress = itemView.findViewById<ProgressBar>(R.id.indeterminate_progress)!!
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)!!
        val progressText = itemView.findViewById<TextView>(R.id.progress_text)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val interventionItem =
            LayoutInflater.from(parent?.context).inflate(R.layout.item_intervention, parent, false)
        return ViewHolder(interventionItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itv = interventions[position];
        with(holder) {
            turbineNameView.text = itv.name
            cardView.tag = itv
            cardView.setOnClickListener(this@InterventionAdapter)
            progressBar.visibility = View.GONE
            progressText.visibility = View.GONE
            indeterminateProgress.visibility = View.GONE
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.cardview_light_background
                )
            )
            helperView.visibility = View.GONE
            uploadButton.visibility = View.GONE

            if (itv.expired) {
                cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        cardView.context,
                        R.color.bulma_danger_light
                    )
                )
                helperView.visibility = View.VISIBLE
                helperView.text = "Intervention is not 'ON GOING'"
                helperView.setTextColor(ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_danger_dark
                ))
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
                        helperView.text = "Datas has been sent"
                        helperView.setTextColor(ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_success_dark
                        ))
                    }
                    EXPORTATION_STATE_NOT_EXPORTED -> {
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                cardView.context,
                                R.color.bulma_warning_light
                            )
                        )
                        helperView.visibility = View.VISIBLE
                        helperView.setTextColor(ContextCompat.getColor(
                            cardView.context,
                            R.color.bulma_warning_dark
                        ))

                        if(itv.exporting){
                            helperView.text = "Exporting..."
                            progressBar.visibility = View.VISIBLE
                            progressText.visibility = View.VISIBLE
                            itv.progress.observe(lifecycleOwner, Observer { newVal ->
                                progressBar.progress = newVal
                                progressText.text = "$newVal %"
                            })
                            indeterminateProgress.visibility = View.VISIBLE
                        }else{
                            if(itv.exportErrorsInLastExport){
                                helperView.setTextColor(ContextCompat.getColor(
                                    cardView.context,
                                    R.color.bulma_danger_dark
                                ))
                                helperView.text = "There was errors on exportation"
                            }else{
                                helperView.text = "Datas are not exported"
                            }
                            uploadButton.visibility = View.VISIBLE
                        }
                    }
                }
            }
            uploadButton.tag = itv
            uploadButton.setOnClickListener(this@InterventionAdapter)
        }
    }

    override fun getItemCount(): Int = interventions.size

    override fun onClick(view: View) {
        val itv = view.tag
        if(itv != null && itv is Intervention){
            if(! itv.exporting){
                when (view.id) {
                    R.id.upload_button -> {
                        interventionListener.onInterventionUploadClick(itv)
                    }
                    R.id.card_view -> {
                        interventionListener.onInterventionSelected(itv)
                    }
                }
            }
        }
    }



}