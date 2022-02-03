package com.heliopales.bladeexpertfiller.turbine

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Turbine(
    @PrimaryKey val id: Int,
    val alias: String,
    val serial: String?,
    val numInWindfarm: Int?,
    val windfarmId: Int
) : Parcelable