package com.heliopales.bladeexpertfiller.bladeexpert

import com.heliopales.bladeexpertfiller.API_KEY
import com.heliopales.bladeexpertfiller.App
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface BladeExpertService {

    @GET(value = "api/mobile/logo")
    @Streaming
    fun getLogo(
        @Query("key") key: String = API_KEY,
    ): Call<ResponseBody>

    @GET(value = "api/mobile/intervention")
    fun getInterventions(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<InterventionWrapper>>

    @GET(value = "api/mobile/pictures/current/condition/{spotConditionId}")
    fun getSpotConditionPictureIdsOfCurrentState(
        @Path("spotConditionId") spotConditionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<Long>>

    @GET(value = "api/mobile/pictures/condition/{spotConditionId}")
    fun getSpotConditionPictureIds(
        @Path("spotConditionId") spotConditionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<Long>>

    @GET(value = "api/mobile/picture/{pictureId}")
    @Streaming
    fun getSpotConditionPicture(
        @Path("pictureId") pictureId: Long,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<ResponseBody>

    @GET(value = "api/mobile/intervention/{interventionId}/{inoutdoorDamage}/blade/{bladeId}")
    fun getDamages(
        @Path("interventionId") interventionId: Int,
        @Path("bladeId") bladeId: Int,
        @Path("inoutdoorDamage") inoutdoorDamage: String,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<DamageSpotConditionWrapper>>

    @GET(value = "api/mobile/intervention/{interventionId}/drainhole")
    fun getDrainholes(
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<DrainholeSpotConditionWrapper>>

    @GET(value = "api/mobile/intervention/{interventionId}/lightning")
    fun getLightnings(
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<LightningSpotConditionWrapper>>

    @GET(value = "api/mobile/intervention/{interventionId}/weather")
    fun getWeathers(
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey
    ): Call<Array<WeatherWrapper>>

    @POST(value = "api/mobile/weather")
    fun saveWeather(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body weatherWrappers: List<WeatherWrapper>
    ): Call<List<WeatherWrapper>>

    @GET(value = "api/mobile/severity")
    fun getSeverities(@Query("key") key: String = API_KEY): Call<Array<SeverityWrapper>>

    @GET(value = "api/mobile/damagetype")
    fun getDamageTypes(@Query("key") key: String = API_KEY): Call<Array<DamageTypeWrapper>>

    @PUT(value = "api/mobile/intervention")
    fun updateIntervention(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body interventionWrapper: InterventionWrapper
    ): Call<ResponseBody>

    @PUT(value = "api/mobile/bladeSerial")
    fun updateBladeSerial(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body bladeWrapper: BladeWrapper
    ): Call<ResponseBody>

    @Multipart
    @POST(value = "api/mobile/spotCondition/{id}/addPicture")
    fun addPictureToSpotCondition(
        @Path("id") spotConditionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Part filePart: MultipartBody.Part
    ): Call<Long>


    @POST(value = "api/mobile/indoorDamage")
    fun saveIndoorDamageSpotCondition(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body damageSpotConditionWrapper: DamageSpotConditionWrapper
    ): Call<DamageSpotConditionWrapper>

    @POST(value = "api/mobile/outdoorDamage")
    fun saveOutdoorDamageSpotCondition(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body damageSpotConditionWrapper: DamageSpotConditionWrapper
    ): Call<DamageSpotConditionWrapper>

    @POST(value = "api/mobile/drainhole")
    fun saveDrainholeSpotCondition(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body drainholeSpotConditionWrapper: DrainholeSpotConditionWrapper
    ): Call<DrainholeSpotConditionWrapper>

    @POST(value = "api/mobile/lightning")
    fun saveLightningSpotCondition(
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Body lightningSpotConditionWrapper: LightningSpotConditionWrapper
    ): Call<LightningSpotConditionWrapper>

    @Multipart
    @POST(value = "api/mobile/intervention/{interventionId}/turbine/{turbineId}/turbineImage")
    fun addPictureToTurbine(
        @Path("turbineId") turbineId: Int,
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Part filePart: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST(value = "api/mobile/intervention/{interventionId}/interventionImage")
    fun addPictureToIntervention(
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Part filePart: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST(value = "api/mobile/intervention/{interventionId}/blade/{bladeId}/bladeImage")
    fun addPictureToBlade(
        @Path("bladeId") bladeId: Int,
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Part filePart: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST(value = "api/mobile/intervention/{interventionId}/windfarm/{windfarmId}/windfarmImage")
    fun addPictureToWindfarm(
        @Path("windfarmId") windfarmId: Int,
        @Path("interventionId") interventionId: Int,
        @Query("key") key: String = API_KEY,
        @Query("userKey") userKey: String? = App.database.userSettingsDao()
            .getUserSettings()?.userApiKey,
        @Part filePart: MultipartBody.Part
    ): Call<ResponseBody>

}