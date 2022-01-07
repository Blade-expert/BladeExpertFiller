package com.heliopales.bladeexpertfiller.spotcondition.lightning

import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["receptorId", "lightningSpotConditionId"])
data class ReceptorMeasure(
    val receptorId: Int,
    val lightningSpotConditionId: Int,
    var value: String? = null,
    var severityId: Int? = null
) {

    @Ignore
    var receptorLabel: String? = null
}