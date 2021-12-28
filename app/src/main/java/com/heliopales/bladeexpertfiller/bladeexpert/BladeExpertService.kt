package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.App
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

private const val API_KEY = "4IwWjazZpqyJy7ISAsXNaS3HM346tlD2";


interface BladeExpertService {

    @GET(value = "api/mobile/intervention")
    fun getInterventions(@Query("key") key:String = API_KEY): Call<Array<InterventionWrapper>>

    @GET(value = "api/mobile/severity")
    fun getSeverities(@Query("key") key:String = API_KEY): Call<Array<SeverityWrapper>>

    @GET(value = "api/mobile/damagetype")
    fun getDamageTypes(@Query("key") key:String = API_KEY): Call<Array<DamageTypeWrapper>>

    @PUT(value = "api/mobile/turbineSerial")
    fun updateTurbineSerial(@Query("key") key:String = API_KEY, @Body interventionWrapper:InterventionWrapper): Call<Boolean>

    @PUT(value = "api/mobile/bladeSerial")
    fun updateBladeSerial(@Query("key") key:String = API_KEY, @Body bladeWrapper:BladeWrapper): Call<Boolean>

    @POST(value = "api/mobile/indoorDamage")
    fun saveIndoorDamageSpotCondition(mapToBladeExpertDamageSpotCondition: DamageSpotConditionWrapper)

    @POST(value = "api/mobile/outdoorDamage")
    fun saveOutdoorDamageSpotCondition(mapToBladeExpertDamageSpotCondition: DamageSpotConditionWrapper)
}