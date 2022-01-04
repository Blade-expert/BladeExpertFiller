package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningReceptor

@Dao
interface ReceptorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertLightningReceptors(lightningReceptors: List<LightningReceptor>)

    @Query("SELECT * FROM LightningReceptor WHERE bladeId = :bladeId")
    fun getByBladeId(bladeId:Int): List<LightningReceptor>

}