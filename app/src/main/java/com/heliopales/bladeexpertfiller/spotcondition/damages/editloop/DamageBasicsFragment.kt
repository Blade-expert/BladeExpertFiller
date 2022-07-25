package com.heliopales.bladeexpertfiller.spotcondition.damages.editloop

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.camera.GalleryNiceActivity
import com.heliopales.bladeexpertfiller.intervention.InterventionListActivity
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.damages.DamageViewPagerActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.math.roundToInt


class DamageBasicsFragment : Fragment() {

    val TAG = DamageBasicsFragment::class.java.simpleName

    private lateinit var damage: DamageSpotCondition
    private lateinit var radialPosition: EditText
    private lateinit var longitudinalLength: EditText
    private lateinit var radialLength: EditText
    private lateinit var repetition: EditText
    private lateinit var description: EditText
    private lateinit var spotButton: Button
    private lateinit var damageDownloadPictures: ImageButton
    private lateinit var damageDownloadProgress: ProgressBar

    private var size: Int = 0
    private var c: Int = 0
    private val counter = MutableLiveData<Int>(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        damage = (activity as DamageViewPagerActivity).damage
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radialPosition = view.findViewById(R.id.damage_radial_position)
        longitudinalLength = view.findViewById(R.id.damage_longitudinal_length)
        radialLength = view.findViewById(R.id.damage_radial_length)
        repetition = view.findViewById(R.id.damage_repetition)
        description = view.findViewById(R.id.damage_description)
        spotButton = view.findViewById(R.id.damage_spot_button)
        spotButton.text = damage.spotCode
        if (damage.spotCode == null || damage.spotCode == "ORPHAN" || damage.spotCode == "")
            spotButton.visibility = View.INVISIBLE
        damageDownloadPictures = view.findViewById(R.id.damage_download_pictures)
        damageDownloadProgress = view.findViewById(R.id.damage_download_progress)


        attachListeners()
    }

    private fun attachListeners() {
        radialPosition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (radialPosition.text.isEmpty())
                    damage.radialPosition = null
                else
                    damage.radialPosition =
                        radialPosition.text.toString().replace(",", ".").toFloat()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        longitudinalLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (longitudinalLength.text.isEmpty())
                    damage.longitudinalLength = null
                else
                    damage.longitudinalLength =
                        longitudinalLength.text.toString().replace(",", ".")
                            .toFloat().roundToInt()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        radialLength.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (radialLength.text.isEmpty())
                    damage.radialLength = null
                else
                    damage.radialLength =
                        radialLength.text.toString().replace(",", ".")
                            .toFloat().roundToInt()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        repetition.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (repetition.text.isEmpty())
                    damage.repetition = null
                else
                    damage.repetition = repetition.text.toString().toIntOrNull()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        description.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (description.text.isEmpty())
                    damage.description = null
                else
                    damage.description = description.text.toString()
                App.database.interventionDao()
                    .updateExportationState(damage.interventionId, EXPORTATION_STATE_NOT_EXPORTED)
            }
        })

        spotButton.setOnClickListener {
            val intent = Intent(requireContext(), GalleryNiceActivity::class.java)
            val path =
                App.getDamagePath(
                    damage.interventionId,
                    damage.bladeId,
                    damage.localId
                ) + "/previous"
            intent.putExtra(GalleryNiceActivity.EXTRA_DIRECTORY_PATH, path)
            intent.putExtra(GalleryNiceActivity.EXTRA_SHOW_DELETE_BUTTON, false)
            startActivity(intent)
        }

        damageDownloadPictures.setOnClickListener {
            if (damage.id == null) return@setOnClickListener

            it.visibility = View.GONE
            damageDownloadProgress.visibility = View.VISIBLE

            damageDownloadProgress.isIndeterminate = true


            counter.removeObservers(viewLifecycleOwner)
            counter.observe(viewLifecycleOwner, Observer { count ->
                if (count == 0) return@Observer
                damageDownloadProgress.progress = count
                if (count >= size) {
                    it.visibility = View.VISIBLE
                    damageDownloadProgress.visibility = View.GONE
                }
            })

            if (App.bladeExpertService != null)
                App.bladeExpertService!!.getSpotConditionPictureIds(spotConditionId = damage.id!!)
                    .enqueue(object : retrofit2.Callback<Array<Long>> {
                        override fun onResponse(
                            call: Call<Array<Long>>,
                            response: Response<Array<Long>>
                        ) {
                            if (response.isSuccessful && response.body() != null && response.body()!!
                                    .isNotEmpty()
                            ) {
                                val directory = File(
                                    App.getDamagePath(
                                        damage.interventionId,
                                        damage.bladeId,
                                        damage.localId
                                    )
                                )
                                if (!directory.exists()) {
                                    directory.mkdirs()
                                }
                                size = response.body()!!.size

                                counter.value = 0
                                c = 0

                                damageDownloadProgress.isIndeterminate = false
                                damageDownloadProgress.min = 0
                                damageDownloadProgress.max = size

                                response.body()!!.forEach { id ->
                                    Log.d(TAG, "Id de photo a telecharger : $id")
                                    if (App.database.pictureDao().getByRemoteId(id) == null) {
                                        val newFile = File("${directory.absolutePath}/$id.jpg")
                                        downloadPictureFile(id, newFile)
                                    }
                                }
                            } else {
                                it.visibility = View.VISIBLE
                                damageDownloadProgress.visibility = View.GONE
                            }
                        }

                        override fun onFailure(call: Call<Array<Long>>, t: Throwable) {
                            it.visibility = View.VISIBLE
                            damageDownloadProgress.visibility = View.GONE
                        }
                    })


        }
    }

    private fun downloadPictureFile(id: Long, newFile: File) {
        if (App.bladeExpertService != null)
            App.bladeExpertService!!.getSpotConditionPicture(pictureId = id)
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        val body = response.body()

                        counter.postValue(++c)

                        if (response.isSuccessful && body != null) {
                            val inputStream = body.byteStream()
                            Files.copy(
                                inputStream,
                                newFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                            );
                            inputStream.close()

                            App.database.pictureDao().insertPicture(
                                Picture(
                                    fileName = newFile!!.name,
                                    absolutePath = newFile!!.absolutePath,
                                    uri = Uri.fromFile(newFile).toString(),
                                    type = PICTURE_TYPE_DAMAGE,
                                    relatedId = damage.localId,
                                    interventionId = damage.interventionId,
                                    exportState = EXPORTATION_STATE_EXPORTED,
                                    remoteId = id
                                )
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        counter.postValue(++c)
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        radialPosition.setText("${damage.radialPosition ?: ""}")
        longitudinalLength.setText("${damage.longitudinalLength ?: ""}")
        radialLength.setText("${damage.radialLength ?: ""}")
        repetition.setText("${damage.repetition ?: ""}")
        description.setText(damage.description ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_damage_basics, container, false)
    }
}