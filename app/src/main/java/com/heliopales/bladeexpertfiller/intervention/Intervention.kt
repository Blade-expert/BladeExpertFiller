package com.heliopales.bladeexpertfiller.intervention

import android.os.Parcel
import android.os.Parcelable
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EMPTY

data class Intervention(val id: Int, val turbineName: String?, var turbineSerial: String?, var state: Int = EXPORTATION_STATE_EMPTY) : Parcelable {

    var expired: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
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
        parcel.writeInt(state)
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

