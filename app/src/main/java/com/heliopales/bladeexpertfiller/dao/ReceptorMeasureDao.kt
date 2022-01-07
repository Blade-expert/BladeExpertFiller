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

    @Query("SELECT * FROM ReceptorMeasure WHERE receptorId=:receptorId AND lightningSpotConditionId=:lightningId")
    fun getByReceptorIdAndLightningId(receptorId: Int, lightningId: Int): ReceptorMeasure


}