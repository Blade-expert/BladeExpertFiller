package com.heliopales.bladeexpertfiller

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.heliopales.bladeexpertfiller.blade.Blade
import com.heliopales.bladeexpertfiller.intervention.Intervention

private const val DATABASE_NAME = "bladeexpert-filler.db"
private const val DATABASE_VERSION = 1

private const val INTERVENTION_TABLE_NAME = "intervention"
private const val INTERVENTION_KEY_ID = "id"
private const val INTERVENTION_KEY_TURBINE_NAME = "turbine_name"
private const val INTERVENTION_KEY_TURBINE_SERIAL = "turbine_serial"
private const val INTERVENTION_KEY_STATE = "exportation_state"


private const val BLADE_TABLE_NAME = "blade"
private const val BLADE_KEY_ID = "id"
private const val BLADE_KEY_POSITION = "position"
private const val BLADE_KEY_SERIAL = "serial"
private const val BLADE_KEY_INTERVENTION_ID = "intervention_id"
private const val BLADE_KEY_STATE = "exportation_state"


private const val BLADE_TABLE_CREATE = """
    CREATE TABLE $BLADE_TABLE_NAME(
        $BLADE_KEY_ID INTEGER PRIMARY KEY,
        $BLADE_KEY_POSITION TEXT,
        $BLADE_KEY_SERIAL TEXT,
        $BLADE_KEY_INTERVENTION_ID INTEGER,
        $BLADE_KEY_STATE INTEGER
    )
"""

private const val INTERVENTION_TABLE_CREATE = """
    CREATE TABLE $INTERVENTION_TABLE_NAME(
        $INTERVENTION_KEY_ID INTEGER PRIMARY KEY,
        $INTERVENTION_KEY_TURBINE_NAME TEXT,
        $INTERVENTION_KEY_TURBINE_SERIAL TEXT,
        $INTERVENTION_KEY_STATE INTEGER
    )
"""

private const val INTERVENTION_QUERY_GET_ALL = "SELECT * FROM $INTERVENTION_TABLE_NAME"

private const val BLADE_QUERY_GET_BY_INTERVENTION =
    "SELECT * FROM $BLADE_TABLE_NAME WHERE $BLADE_KEY_INTERVENTION_ID = ?"

class Database(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    val TAG = Database::class.java.simpleName

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(INTERVENTION_TABLE_CREATE)
        db?.execSQL(BLADE_TABLE_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun getAllInterventions(): MutableList<Intervention> {
        val interventions = mutableListOf<Intervention>()
        readableDatabase.rawQuery(INTERVENTION_QUERY_GET_ALL, null).use { cursor ->
            while (cursor.moveToNext()) {
                val idColumnIndex = cursor.getColumnIndex(INTERVENTION_KEY_ID)
                val turbineNameColumnIndex = cursor.getColumnIndex(INTERVENTION_KEY_TURBINE_NAME)
                val turbineSerialColumnIndex =
                    cursor.getColumnIndex(INTERVENTION_KEY_TURBINE_SERIAL)
                val stateColumnIndex = cursor.getColumnIndex(INTERVENTION_KEY_STATE)
                interventions.add(
                    Intervention(
                        cursor.getInt(idColumnIndex),
                        cursor.getString(turbineNameColumnIndex),
                        cursor.getString(turbineSerialColumnIndex),
                        cursor.getInt(stateColumnIndex)
                    )
                )
            }
        }
        return interventions
    }

    fun insertNonExistingIntervention(itv: Intervention) {
        val values = ContentValues()
        values.put(INTERVENTION_KEY_ID, itv.id)
        values.put(INTERVENTION_KEY_TURBINE_NAME, itv.turbineName)
        values.put(INTERVENTION_KEY_TURBINE_SERIAL, itv.turbineSerial)
        values.put(INTERVENTION_KEY_STATE, itv.state)
        Log.d(TAG, "Inserting intervention on ${itv.turbineName}")

        val res = writableDatabase.insertWithOnConflict(
            INTERVENTION_TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        Log.d(TAG, "=> inserted row id = $res")
    }

    fun deleteIntervention(intervention: Intervention): Boolean {
        return writableDatabase.delete(
            INTERVENTION_TABLE_NAME,
            "$INTERVENTION_KEY_ID = ?",
            arrayOf("${intervention.id}")
        ) == 1
    }

    fun insertNonExistingBlade(bla: Blade, interventionId: Int) {
        val values = ContentValues()
        values.put(BLADE_KEY_ID, bla.id)
        values.put(BLADE_KEY_POSITION, bla.position)
        values.put(BLADE_KEY_SERIAL, bla.serial)
        values.put(BLADE_KEY_INTERVENTION_ID, interventionId)
        values.put(BLADE_KEY_STATE, bla.state)

        writableDatabase.insertWithOnConflict(
            BLADE_TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun getBladesByInterventionId(interventionId: Int): MutableList<Blade> {
        val blades = mutableListOf<Blade>()
        readableDatabase.rawQuery(
            BLADE_QUERY_GET_BY_INTERVENTION,
            arrayOf(interventionId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val idColumnIndex = cursor.getColumnIndex(BLADE_KEY_ID)
                val positionColumnIndex = cursor.getColumnIndex(BLADE_KEY_POSITION)
                val serialColumnIndex = cursor.getColumnIndex(BLADE_KEY_SERIAL)
                val stateColumnIndex = cursor.getColumnIndex(BLADE_KEY_STATE)
                blades.add(
                    Blade(
                        id = cursor.getInt(idColumnIndex),
                        position = cursor.getString(positionColumnIndex),
                        serial = cursor.getString(serialColumnIndex),
                        state = cursor.getInt(stateColumnIndex)
                    )
                )
            }
        }
        return blades
    }

    fun deleteBladesOfIntervention(intervention: Intervention){
        writableDatabase.delete(
            BLADE_TABLE_NAME,
            "$BLADE_KEY_INTERVENTION_ID = ?",
            arrayOf("${intervention.id}")
        )
    }

    fun updateTurbineSerialNumber(intervention: Intervention, serial: String) : Boolean {
        val values = ContentValues()
        values.put(INTERVENTION_KEY_TURBINE_SERIAL, serial)
        Log.d(TAG, "will update turbine serial number of intervention ${intervention.id} to $serial")
        val nr = writableDatabase.update(INTERVENTION_TABLE_NAME,values, "$INTERVENTION_KEY_ID = ?", arrayOf("${intervention.id}"))
        return nr == 1
    }

    fun updateBladeSerialNumber(blade: Blade, serial: String): Boolean {
        val values = ContentValues()
        values.put(BLADE_KEY_SERIAL, serial)
        Log.d(TAG, "will update serial number of blade ${blade.id} to $serial")
        val nr = writableDatabase.update(BLADE_TABLE_NAME,values, "$BLADE_KEY_ID = ?", arrayOf("${blade.id}"))
        return nr == 1
    }

    fun updateInterventionState(intervention: Intervention) : Boolean {
        val values = ContentValues()
        values.put(INTERVENTION_KEY_STATE, intervention.state)
        val nr = writableDatabase.update(INTERVENTION_TABLE_NAME,values, "$INTERVENTION_KEY_ID = ?", arrayOf("${intervention.id}"))
        return nr == 1
    }

    fun updateBladeState(blade: Blade) : Boolean {
        val values = ContentValues()
        values.put(BLADE_KEY_STATE, blade.state)
        val nr = writableDatabase.update(BLADE_TABLE_NAME,values, "$BLADE_KEY_ID = ?", arrayOf("${blade.id}"))
        return nr == 1
    }



}