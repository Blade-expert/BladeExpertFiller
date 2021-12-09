package com.heliopales.bladeexpertfiller.secondaryentities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Severity(
    @PrimaryKey val id: Int,
    val alias: String?,
    val color: String?,
    val fontColor: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(alias)
        parcel.writeString(color)
        parcel.writeString(fontColor)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Severity> {
        override fun createFromParcel(parcel: Parcel): Severity {
            return Severity(parcel)
        }

        override fun newArray(size: Int): Array<Severity?> {
            return arrayOfNulls(size)
        }
    }


}