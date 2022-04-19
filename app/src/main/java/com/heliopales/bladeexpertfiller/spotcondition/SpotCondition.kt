package com.heliopales.bladeexpertfiller.spotcondition

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.spotcondition.lightning.MeasureMethod

const val INHERIT_TYPE_DAMAGE_IN = "IDC"
const val INHERIT_TYPE_DAMAGE_OUT = "ODC"


@Entity
data class DamageSpotCondition(val inheritType: String, val fieldCode: String, val interventionId: Int, val bladeId: Int, val closed:Boolean? = false) {

    @PrimaryKey(autoGenerate = true) var localId: Int = 0
    var id: Int? = null
    var scope: String? = null
    var scopeRemark: String? = null
    var spotCode: String? = null
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

    fun getSummarizedFieldCodeRadiusPosition() : String{
        var text = "$fieldCode";
        if(radialPosition != null)
            text+=" - R${radialPosition!!.toInt()}"
        if(position != null)
            text+=getDamagePositionAlias()
        return text
    }

     fun getDamagePositionAlias(): String {
        when (position) {
            null -> return ""
            "OLE", "ILE" -> return " - LE"
            "OTE", "ITE" -> return " - TE"
            "OPS", "IPS" -> return " - PS"
            "OSS", "ISS" -> return " - SS"
            "IRT", "ORT" -> return " - Root"
            "ITP", "OTP" -> return " - Tip"
            "IWB" -> return ""
            "IHA" -> return " - Hatch"
        }
        return ""
    }

     fun getDamageProfileDepthAlias(): String {
        when (profileDepth) {
            "O1", "I1" -> return " - LE panel"
            "O2", "I2" -> return " - Spar cap"
            "O3", "I3" -> return " - TE panel"
            "W1L" -> return " - Web 1 LE Side"
            "W1T" -> return " - Web 1 TE Side"
            "W2L" -> return " - Web 2 LE Side"
            "W2T" -> return " - Web 2 TE Side"
            "W3L" -> return " - Web 3 LE Side"
            "W3T" -> return " - Web 3 TE Side"
            "W4L" -> return " - Web 4 LE Side"
            "W4T" -> return " - Web 4 TE Side"
        }
        return ""
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