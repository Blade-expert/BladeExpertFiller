package com.heliopales.bladeexpertfiller

import android.app.Application
import android.util.Log
import com.heliopales.bladeexpertfiller.bladeexpert.BladeExpertService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

private const val BASE_URL = "http://192.168.1.181/bladeexpert/"


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
    }

    override fun onCreate() {
        super.onCreate()
        instance = this


    }

}