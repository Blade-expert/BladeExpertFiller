package com.heliopales.bladeexpertfiller

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.android.material.tabs.TabLayout
import com.heliopales.bladeexpertfiller.bladeexpert.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

//RECETTE
private const val BASE_URL = "https://bladeexpert-recette.herokuapp.com/bladeexpert/"

//LOCAL
//private const val BASE_URL = "http://192.168.1.181/bladeexpert/"


class App : Application() {

    private val TAG = App::class.java.simpleName

    companion object {

        lateinit var instance: App

        val database: AppDatabase by lazy {
            Room.databaseBuilder(
                instance.applicationContext,
                AppDatabase::class.java, "database-bxp-filler"
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
            .build()

        private val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val bladeExpertService: BladeExpertService = retrofit.create(BladeExpertService::class.java)

        fun getOutputDirectory(): String {
            val mediaDir = instance.externalMediaDirs.firstOrNull()?.let {
                File(it, instance.resources.getString(R.string.app_name)).apply { mkdirs() }
            }

            var file = if (mediaDir != null && mediaDir.exists()) {
                mediaDir
            } else {
                instance.filesDir
            }
            Log.i("App", "Output Directory is : ${file.absolutePath}")
            return file.absolutePath
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        updateSeverities()
        updateDamageTypes()
    }


    private fun updateSeverities() {
        Log.d(TAG, "updateSeverities()")
        bladeExpertService.getSeverities()
            .enqueue(object : retrofit2.Callback<Array<SeverityWrapper>> {
                override fun onResponse(
                    call: Call<Array<SeverityWrapper>>,
                    response: Response<Array<SeverityWrapper>>
                ) {
                    response?.body().let {
                        it?.map { sevw -> mapBladeExpertSeverity(sevw) }
                            ?.let { sevs -> database.severityDao().insertAll(sevs) }

                        it?.map { sevw -> mapBladeExpertSeverity(sevw).id }?.let { sevIds ->
                            database.severityDao().deleteWhereIdsNotIn(sevIds)
                        }
                    }

                    database.severityDao().getAll()
                        .forEach { Log.d(TAG, "Severity in database : $it") }
                }
                override fun onFailure(call: Call<Array<SeverityWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to load severities", t)
                }
            })
    }

    private fun updateDamageTypes() {
        Log.d(TAG, "updateDamageTypes()")
        bladeExpertService.getDamageTypes()
            .enqueue(object : retrofit2.Callback<Array<DamageTypeWrapper>> {
                override fun onResponse(
                    call: Call<Array<DamageTypeWrapper>>,
                    response: Response<Array<DamageTypeWrapper>>
                ) {
                    response?.body().let {
                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw) }
                            ?.let {dmts -> database.damageTypeDao().insertAll(dmts) }

                        it?.map { dmtw -> mapBladeExpertDamageType(dmtw).id }?.let { dmtIds ->
                            database.damageTypeDao().deleteWhereIdsNotIn(dmtIds)
                        }
                    }
                    Log.d(TAG, "DamageType Insert done, retrieving task")
                    database.damageTypeDao().getAllInner()
                        .forEach { Log.d(TAG, "Inner DamageType in database : $it") }
                    database.damageTypeDao().getAllOuter()
                        .forEach { Log.d(TAG, "Outer DamageType in database : $it") }
                }

                override fun onFailure(call: Call<Array<DamageTypeWrapper>>, t: Throwable) {
                    Log.e(TAG, "Impossible to load DamageTypes", t)
                }
            })
    }
}