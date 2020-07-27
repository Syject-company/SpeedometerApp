package com.trackingdeluxe.speedometer.data

import com.trackingdeluxe.speedometer.data.db.DBHelper
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.data.models.SpeedInterval
import com.trackingdeluxe.speedometer.data.prefs.PrefsHelper

class DataManager : DBHelper, PrefsHelper {
    private lateinit var dbHelper: DBHelper
    private lateinit var prefsHelper: PrefsHelper

    companion object {
        val instance = DataManager()
    }

    /** init data manager
     * @param dbHelper - class implementing interface DBHelper
     * @param prefsHelper - class implementing interface PrefsHelper
     */
    fun init(dbHelper: DBHelper, prefsHelper: PrefsHelper) {
        this.dbHelper = dbHelper
        this.prefsHelper = prefsHelper
    }

    override fun insertHistoryModel(history: History) =
        dbHelper.insertHistoryModel(history)

    override fun updateHistoryModel(history: History) {
        dbHelper.updateHistoryModel(history)
    }

    override fun deleteHistoryModel(history: History) {
        dbHelper.deleteHistoryModel(history)
    }

    override fun getAllHistory() =
        dbHelper.getAllHistory().reversed()

    override fun removeAllHistory() =
        dbHelper.removeAllHistory()

    override fun insertSpeedInterval(speedInterval: SpeedInterval) =
        dbHelper.insertSpeedInterval(speedInterval)

    override fun updateSpeedInterval(speedInterval: SpeedInterval) {
        dbHelper.updateSpeedInterval(speedInterval)
    }

    override fun deleteSpeedInterval(speedInterval: SpeedInterval) {
        dbHelper.deleteSpeedInterval(speedInterval)
    }

    override fun getAllSpeedIntervals() =
        dbHelper.getAllSpeedIntervals().sortedBy { it.speedMetPerSeconds }

    override fun isShowResultInMiles() =
        prefsHelper.isShowResultInMiles()

    override fun saveShowResultInMiles(resultInMilesMetric: Boolean) {
        prefsHelper.saveShowResultInMiles(resultInMilesMetric)
    }

    override fun getLastSelectedColor() =
        prefsHelper.getLastSelectedColor()

    override fun setLastSelectedColor(selectedColor: Int) {
        prefsHelper.setLastSelectedColor(selectedColor)
    }

    override fun getLastIsSoundEnabled() =
        prefsHelper.getLastIsSoundEnabled()

    override fun setLastIsSoundEnabled(isSoundEnabled: Boolean) {
        prefsHelper.setLastIsSoundEnabled(isSoundEnabled)
    }

    override fun isLightTheme()=
        prefsHelper.isLightTheme()

    override fun setIsLightThemeTheme(isLightTheme: Boolean) {
        prefsHelper.setIsLightThemeTheme(isLightTheme)
    }
}