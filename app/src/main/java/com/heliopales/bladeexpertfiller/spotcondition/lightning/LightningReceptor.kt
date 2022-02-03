package com.heliopales.bladeexpertfiller.spotcondition.lightning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(primaryKeys = ["id","bladeId"])
data class LightningReceptor(
    val id: Int,
    val bladeId: Int,
    val radius: Float,
    val position: String
)