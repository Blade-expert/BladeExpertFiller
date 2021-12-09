package com.heliopales.bladeexpertfiller.bladeexpert

import com.google.gson.annotations.SerializedName

data class InterventionWrapper(
    val id: Int,
    val turbineName: String,
    val turbineSerial: String,
    val blades: Array<BladeWrapper>
)


data class BladeWrapper(
    val id: Int,
    val interventionId: Int,
    val position: String,
    @SerializedName("serialNumber") val serial: String
)

data class SeverityWrapper(
    val id: Int,
    val alias : String,
    val color: String,
    val fontColor: String
)