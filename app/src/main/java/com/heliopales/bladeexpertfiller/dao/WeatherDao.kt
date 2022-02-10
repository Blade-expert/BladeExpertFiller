package com.heliopales.bladeexpertfiller.dao

import androidx.room.*
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.weather.Weather

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(weather: Weather): Long

    @Query("SELECT * FROM Weather WHERE id = :remoteId")
    fun getWeatherByRemoteId(remoteId: Int): Weather?

    @Query("SELECT * FROM Weather WHERE interventionId = :interventionId")
    fun getWeathersByInterventionId(interventionId: Int): MutableList<Weather>

    @Delete
    fun delete(weather: Weather)
}