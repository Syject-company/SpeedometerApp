package com.trackingdeluxe.speedometer.data.db.dao

import androidx.room.*
import com.trackingdeluxe.speedometer.data.models.History

@Dao
interface HistoryDao {

    /** insert new history model
     * @param history - model to insert in database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistoryModel(history: History): Long

    /** update history model
     * @param history - model to update in database
     */
    @Update
    fun updateHistoryModel(history: History)

    /** delete history model
     * @param history - model to delete in database
     */
    @Delete
    fun deleteHistoryModel(history: History)

    // get all user history collection from database
    @Query("SELECT * FROM history")
    fun getAllHistory(): List<History>

    // remove all user history collection from database
    @Query("DELETE FROM history")
    fun removeAllHistory()
}