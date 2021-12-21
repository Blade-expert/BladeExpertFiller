package com.heliopales.bladeexpertfiller.damages.editloop

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.heliopales.bladeexpertfiller.App
import com.heliopales.bladeexpertfiller.R
import com.heliopales.bladeexpertfiller.damages.DamageTypeCategory
import com.heliopales.bladeexpertfiller.damages.DamageViewPagerActivity
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_IN
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_OUT
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.utils.closeKeyboard

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DamageTypeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DamageTypeFragment : Fragment() {
    private val TAG = DamageTypeFragment::class.java.simpleName

    private lateinit var damageTypes: List<DamageType>

    private lateinit var categorySpinner: Spinner
    private lateinit var damageTypeSpinner: Spinner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when ((activity as DamageViewPagerActivity).damage.inheritType) {
            INHERIT_TYPE_DAMAGE_OUT -> damageTypes = App.database.damageTypeDao().getAllOuter()
                .sortedWith(compareBy({ it.category?.name }, { it.name }))

            INHERIT_TYPE_DAMAGE_IN -> damageTypes = App.database.damageTypeDao().getAllInner()
                .sortedWith(compareBy({ it.category?.name }, { it.name }))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categorySpinner = view.findViewById(R.id.damage_type_category_spinner)
        damageTypeSpinner = view.findViewById(R.id.damage_type_spinner)

        categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            DamageTypeCategory.values()
        )

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val damageTypeList = mutableListOf<DamageType>()
                damageTypeList.add(0, DamageType(-1, null, "- -", "IDT"))
                damageTypeList.addAll(damageTypes.filter { it.category == categorySpinner.selectedItem })
                damageTypeSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.item_spinner,
                    damageTypeList
                )
                Log.v(
                    TAG,
                    "Selected item : ${categorySpinner.selectedItem}"
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.v(
                    TAG,
                    "Nothing selected"
                )
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_damage_type, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = DamageTypeFragment()
    }

    override fun onResume() {
        super.onResume()
        activity?.closeKeyboard()
    }

}