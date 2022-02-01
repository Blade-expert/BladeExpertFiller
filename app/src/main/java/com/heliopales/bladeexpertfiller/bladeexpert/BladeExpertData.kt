package com.heliopales.bladeexpertfiller.bladeexpert

import com.google.gson.annotations.SerializedName
import com.heliopales.bladeexpertfiller.spotcondition.lightning.MeasureMethod

data class InterventionWrapper(
    val id: Int,
    val name: String,
    val turbineId: Int,
    val turbineSerial: String?,
    val windfarmId: Int,
    val windfarmName: String,
    val blades: Array<BladeWrapper>?,
    val turbines: Array<TurbineWrapper>?
)

data class BladeWrapper(
    val id: Int,
    val interventionId: Int,
    val position: String?,
    @SerializedName("serialNumber") val serial: String?,
    val receptors: Array<LightningReceptorWrapper>?
)

data class TurbineWrapper(
    val id: Int,
    val alias: String,
    val serial: String?,
    val numInWindfarm: Int?,
    val windfarmId: Int
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
    val severityId: Int?,
    val description: String?,
    val damageTypeId: Int?,
    val radialPosition: Float?,
    val radialLength: Int?, //Width
    val longitudinalLength: Int?, //Length
    val repetition: Int?,
    val position: String?,
    val profileDepth: String?
)

data class DrainholeSpotConditionWrapper(
    val id: Int?,
    val interventionId: Int,
    val bladeId: Int,
    val severityId: Int?,
    val description: String?,
)

data class LightningSpotConditionWrapper(
    val id: Int?,
    val interventionId: Int,
    val bladeId: Int,
    val description: String?,
    val measureMethod: MeasureMethod?,
    val measures: List<LightningReceptorMeasureWrapper>
)

data class LightningReceptorMeasureWrapper(
    val receptorId: Int,
    val value: String?,
    val severityId: Int?
)