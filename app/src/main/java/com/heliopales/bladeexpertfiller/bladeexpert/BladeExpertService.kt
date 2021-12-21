package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.App
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

private const val API_KEY = "4IwWjazZpqyJy7ISAsXNaS3HM346tlD2";


interface BladeExpertService {

    @GET(value = "api/mobile/intervention")
    fun getInterventions(@Query("key") key:String = API_KEY): Call<Array<InterventionWrapper>>

    @GET(value = "api/mobile/severity")
    fun getSeverities(@Query("key") key:String = API_KEY): Call<Array<SeverityWrapper>>

    @GET(value = "api/mobile/damagetype")
    fun getDamageTypes(@Query("key") key:String = API_KEY): Call<Array<DamageTypeWrapper>>
}