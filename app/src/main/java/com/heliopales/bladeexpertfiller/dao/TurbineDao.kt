package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.turbine.Turbine

@Dao
interface TurbineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertTurbines(turbines: List<Turbine>)

    @Query("SELECT * FROM Turbine WHERE windfarmId = :wfmId")
    fun getTurbinesByWindfarmId(wfmId: Int) : List<Turbine>

    @Query("SELECT * FROM Turbine WHERE id = :trbId")
    fun getTurbinesById(trbId: Int) : Turbine?
}