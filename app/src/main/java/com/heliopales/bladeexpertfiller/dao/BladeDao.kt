package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.intervention.Intervention

@Dao
interface BladeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNonExistingBlades(bla: List<Blade>)

    @Query("SELECT * FROM blade WHERE interventionId = :interventionId")
    fun getBladesByInterventionId(interventionId: Int) : List<Blade>

    @Query("DELETE FROM blade WHERE interventionId = :interventionId")
    fun deleteBladesOfInterventionId(interventionId: Int)

    @Update
    fun updateBlade(blade: Blade): Int

    @Query("SELECT * FROM blade WHERE id = :id")
    fun getById(id: Int): Blade


}