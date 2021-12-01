package com.heliopales.bladeexpertfiller.intervention

import android.app.ActionBar
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.Database
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.utils.dpToPx
import com.heliopales.bladeexpertfiller.utils.spToPx

class InterventionDetailsActivity : AppCompatActivity() {

    companion object {
        val EXTRA_INTERVENTION = "intervention"
    }

    private lateinit var intervention: Intervention;
    private lateinit var database: Database;
    private lateinit var turbineNameView: TextView;
    private lateinit var turbineSerialView: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intervention_details)

        database = App.database;

        intervention = intent.getParcelableExtra<Intervention>(EXTRA_INTERVENTION)!!

        turbineNameView = findViewById(R.id.turbineName)
        turbineNameView.text = intervention.turbineName

        turbineSerialView = findViewById(R.id.turbineSerialNumber)
        turbineSerialView.text = if(intervention.turbineSerial == null) "-----" else intervention.turbineSerial;

        addBladeButtons()
    }

    fun addBladeButtons(){
        findViewById<LinearLayout>(R.id.bladeButtonLayout).let {
            val resources = applicationContext.resources
            val layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(dpToPx(10f))

            database.getBladesByInterventionId(intervention.id).sortedBy { bla -> bla.position }.forEach { bla ->
                val button = Button(this)
                button.text = "${bla.position}" + if(bla.serial == null) "" else " - ${bla.serial}";
                button.setPadding(dpToPx(24f))
                button.textSize = spToPx(7f)
                button.layoutParams = layoutParams
                button.setBackgroundColor(getColor(R.color.bulma_link))
                button.setTextColor(getColor(R.color.bulma_white))

                it.addView(button)
            }


        }
    }
}