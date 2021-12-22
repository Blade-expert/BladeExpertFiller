package com.heliopales.bladeexpertfiller.secondaryentities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.damages.DamageTypeCategory

@Entity
data class DamageType(
    @PrimaryKey val id: Int,
    val category: DamageTypeCategory?,
    val name: String,
    val inheritType: String
):Parcelable {

    @Ignore var selected: Boolean = false

    override fun toString(): String {
        return "${category?.name ?: ""} - $name"
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        DamageTypeCategory.getDamageTypeCategoryByCode(parcel.readString()),
        parcel.readString()?:"",
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(category?.code)
        parcel.writeString(name)
        parcel.writeString(inheritType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DamageType> {
        override fun createFromParcel(parcel: Parcel): DamageType {
            return DamageType(parcel)
        }
        override fun newArray(size: Int): Array<DamageType?> {
            return arrayOfNulls(size)
        }
    }

}