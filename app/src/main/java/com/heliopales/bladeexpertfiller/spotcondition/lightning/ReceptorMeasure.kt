package com.heliopales.bladeexpertfiller.spotcondition.lightning

import androidx.room.Entity
import androidx.room.Ignore

@Entity(primaryKeys = ["receptorId", "lightningSpotConditionLocalId"])
data class ReceptorMeasure(
    val receptorId: Int,
    val lightningSpotConditionLocalId: Int,
    var value: Float? = null,
    var isOverLimit: Boolean = false,
    var severityId: Int? = null
) {

    @Ignore
    var receptorLabel: String? = null
}