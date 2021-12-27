package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.damages.INHERIT_TYPE_DAMAGE_IN

@Dao
interface DamageDao {

    @Insert
    fun insertDamage(damage: DamageSpotCondition): Long

    @Update
    fun updateDamage(damage: DamageSpotCondition)

    @Query("SELECT * FROM DamageSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId AND inheritType = :inheritType")
    fun getDamagesByBladeAndInterventionAndInheritType(bladeId: Int, interventionId: Int, inheritType: String): MutableList<DamageSpotCondition>

    @Query("SELECT * FROM DamageSpotCondition WHERE localId = :localId")
    fun getDamageByLocalId(localId: Long): DamageSpotCondition



}