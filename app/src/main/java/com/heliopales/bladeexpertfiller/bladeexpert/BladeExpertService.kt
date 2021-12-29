package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.App
import okhttp3.MultipartBody
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

    @Multipart
    @POST(value = "api/mobile/spotCondition/{id}/addPicture")
    fun addPictureToSpotCondition(@Path("id") spotConditionId: Int, @Query("key") key:String = API_KEY, @Part filePart: MultipartBody.Part): Call<ResponseBody>

    @POST(value = "api/mobile/indoorDamage")
    fun saveIndoorDamageSpotCondition(@Query("key") key:String = API_KEY, @Body damageSpotConditionWrapper: DamageSpotConditionWrapper): Call<DamageSpotConditionWrapper>

    @POST(value = "api/mobile/outdoorDamage")
    fun saveOutdoorDamageSpotCondition(@Query("key") key:String = API_KEY, @Body damageSpotConditionWrapper: DamageSpotConditionWrapper): Call<DamageSpotConditionWrapper>


}