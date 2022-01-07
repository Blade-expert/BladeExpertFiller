package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition

@Dao
interface DamageDao {

    @Insert
    fun insertDamage(damage: DamageSpotCondition): Long

    @Update
    fun updateDamage(damage: DamageSpotCondition)

    @Query("SELECT * FROM DamageSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId AND inheritType = :inheritType")
    fun getDamagesByBladeAndInterventionAndInheritType(bladeId: Int, interventionId: Int, inheritType: String): MutableList<DamageSpotCondition>

    @Query("SELECT * FROM DamageSpotCondition WHERE localId = :localId")
    fun getDamageByLocalId(localId: Int): DamageSpotCondition

    @Query("SELECT * FROM DamageSpotCondition WHERE interventionId = :interventionId")
    fun getDamagesByInterventionId(interventionId: Int): List<DamageSpotCondition>

    @Query("SELECT COUNT(localId) FROM DamageSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId AND inheritType = :inheritType")
    fun countDamageByBladeId(bladeId: Int, interventionId: Int, inheritType: String): Int

    @Delete
    fun delete(damage: DamageSpotCondition)

}