package com.heliopales.bladeexpertfiller.spotcondition.damages

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

class DamageAdapter(
    private val damages: List<DamageSpotCondition>,
    private val damageListener: DamageItemListener
) : RecyclerView.Adapter<DamageAdapter.ViewHolder>(), View.OnClickListener {

    private val TAG = DamageAdapter::class.java.simpleName

    interface DamageItemListener {
        fun onDamageSelected(damage: DamageSpotCondition)
        fun onCameraButtonClicked(damage: DamageSpotCondition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById<CardView>(R.id.damage_card_view)
        val textView1: TextView = itemView.findViewById<TextView>(R.id.damage_text_1)
        val textView2: TextView = itemView.findViewById<TextView>(R.id.damage_text_2)
        val fieldCode: TextView = itemView.findViewById<TextView>(R.id.damage_field_code)
        val cameraButton: ImageButton = itemView.findViewById<ImageButton>(R.id.damage_camera_button)
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
            cameraButton.tag = dsc
            Log.d(TAG,"camera tag set for damage${(cameraButton.tag as DamageSpotCondition).localId}")
            cameraButton.setOnClickListener(this@DamageAdapter)

            textView1.text =
                "R = ${if (dsc.radialPosition != null) dsc.radialPosition else "--"} m${getDamagePosition(dsc)}${getDamageProfileDepth(dsc)}"

            if (dsc.damageTypeId != null) {
                val dmt = App.database.damageTypeDao().getById(dsc.damageTypeId!!)
                textView2.text = "$dmt"
            } else {
                textView2.text = "No damage type selected"
            }

            fieldCode.text = dsc.fieldCode

            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_light
                )
            )
            textView1.setTextColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )
            textView2.setTextColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )
            fieldCode.setTextColor(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )
            cameraButton.foregroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    cardView.context,
                    R.color.bulma_black
                )
            )

            if (dsc.severityId != null) {
                val sev = App.database.severityDao().getById(dsc.severityId!!)
                if (sev != null) {
                    cardView.setCardBackgroundColor(Color.parseColor(sev.color))
                    textView1.setTextColor(Color.parseColor(sev.fontColor))
                    textView2.setTextColor(Color.parseColor(sev.fontColor))
                    fieldCode.setTextColor(Color.parseColor(sev.fontColor))
                    cameraButton.foregroundTintList = ColorStateList.valueOf(Color.parseColor(sev.fontColor))
                }
            }
        }
    }


    private fun getDamagePosition(dsc: DamageSpotCondition): String {
        when (dsc.position) {
            null -> return ""
            "OLE", "ILE" -> return " - LE"
            "OTE", "ITE" -> return " - TE"
            "OPS", "IPS" -> return " - PS"
            "OSS", "ISS" -> return " - SS"
            "IRT", "ORT" -> return " - Root"
            "ITP", "OTP" -> return " - Tip"
            "IWB" -> return ""
            "IHA" -> return " - Hatch"
        }
        return ""
    }

    private fun getDamageProfileDepth(dsc: DamageSpotCondition): String {
        when(dsc.profileDepth){
            "O1", "I1" -> return " - LE panel"
            "O2", "I2" -> return " - Spar cap"
            "O3", "I3" -> return " - TE panel"
            "W1L" -> return " - Web 1 LE Side"
            "W1T" -> return " - Web 1 TE Side"
            "W2L" -> return " - Web 2 LE Side"
            "W2T" -> return " - Web 2 TE Side"
            "W3L" -> return " - Web 3 LE Side"
            "W3T" -> return " - Web 3 TE Side"
            "W4L" -> return " - Web 4 LE Side"
            "W4T" -> return " - Web 4 TE Side"
        }
        return ""
    }


    override fun getItemCount(): Int = damages.size

    override fun onClick(view: View) {
        when(view.id){
            R.id.damage_card_view -> damageListener.onDamageSelected(view.tag as DamageSpotCondition)
            R.id.damage_camera_button -> damageListener.onCameraButtonClicked(view.tag as DamageSpotCondition)

        }

    }
}