package com.heliopales.bladeexpertfiller

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.dao.BladeDao
import com.heliopales.bladeexpertfiller.dao.InterventionDao
import com.heliopales.bladeexpertfiller.dao.SeverityDao
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.secondaryentities.Severity

@Database(entities = [Intervention::class, Blade::class, Severity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun interventionDao() : InterventionDao
    abstract fun bladeDao() : BladeDao
    abstract fun severityDao(): SeverityDao
}