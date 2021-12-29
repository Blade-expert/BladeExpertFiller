package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_BLADE
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_TURBINE
import com.heliopales.bladeexpertfiller.picture.Picture

@Dao
interface PictureDao {

    @Insert(onConflict = REPLACE)
    fun insertPicture(pic: Picture)

    @Query("DELETE FROM picture WHERE fileName = :fileName")
    fun deletePictureByFileName(fileName: String)

    @Query("UPDATE picture SET exportState = :state WHERE fileName = :fileName")
    fun updateExportationState(fileName: String, state: Int)

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_DAMAGE and relatedId = :damageLocalId")
    fun getDamageSpotPicturesByDamageId(damageLocalId: Int) : List<Picture>

    @Query("SELECT * FROM picture WHERE type = $PICTURE_TYPE_DAMAGE and relatedId = :damageLocalId and exportState=$EXPORTATION_STATE_NOT_EXPORTED")
    fun getNonExportedDamageSpotPicturesByDamageId(damageLocalId: Int) : List<Picture>

    @Query("SELECT * FROM picture")
    fun getAll(): List<Picture>

    @Query("DELETE FROM picture WHERE type = $PICTURE_TYPE_DAMAGE and relatedId = :damageLocalId")
    fun deleteDamagePictures(damageLocalId: Int)

    @Query("DELETE FROM picture WHERE type = $PICTURE_TYPE_BLADE and relatedId = :bladeId")
    fun deleteBladePictures(bladeId: Int)

    @Query("DELETE FROM picture WHERE type = $PICTURE_TYPE_TURBINE and relatedId = :interventionId")
    fun deleteTurbinePictures(interventionId: Int)
}