package com.heliopales.bladeexpertfiller.damages

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EMPTY
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import io.reactivex.internal.operators.flowable.FlowableElementAt
import kotlinx.android.parcel.Parcelize

const val INHERIT_TYPE_DAMAGE_IN = "IDC"
const val INHERIT_TYPE_DAMAGE_OUT = "ODC"


@Entity
data class DamageSpotCondition(val inheritType: String, val fieldCode: String, val interventionId: Int, val bladeId: Int) {

    @PrimaryKey(autoGenerate = true) var localId: Int = 0
    var id: Int? = null
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
class DrainholeSpotCondition(val interventionId: Int, val bladeId: Int) {
    @PrimaryKey(autoGenerate = true)
    var localId: Int? = null
    var id: Int? = null
    var severityId: Int? = null
    var description: String? = null
}

@Entity
class LightningSpotCondition(val interventionId: Int, val bladeId: Int) {
    @PrimaryKey(autoGenerate = true)
    var localId: Int? = null
    var id: Int? = null
    var description: String? = null
}