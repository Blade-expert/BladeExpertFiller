package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition

@Dao
interface DrainholeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertDrainhole(drain: DrainholeSpotCondition): Long

    @Query("SELECT * FROM DrainholeSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId")
    fun getByBladeAndIntervention(bladeId: Int, interventionId: Int): DrainholeSpotCondition?

    @Delete
    fun delete(drain: DrainholeSpotCondition)

    @Query("SELECT count(localId) FROM DrainholeSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId")
    fun countByBladeId(bladeId: Int, interventionId: Int): Int
}