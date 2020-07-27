package com.trackingdeluxe.speedometer.data.db.dao

import androidx.room.*
import com.trackingdeluxe.speedometer.data.models.SpeedInterval

@Dao
interface SpeedIntervalDao {

    /** insert new speed interval model
     * @param speedInterval - model to insert in database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSpeedInterval(speedInterval: SpeedInterval):Long

    /** update speed interval model
     * @param speedInterval - model to update in database
     */
    @Update
    fun updateSpeedInterval(speedInterval: SpeedInterval)


    /** delete speed interval model
     * @param speedInterval - model to delete in database
     */
    @Delete
    fun deleteSpeedInterval(speedInterval: SpeedInterval)

    // get all user intervals collection from database
    @Query("SELECT * FROM speedinterval")
    fun getAllSpeedIntervals(): List<SpeedInterval>
}