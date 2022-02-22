package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heliopales.bladeexpertfiller.spotcondition.lightning.ReceptorMeasure

@Dao
interface ReceptorMeasureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(measure: ReceptorMeasure)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(measures: List<ReceptorMeasure>)

    @Query("DELETE FROM ReceptorMeasure WHERE lightningSpotConditionLocalId=:lightningLocalId")
    fun deleteByLightningLocalId(lightningLocalId: Int)

    @Query("SELECT * FROM ReceptorMeasure WHERE receptorId=:receptorId AND lightningSpotConditionLocalId=:lightningId")
    fun getByReceptorIdAndLightningId(receptorId: Int, lightningId: Int): ReceptorMeasure

    @Query("SELECT * FROM ReceptorMeasure WHERE lightningSpotConditionLocalId=:lightningLocalId")
    fun getByLightningSpotConditionLocalId(lightningLocalId: Int): List<ReceptorMeasure>

    @Query("SELECT count(receptorId) FROM ReceptorMeasure WHERE lightningSpotConditionLocalId=:lightningLocalId AND (value is not null OR isOverLimit = 1)")
    fun getCountByLightningSpotConditionLocalId(lightningLocalId: Int): Int

}