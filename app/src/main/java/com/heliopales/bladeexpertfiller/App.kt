package com.heliopales.bladeexpertfiller

import android.app.Application
import android.util.Log
import com.google.android.material.tabs.TabLayout
import com.heliopales.bladeexpertfiller.bladeexpert.BladeExpertService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

private const val BASE_URL = "https://www.bladeexpert-recette.herokuapp.com/bladeexpert/"


class App: Application() {



    companion object{

        lateinit var instance: App

        val database: Database by lazy{
            Database(instance)
        }

        private val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        private val retrofit = Retrofit.Builder()
            .client(httpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val bladeExpertService: BladeExpertService = retrofit.create(BladeExpertService::class.java)

        fun getOutputDirectory(): String {
            val mediaDir = instance.externalMediaDirs.firstOrNull()?.let {
                File(it, instance.resources.getString(R.string.app_name)).apply { mkdirs() } }

            var file = if (mediaDir != null && mediaDir.exists()){
                mediaDir
            }else{
                instance.filesDir
            }
            Log.i("App", "Output Directory is : ${file.absolutePath}")
            return file.absolutePath
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this


    }

}