package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition

@Dao
interface LightningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(lightningSpotCondition: LightningSpotCondition): Long

    @Query("SELECT * FROM LightningSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId")
    fun getByBladeAndIntervention(bladeId: Int, interventionId: Int): LightningSpotCondition?

    @Query("SELECT * FROM LightningSpotCondition WHERE interventionId = :interventionId")
    fun getByInterventionId(interventionId: Int): List<LightningSpotCondition>

    @Delete
    fun delete(lightning: LightningSpotCondition)

    @Query("SELECT count(localId) FROM LightningSpotCondition WHERE bladeId = :bladeId AND interventionId = :interventionId")
    fun countByBladeId(bladeId: Int, interventionId: Int): Int

    @Query("SELECT * FROM LightningSpotCondition WHERE id = :remoteId")
    fun getByRemoteId(remoteId: Int): LightningSpotCondition


}