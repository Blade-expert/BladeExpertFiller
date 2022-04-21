package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.heliopales.bladeexpertfiller.*
import com.heliopales.bladeexpertfiller.picture.Picture

@Dao
interface PictureDao {

    @Insert(onConflict = REPLACE)
    fun insertPicture(pic: Picture)

    @Query("DELETE FROM picture WHERE fileName = :fileName")
    fun deletePictureByFileName(fileName: String)

    @Query("UPDATE picture SET exportState = :state WHERE fileName = :fileName")
    fun updateExportationState(fileName: String, state: Int)

    @Query("UPDATE picture SET remoteId = :remoteId WHERE fileName = :fileName")
    fun updateRemoteId(fileName: String, remoteId: Long)

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_DAMAGE and relatedId = :damageLocalId")
    fun getDamageSpotPicturesByDamageId(damageLocalId: Int): List<Picture>

    @Query("SELECT * FROM picture WHERE remoteId = :remoteId")
    fun getByRemoteId(remoteId: Long): Picture?

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_DAMAGE and relatedId = :damageLocalId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun getNonExportedDamageSpotPicturesByDamageId(damageLocalId: Int): List<Picture>

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_DRAIN and relatedId = :drainLocalId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun getNonExportedDrainholeSpotPicturesByDrainId(drainLocalId: Int): List<Picture>

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_LPS and relatedId = :lpsLocalId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun getNonExportedLightningSpotPicturesByLightningId(lpsLocalId: Int): List<Picture>

    @Query("SELECT * FROM picture WHERE type = :type and interventionId = :interventionId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun getNonExportedPicturesByTypeAndInterventionId(
        type: Int,
        interventionId: Int
    ): List<Picture>

    @Query("SELECT * FROM picture")
    fun getAll(): List<Picture>

    @Query("DELETE FROM picture WHERE interventionId = :interventionId")
    fun deletePicturesLinkedToIntervention(interventionId: Int)

    @Query("SELECT count(fileName) FROM picture WHERE interventionId = :interventionId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun countNonExportedPicturesByInterventionId(interventionId: Int): Int



}