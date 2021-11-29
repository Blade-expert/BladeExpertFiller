package com.heliopales.bladeexpertfiller

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcel
import android.os.Parcelable

private const val DATABASE_NAME = "bladeexpert-filler.db"
private const val DATABASE_VERSION = 1

private const val INTERVENTION_TABLE_NAME = "intervention"
private const val INTERVENTION_KEY_ID = "id"
private const val INTERVENTION_KEY_TURBINE_NAME = "turbine_name"


private val INTERVENTION_TABLE_CREATE = """
    CREATE TABLE $INTERVENTION_TABLE_NAME(
        $INTERVENTION_KEY_ID INTEGER PRIMARY KEY,
        $INTERVENTION_KEY_TURBINE_NAME TEXT 
    )
"""



class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(INTERVENTION_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


}