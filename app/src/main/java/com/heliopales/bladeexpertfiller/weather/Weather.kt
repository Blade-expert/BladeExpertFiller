package com.heliopales.bladeexpertfiller.weather

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime

@Entity
@Parcelize
data class Weather(
    val interventionId: Int,
    @PrimaryKey(autoGenerate = true) var localId: Int = 0,
    var id: Int? = null,
    var dateTime: LocalDateTime? = null,
    var type: String? = null,
    var windspeed: Float? = null,
    var temperature: Float? = null,
    var humidity: Float? = null
) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Weather
        if (localId != other.localId) return false
        return true
    }

    override fun hashCode(): Int {
        return localId.hashCode()
    }

}

