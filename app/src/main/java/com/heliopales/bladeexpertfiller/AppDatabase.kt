package com.heliopales.bladeexpertfiller


import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.dao.*
import com.heliopales.bladeexpertfiller.intervention.Intervention
import com.heliopales.bladeexpertfiller.picture.Picture
import com.heliopales.bladeexpertfiller.secondaryentities.DamageType
import com.heliopales.bladeexpertfiller.secondaryentities.Severity
import com.heliopales.bladeexpertfiller.settings.UserSettings
import com.heliopales.bladeexpertfiller.settings.UserSettingsDao
import com.heliopales.bladeexpertfiller.spotcondition.DamageSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.DrainholeSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.LightningSpotCondition
import com.heliopales.bladeexpertfiller.spotcondition.lightning.LightningReceptor
import com.heliopales.bladeexpertfiller.spotcondition.lightning.ReceptorMeasure
import com.heliopales.bladeexpertfiller.turbine.Turbine
import com.heliopales.bladeexpertfiller.weather.Weather

@Database(
    exportSchema = true,
    entities = [Intervention::class, Blade::class, Turbine::class, Severity::class, DamageType::class,
        DamageSpotCondition::class, DrainholeSpotCondition::class, LightningSpotCondition::class,
        LightningReceptor::class, ReceptorMeasure::class, Picture::class, UserSettings::class, Weather::class],
    version = 2,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interventionDao(): InterventionDao
    abstract fun bladeDao(): BladeDao
    abstract fun turbineDao(): TurbineDao
    abstract fun severityDao(): SeverityDao
    abstract fun damageTypeDao(): DamageTypeDao
    abstract fun damageDao(): DamageDao
    abstract fun drainholeDao(): DrainholeDao
    abstract fun pictureDao(): PictureDao
    abstract fun receptorDao(): ReceptorDao
    abstract fun lightningDao(): LightningDao
    abstract fun receptorMeasureDao(): ReceptorMeasureDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun weatherDao(): WeatherDao
}