package com.heliopales.bladeexpertfiller.spotcondition.lightning

import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["receptorId", "lightningSpotConditionId"])
data class ReceptorMeasure(
    val receptorId: Int,
    val lightningSpotConditionId: Int,
    val severityId:Int? = null,
    val value: String? = null
) {

    @Ignore var receptorLabel: String? = null
}