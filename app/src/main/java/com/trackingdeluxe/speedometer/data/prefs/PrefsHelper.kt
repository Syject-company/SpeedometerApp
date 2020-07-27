package com.trackingdeluxe.speedometer.data.prefs

interface PrefsHelper {

    //get is user select show result in miles
    //if user not selected before return null
    fun isShowResultInMiles(): Boolean?

    /**save is user select show result in miles
     * @param resultInMilesMetric - flag is user select show result in miles
     */
    fun saveShowResultInMiles(resultInMilesMetric: Boolean)

    //get last user selected interval color
    fun getLastSelectedColor(): Int

    /**get last user selected interval color
     * @param selectedColor - user selected last color value
     */
    fun setLastSelectedColor(selectedColor: Int)

    //get flag is user enable sound in last interval
    fun getLastIsSoundEnabled(): Boolean

    /**set flag is user enable sound
     * @param isSoundEnabled - is sound enable
     */
    fun setLastIsSoundEnabled(isSoundEnabled: Boolean)

    //get flag is user select light theme
    fun isLightTheme(): Boolean

    /**set flag if user enable light theme
     * @param isLightTheme - is light theme enable
     */
    fun setIsLightThemeTheme(isLightTheme: Boolean)
}