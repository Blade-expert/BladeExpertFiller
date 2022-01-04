package com.heliopales.bladeexpertfiller.bladeexpert

import com.google.gson.annotations.SerializedName

data class InterventionWrapper(
    val id: Int,
    val turbineName: String?,
    val turbineSerial: String?,
    val blades: Array<BladeWrapper>?,
    val changeTurbineSerialAllowed: Boolean,
    val changeTurbinePictureAllowed: Boolean,
)


data class BladeWrapper(
    val id: Int,
    val interventionId: Int,
    val position: String?,
    @SerializedName("serialNumber") val serial: String?,
    val changeSerialAllowed: Boolean,
    val changePictureAllowed: Boolean,
    val receptors: Array<LightningReceptorWrapper>?
)

data class LightningReceptorWrapper(
    val id: Int,
    val bladeId: Int,
    val radius: Float,
    val position: String
)

data class SeverityWrapper(
    val id: Int,
    val alias: String,
    val color: String,
    val fontColor: String
)

data class DamageTypeWrapper(
    val id: Int,
    @SerializedName("category") val categoryCode: String,
    val name: String,
    val inheritType: String
)

data class DamageSpotConditionWrapper(
    val id: Int?,
    val fieldCode: String,
    val interventionId: Int,
    val bladeId: Int,
    var severityId: Int?,
    var description: String?,
    var damageTypeId: Int?,
    var radialPosition: Float?,
    var radialLength: Int?, //Width
    var longitudinalLength: Int?, //Length
    var repetition: Int?,
    var position: String?,
    var profileDepth: String?
)

data class DrainholeSpotConditionWrapper(
    val id: Int?,
    val interventionId: Int,
    val bladeId: Int,
    var severityId: Int?,
    var description: String?,
)