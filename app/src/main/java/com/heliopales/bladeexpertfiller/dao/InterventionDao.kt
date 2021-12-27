package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.intervention.Intervention

@Dao
interface InterventionDao {
    @Query("SELECT * FROM intervention")
    fun getAllInterventions(): List<Intervention>

    @Query("SELECT * FROM intervention WHERE id= :id")
    fun getById(id: Int): Intervention

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNonExistingIntervention(intervention: Intervention) : Long

    @Delete
    fun deleteIntervention(intervention: Intervention) : Int

    @Update
    fun updateIntervention(intervention: Intervention) : Int

    @Query("UPDATE intervention SET exportationState = :state WHERE id = :interventionId")
    fun updateExportationState(interventionId: Int, state:Int)
}