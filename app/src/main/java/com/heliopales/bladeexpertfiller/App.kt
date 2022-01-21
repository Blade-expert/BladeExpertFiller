package com.heliopales.bladeexpertfiller

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.bladeexpert.*
import com.heliopales.bladeexpertfiller.intervention.Intervention
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.PrintWriter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

const val DATABASE_FILE_NAME = "bxpfil_db_2"

const val API_KEY = "LdVx6Ur82mm9oQZT4zSzBLea2Xnmzh4M";

//PRODUCTION
private const val BASE_URL = "https://www.blade-expert.com/"

//RECETTE
//private const val BASE_URL = "https://bladeexpert-recette.herokuapp.com/bladeexpert/"

//LOCAL
//private const val BASE_URL = "http://192.168.1.181/bladeexpert/"

private const val MAXIMUM_PARALLEL_REQUESTS = 1

class App : Application() {

    private val TAG = App::class.java.simpleName

    companion object {

        lateinit var instance: App

        val database: AppDatabase by lazy {
            Room.databaseBuilder(
                instance.applicationContext,
                AppDatabase::class.java, DATABASE_FILE_NAME
            )
                .allowMainThreadQueries()
                .build()
        }

        private val httpClient = OkHttpClient.Builder()
            .addInterceptor(run {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.apply {
                    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                }
            })
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        private fun retrofit(): Retrofit {
            httpClient.dispatcher.maxRequests = MAXIMUM_PARALLEL_REQUESTS
            httpClient.dispatcher.maxRequestsPerHost = MAXIMUM_PARALLEL_REQUESTS
            return Retrofit.Builder()
                .client(httpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val bladeExpertService: BladeExpertService =
            retrofit().create(BladeExpertService::class.java)

        private fun getOutputDirectory(): String {
            val mediaDir = instance.externalMediaDirs.firstOrNull()?.let {
                File(it, "${instance.resources.getString(R.string.app_name)}_$DATABASE_FILE_NAME").apply { mkdirs() }
            }

            var file = if (mediaDir != null && mediaDir.exists()) {
                mediaDir
            } else {
                instance.filesDir
            }
            Log.i("App", "Output Directory is : ${file.absolutePath}")
            return file.absolutePath
        }

        fun getInterventionPath(intervention: Intervention): String {
            return "${getOutputDirectory()}/intervention_${intervention.id}_${
                intervention.turbineName?.replace(
                    " ",
                    "-"
                )
            }"
        }

        fun getInterventionPath(interventionId: Int): String {
            val intervention = database.interventionDao().getById(interventionId)
            return getInterventionPath(intervention)
        }

        fun getBladePath(intervention: Intervention, blade: Blade): String {
            return "${getInterventionPath(intervention)}/blade_${blade.id}_${
                blade.position?.replace(
                    " ",
                    "-"
                )
            }"
        }

        fun getBladePath(interventionId: Int, bladeId: Int): String {
            val intervention = database.interventionDao().getById(interventionId)
            val blade = database.bladeDao().getById(bladeId)
            return getBladePath(intervention, blade)
        }

        fun getDamagePath(
            interventionId: Int,
            bladeId: Int,
            damageSpotConditionLocalId: Int?
        ): String {
            val intervention = database.interventionDao().getById(interventionId)
            val blade = database.bladeDao().getById(bladeId)
            return if (damageSpotConditionLocalId != null) {
                val damage = database.damageDao().getDamageByLocalId(damageSpotConditionLocalId)
                "${getBladePath(intervention, blade)}/damage_${damage.localId}_${damage.fieldCode}"
            } else {
                "${getBladePath(intervention, blade)}/damage_unknown"
            }
        }

        fun getDrainPath(
            interventionId: Int,
            bladeId: Int
        ): String {
            val intervention = database.interventionDao().getById(interventionId)
            val blade = database.bladeDao().getById(bladeId)
            return "${getBladePath(intervention, blade)}/drainhole"

        }

        fun getLPSPath(
            interventionId: Int,
            bladeId: Int
        ): String {
            val intervention = database.interventionDao().getById(interventionId)
            val blade = database.bladeDao().getById(bladeId)
            return "${getBladePath(intervention, blade)}/lightning"

        }

        fun writeOnInterventionLogFile(intervention: Intervention, message: String) {
            val path = File(getInterventionPath(intervention))
            if (!path.exists()) path.mkdirs()
            val logFile = File(path, "log.txt")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            val toWrite = "[${Date()}] $message\n"
            logFile.appendText(toWrite)
        }

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }


}