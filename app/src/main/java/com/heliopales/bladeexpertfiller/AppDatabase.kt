package com.heliopales.bladeexpertfiller

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.damages.DamageSpotCondition
import com.heliopales.bladeexpertfiller.dao.*
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.secondaryentities.Severity

@Database(
    entities = [Intervention::class, Blade::class, Severity::class, DamageType::class, DamageSpotCondition::class, Picture::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun bladeDao(): BladeDao
    abstract fun severityDao(): SeverityDao
    abstract fun damageTypeDao(): DamageTypeDao
    abstract fun damageDao(): DamageDao
    abstract fun pictureDao(): PictureDao
}