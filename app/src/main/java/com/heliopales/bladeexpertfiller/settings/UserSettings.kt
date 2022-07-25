package com.heliopales.bladeexpertfiller.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserSettings(@PrimaryKey val id: Int = 1,
                        var serverAddress: String?=null,
                        var userApiKey: String = "",
                        var volumeKeyForPicture: Boolean? = true,
                        var cameraSounds: Boolean? = true)