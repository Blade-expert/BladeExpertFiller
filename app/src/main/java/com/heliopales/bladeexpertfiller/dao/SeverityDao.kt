package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heliopales.bladeexpertfiller.secondaryentities.Severity

@Dao
interface SeverityDao {

    @Query("SELECT * FROM severity")
    fun getAll(): List<Severity>;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(severities: List<Severity>);

    @Query("DELETE FROM severity WHERE id NOT IN (:severityIds) ")
    fun deleteWhereIdsNotIn(severityIds: List<Int>)


}