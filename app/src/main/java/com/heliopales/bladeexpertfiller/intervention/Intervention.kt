package com.heliopales.bladeexpertfiller.intervention

import android.os.Parcel
import android.os.Parcelable
import com.heliopales.bladeexpertfiller.blade.Blade

data class Intervention(val id: Int, val turbineName: String?, val turbineSerial: String?) : Parcelable {

    var expired: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
        expired = parcel.readByte() != 0.toByte()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Intervention

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(turbineName)
        parcel.writeString(turbineSerial)
        parcel.writeByte(if (expired) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Intervention> {
        override fun createFromParcel(parcel: Parcel): Intervention {
            return Intervention(parcel)
        }

        override fun newArray(size: Int): Array<Intervention?> {
            return arrayOfNulls(size)
        }
    }


}