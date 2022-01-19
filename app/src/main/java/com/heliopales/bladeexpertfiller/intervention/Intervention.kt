package com.heliopales.bladeexpertfiller.intervention

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.heliopales.bladeexpertfiller.EXPORTATION_STATE_EMPTY
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Intervention(
    @PrimaryKey val id: Int,
    val turbineName: String?,
    var turbineSerial: String?,
    var exportationState: Int = EXPORTATION_STATE_EMPTY
) : Parcelable {

    var expired: Boolean = false

    @Ignore
    var exporting: Boolean = false

    @Ignore
    var progress = MutableLiveData<Int>(0)

    @Ignore
    var exportCount = 0;

    @Ignore
    val exportNumberOfOperations = MutableLiveData<Int>(0)

    @Ignore
    val exportRealizedOperations = MutableLiveData<Int>(0)

    var exportErrorsInLastExport: Boolean = false;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Intervention

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }


}

