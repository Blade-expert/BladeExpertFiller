package com.heliopales.bladeexpertfiller.blade

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EMPTY
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.intervention.Intervention
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Blade (
    @PrimaryKey val id:Int,
    val interventionId: Int,
    val position:String?,
    var serial: String?): Parcelable{




    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Blade

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }





}