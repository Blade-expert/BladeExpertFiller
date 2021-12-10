package com.heliopales.bladeexpertfiller.damages

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.reactivex.internal.operators.flowable.FlowableElementAt

const val INHERIT_TYPE_DRAINHOLE = "DHS"
const val INHERIT_TYPE_LIGHTNING = "LSC"
const val INHERIT_TYPE_DAMAGE_IN = "IDC"
const val INHERIT_TYPE_DAMAGE_OUT = "ODC"


abstract class SpotCondition(
    var inheritType: String
) {
    @PrimaryKey(autoGenerate = true)  var localId: Int? = null
    var id: Int? = null
    var interventionId: Int? = null
    var bladeId: Int? = null
    var severityId: Int? = null
    var description: String? = null
}

@Entity
class DamageSpotCondition(inheritType:String) : SpotCondition(inheritType){
    var damageTypeId:Int? = null
    var radialPosition:Float? = null
    var radialLength:Int? = null //Width
    var longitudinalLength:Int? = null //Length
    var repetition:Int? = null
    var position:String? = null
    var profileDepth: String? = null
}

@Entity
class DrainholeSpotCondition : SpotCondition(INHERIT_TYPE_DRAINHOLE){

}

@Entity
class LightningSpotCondition : SpotCondition(INHERIT_TYPE_LIGHTNING){

}