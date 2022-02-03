package com.heliopales.bladeexpertfiller.blade

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class ChangeBladeSerialDialogFragment(val initText: String?) : DialogFragment() {

    interface ChangeBladeSerialDialogListener {
        fun onDialogPositiveClick(serial: String)
        fun onDialogNegativeClick()
    }

    var listener: ChangeBladeSerialDialogListener? = null

    lateinit var input: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        input = EditText(context)
        if (initText != null) {
            input.setText(initText, TextView.BufferType.EDITABLE);
            input.post { input.setSelection(input.text.length) }
        }
        with(input) {
            inputType = InputType.TYPE_CLASS_TEXT
            hint = "Numéro de série"
        }

        var builder = AlertDialog.Builder(context)
        builder.setTitle("Numéro de série pale")
            .setView(input)
            .setPositiveButton(
                "Enregistrer"
            ) { _, _ ->
                listener?.onDialogPositiveClick(input.text.toString())
            }
            .setNegativeButton(
                "Annuler"
            ) { dialog, _ ->
                dialog.cancel()
                listener?.onDialogNegativeClick()
            }

        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }

}