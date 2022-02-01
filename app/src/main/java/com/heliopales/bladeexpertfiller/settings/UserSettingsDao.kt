package com.heliopales.bladeexpertfiller.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query

@Dao
interface UserSettingsDao {

    @Query("SELECT * FROM UserSettings")
    fun getUserSettings(): UserSettings?

    @Insert(onConflict = REPLACE)
    fun upsert(settings: UserSettings)
}