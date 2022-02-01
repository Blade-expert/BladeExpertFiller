package com.heliopales.bladeexpertfiller.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserSettings(@PrimaryKey val id: Int = 1, var userApiKey: String) {
}