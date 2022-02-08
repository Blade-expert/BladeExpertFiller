package com.heliopales.bladeexpertfiller.spotcondition

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.spotcondition.lightning.MeasureMethod

const val INHERIT_TYPE_DAMAGE_IN = "IDC"
const val INHERIT_TYPE_DAMAGE_OUT = "ODC"


@Entity
data class DamageSpotCondition(val inheritType: String, val fieldCode: String, val interventionId: Int, val bladeId: Int) {

    @PrimaryKey(autoGenerate = true) var localId: Int = 0
    var id: Int? = null
    var scope: String? = null
    var severityId: Int? = null
    var description: String? = null
    var damageTypeId: Int? = null
    var radialPosition: Float? = null
    var radialLength: Int? = null //Width
    var longitudinalLength: Int? = null //Length
    var repetition: Int? = null
    var position: String? = null
    var profileDepth: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DamageSpotCondition
        if (localId != other.localId) return false
        return true
    }
    override fun hashCode(): Int {
        return localId.hashCode()
    }
}

@Entity
data class DrainholeSpotCondition(val interventionId: Int, val bladeId: Int) {
    @PrimaryKey(autoGenerate = true) var localId: Int = 0
    var id: Int? = null
    var severityId: Int? = null
    var description: String? = null
}

@Entity
data class LightningSpotCondition(val interventionId: Int, val bladeId: Int) {
    @PrimaryKey(autoGenerate = true)var localId: Int = 0
    var id: Int? = null
    var description: String? = null
    var measureMethod: MeasureMethod? = null
}