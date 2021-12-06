package com.heliopales.bladeexpertfiller.blade

import android.os.Parcel
import android.os.Parcelable
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EMPTY
import com.heliopales.bladeexpertfiller.intervention.Intervention

data class Blade (val id:Int, val position:String?, var serial: String?, var state: Int = EXPORTATION_STATE_EMPTY) : Parcelable{

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(position)
        parcel.writeString(serial)
        parcel.writeInt(state)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Blade> {
        override fun createFromParcel(parcel: Parcel): Blade {
            return Blade(parcel)
        }

        override fun newArray(size: Int): Array<Blade?> {
            return arrayOfNulls(size)
        }
    }


}