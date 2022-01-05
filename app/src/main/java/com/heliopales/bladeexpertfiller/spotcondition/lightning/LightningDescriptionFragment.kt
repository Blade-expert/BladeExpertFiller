package com.heliopales.bladeexpertfiller.spotcondition.lightning

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.intervention.ChangeTurbineSerialDialogFragment

class LightningDescriptionFragment(val description: String?) : DialogFragment() {

    private var input: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.d(this.javaClass.simpleName, "onCreateDialog()")

        val inflater = activity?.layoutInflater
        var builder = AlertDialog.Builder(activity)


        val view = inflater?.inflate(R.layout.fragment_lightning_description, null)

        builder.setView(view)
            .setPositiveButton("Save") { _, _ ->
                (requireActivity() as LightningActivity).lightning?.description =
                    input?.text.toString()
            }
            .setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()

        input = view?.findViewById(R.id.lightning_description)
        if (description != null) {
            input?.setText(description, TextView.BufferType.EDITABLE);
            input?.post { input!!.setSelection(input!!.text.length) }
        }
        input?.requestFocus()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }
}