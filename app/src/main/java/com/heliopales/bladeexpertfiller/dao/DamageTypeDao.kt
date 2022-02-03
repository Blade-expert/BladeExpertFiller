package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType

@Dao
interface DamageTypeDao {

    @Query("SELECT * FROM DamageType WHERE id = :id")
    fun getById(id: Int): DamageType

    @Query("SELECT * FROM DamageType WHERE inheritType = 'IDT'")
    fun getAllInner(): List<DamageType>

    @Query("SELECT * FROM damagetype WHERE inheritType = 'ODT'")
    fun getAllOuter(): List<DamageType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(damageTypes: List<DamageType>)

    @Query("DELETE FROM damagetype WHERE id NOT IN (:dmtIds) ")
    fun deleteWhereIdsNotIn(dmtIds: List<Int>)
}