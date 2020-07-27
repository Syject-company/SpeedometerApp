package com.trackingdeluxe.speedometer.data.prefs

import android.content.SharedPreferences

class PrefsHelperImpl constructor(private val sharedPreferences: SharedPreferences) : PrefsHelper {
    private val resultInMilesPrefsKey = "res_in_miles_key"
    private val lastSelectedColorKey = "last_selected_limit_color_key"
    private val lastSelectedIsSoundEnabledKey = "last_selected_sound_enabled_key"
    private val isLightThemeKey = "is_light_theme_key"

    //get is user select show result in miles
    //if user not selected before return null
    override fun isShowResultInMiles() =
        if (!sharedPreferences.contains(resultInMilesPrefsKey)) null
        else sharedPreferences.getBoolean(resultInMilesPrefsKey, false)

    /**save is user select show result in miles
     * @param resultInMilesMetric - flag is user select show result in miles
     */
    override fun saveShowResultInMiles(resultInMilesMetric: Boolean) {
        sharedPreferences.edit().putBoolean(resultInMilesPrefsKey, resultInMilesMetric).apply()
    }

    //get last user selected interval color
    override fun getLastSelectedColor() =
        sharedPreferences.getInt(lastSelectedColorKey, -1)

    /**get last user selected interval color
     * @param selectedColor - user selected last color value
     */
    override fun setLastSelectedColor(selectedColor: Int) {
        sharedPreferences.edit().putInt(lastSelectedColorKey, selectedColor).apply()
    }

    //get flag is user enable sound in last interval
    override fun getLastIsSoundEnabled() =
        sharedPreferences.getBoolean(lastSelectedIsSoundEnabledKey, true)

    /**set flag is user enable sound
     * @param isSoundEnabled - is sound enable
     */
    override fun setLastIsSoundEnabled(isSoundEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(lastSelectedIsSoundEnabledKey, isSoundEnabled).apply()
    }

    //get flag is user select light theme
    override fun isLightTheme() =
        sharedPreferences.getBoolean(isLightThemeKey, false)

    /**set flag if user enable light theme
     * @param isLightTheme - is light theme enable
     */
    override fun setIsLightThemeTheme(isLightTheme: Boolean) {
        sharedPreferences.edit().putBoolean(isLightThemeKey, isLightTheme).apply()
    }
}