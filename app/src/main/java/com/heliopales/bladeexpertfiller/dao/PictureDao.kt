package com.heliopales.bladeexpertfiller.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.heliopales.bladeexpertfiller.PICTURE_TYPE_DAMAGE
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
}