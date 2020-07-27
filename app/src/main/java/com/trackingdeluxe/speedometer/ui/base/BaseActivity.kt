package com.trackingdeluxe.speedometer.ui.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.SpeedometerApplication
import com.trackingdeluxe.speedometer.data.DataManager
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.utils.AppUtils
import com.trackingdeluxe.speedometer.utils.MetricUtils
import com.trackingdeluxe.speedometer.utils.PermissionUtils


abstract class BaseActivity : AppCompatActivity() {
    var permissionUtils = PermissionUtils()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(getLayoutID())
        if (isPortraitOrientation()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        applyTheme()
        init(savedInstanceState)
    }

    //set flag is portrait orientation in child activities
    open fun isPortraitOrientation() = false

    /** get color value by resource id
     *@param colorID - resource color id
     */
    fun getAppColor(colorID: Int) =
        ContextCompat.getColor(this, colorID)

    //Init child activities
    abstract fun init(savedInstanceState: Bundle?)

    /**
     *Get activity layout id
     *@return layout id
     */
    abstract fun getLayoutID(): Int

    /**
     *Get speedometer application class in child activities
     *@return SpeedometerApplication class
     */
    fun getSpeedometerApplication() = application as SpeedometerApplication

    //Check permissions result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionUtils.onRequestPermissionsResult(grantResults, permissions)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //hide keyboard
    open fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /** build speed text according metric type
     * @param metricType - current metric type
     * @param format - text format to get formatted text
     * @param speed - user speed in meter per seconds
     */
    fun getSpeedText(metricType: MetricType, format: String, speed: Float?) =
        format.format(calcSpeed(metricType, speed)) + " " + getString(metricType.perHourText)

    /** calculate user speed according metric type
     * @param metricType - current metric type
     * @param speed - user speed in meter per seconds
     */
    fun calcSpeed(metricType: MetricType, speed: Float?) =
        MetricUtils.byType(metricType).calcSpeedFromMetersPerSecond(speed ?: 0f)

    /** build user distance according metric type by short distance metric text
     * @param metricType - current metric type
     * @param distance - user distance
     */
    fun getShortDistanceText(metricType: MetricType, distance: Float?) =
        "%.3f".format(MetricUtils.byType(metricType).calcDistance(distance ?: 0f)) + " " +
                getString(metricType.metricShortText)


    /** build ride time text
     * @param rideTimeInMilliseconds - user ride time in milliseconds
     */
    fun getRideTime(rideTimeInMilliseconds: Long?) =
        AppUtils.getTimeFromMilliseconds(
            rideTimeInMilliseconds ?: 0L,
            getString(R.string.minute),
            getString(R.string.hour),
            getString(R.string.day)
        )

    /** Calculate speed to meters per seconds according speed by metric type
     * @param metricType - current metric type
     * @param speed - speed according metric type
     */
    fun calcSpeedToMeterPerSeconds(metricType: MetricType, speed: Float) =
        MetricUtils.byType(metricType).calcSpeedToMeterPerSeconds(speed)

    //apply background color when activity started
    private fun applyTheme() {
        setBackgroundByTheme(DataManager.instance.isLightTheme())
    }

    /** apply background color according user theme
     * @param isLightTheme - flag if user select light theme
     */
    fun setBackgroundByTheme(isLightTheme: Boolean) {
        val rootView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        rootView.setBackgroundColor(
            getAppColor(
                if (isLightTheme) R.color.white
                else R.color.backgroundColor
            )
        )
    }
}