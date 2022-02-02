package com.heliopales.bladeexpertfiller.picture

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_NOT_EXPORTED
import java.net.URI

@Entity
data class Picture(
    @PrimaryKey val fileName: String,
    val absolutePath: String,
    val uri: String, //To Retrieve URI => Uri.parse(s);
    val type: Int,
    val relatedId: Int,
    val interventionId: Int,
    val exportState: Int = EXPORTATION_STATE_NOT_EXPORTED,
    ) {
}