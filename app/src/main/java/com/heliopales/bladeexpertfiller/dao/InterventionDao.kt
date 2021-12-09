package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.intervention.Intervention

@Dao
interface InterventionDao {
    @Query("SELECT * FROM intervention")
    fun getAllInterventions(): List<Intervention>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNonExistingIntervention(intervention: Intervention) : Long

    @Delete
    fun deleteIntervention(intervention: Intervention) : Int

    @Update
    fun updateIntervention(intervention: Intervention) : Int
}