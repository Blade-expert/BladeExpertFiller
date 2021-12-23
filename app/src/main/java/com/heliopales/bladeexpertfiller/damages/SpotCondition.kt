package com.heliopales.bladeexpertfiller.damages

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.reactivex.internal.operators.flowable.FlowableElementAt
import kotlinx.android.parcel.Parcelize

const val INHERIT_TYPE_DAMAGE_IN = "IDC"
const val INHERIT_TYPE_DAMAGE_OUT = "ODC"


@Entity
data class DamageSpotCondition(val inheritType: String, val fieldCode: String) {

    @PrimaryKey(autoGenerate = true)
    var localId: Long? = null
    var id: Int? = null
    var interventionId: Int? = null
    var bladeId: Int? = null
    var severityId: Int? = null
    var description: String? = null
    var damageTypeId: Int? = null
    var radialPosition: Float? = null
    var radialLength: Int? = null //Width
    var longitudinalLength: Int? = null //Length
    var repetition: Int? = null
    var position: String? = null
    var profileDepth: String? = null

}

@Entity
class DrainholeSpotCondition {
    @PrimaryKey(autoGenerate = true)
    var localId: Int? = null
    var id: Int? = null
    var interventionId: Int? = null
    var bladeId: Int? = null
    var severityId: Int? = null
    var description: String? = null
}

@Entity
class LightningSpotCondition {
    @PrimaryKey(autoGenerate = true)
    var localId: Int? = null
    var id: Int? = null
    var interventionId: Int? = null
    var bladeId: Int? = null
    var description: String? = null
}