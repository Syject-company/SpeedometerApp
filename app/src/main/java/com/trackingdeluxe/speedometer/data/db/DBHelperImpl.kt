package com.trackingdeluxe.speedometer.data.db

import com.trackingdeluxe.speedometer.data.db.database.AppDataBase
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.data.models.SpeedInterval

class DBHelperImpl constructor(private val appDataBase: AppDataBase) : DBHelper {

    /** insert new history model
     * @param history - model to insert in database
     */
    override fun insertHistoryModel(history: History) =
        appDataBase.historyDao().insertHistoryModel(history)

    /** update history model
     * @param history - model to update in database
     */
    override fun updateHistoryModel(history: History) {
        appDataBase.historyDao().updateHistoryModel(history)
    }

    /** delete history model
     * @param history - model to delete in database
     */
    override fun deleteHistoryModel(history: History) {
        appDataBase.historyDao().deleteHistoryModel(history)
    }

    // get all user history collection from database
    override fun getAllHistory(): List<History> =
        appDataBase.historyDao().getAllHistory()

    // remove all user history collection from database
    override fun removeAllHistory() {
        appDataBase.historyDao().removeAllHistory()
    }

    /** insert new speed interval model
     * @param speedInterval - model to insert in database
     */
    override fun insertSpeedInterval(speedInterval: SpeedInterval) =
        appDataBase.speedIntervalDao().insertSpeedInterval(speedInterval)

    /** update speed interval model
     * @param speedInterval - model to update in database
     */
    override fun updateSpeedInterval(speedInterval: SpeedInterval) {
        appDataBase.speedIntervalDao().updateSpeedInterval(speedInterval)
    }

    /** delete speed interval model
     * @param speedInterval - model to delete in database
     */
    override fun deleteSpeedInterval(speedInterval: SpeedInterval) {
        appDataBase.speedIntervalDao().deleteSpeedInterval(speedInterval)
    }

    // get all user intervals collection from database
    override fun getAllSpeedIntervals() =
        appDataBase.speedIntervalDao().getAllSpeedIntervals()
}