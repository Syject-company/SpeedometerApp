package com.trackingdeluxe.speedometer.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.trackingdeluxe.speedometer.MetricEngine
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.data.DataManager
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.data.models.SpeedInterval
import com.trackingdeluxe.speedometer.data.models.SpeedRange
import com.trackingdeluxe.speedometer.fragment.onboarding.LandingpageDialog
import com.trackingdeluxe.speedometer.ui.base.BaseActivity
import com.trackingdeluxe.speedometer.utils.AppConstants
import com.trackingdeluxe.speedometer.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_app.*
import kotlinx.android.synthetic.main.banner_view.*
import kotlinx.android.synthetic.main.content_ride_info_layout.*


class ActivityApp : BaseActivity(), AppView,
    Runnable, LandingpageDialog.OnDialogClose, View.OnClickListener {
    private val speedBundleKey = "speed_key"
    private val distanceBundleKey = "distance_key"
    private val speedRangeKey = "range_key"
    private val handlerSpeedBundleKey = "handler_speed_key"
    private val historyBundleKey = "history_key"
    private val isHudShowingBundleKey = "is_hud_showing_key"
    private val speedChangerThread = Thread(this)
    private val metricEngine = MetricEngine()
    private var needOpenSettings = true
    private var needNeedAskPermissions = true
    private var destinationSpeed: Float = 0f
    private var currentSpeed: Float = 0f
    private var isProVersion = false
    private var isHudEnable = false
    private var ringtone: Ringtone? = null

    private lateinit var metricType: MetricType
    private lateinit var rangeProvider: RangeProvider
    private lateinit var locationManager: LocationManager
    private lateinit var landingPageDialog: LandingpageDialog

    override fun getLayoutID() = R.layout.activity_app

    //Init application activity
    override fun init(savedInstanceState: Bundle?) {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        textClock.typeface = ResourcesCompat.getFont(this, R.font.agency)
        rideTimeView.typeface = ResourcesCompat.getFont(this, R.font.agency)
        metricType = getSavedMetricType()
        metricEngine.attachView(this)
        metricEngine.distanceInMeters = savedInstanceState?.getFloat(distanceBundleKey) ?: 0f
        metricEngine.speedInMetersPerSeconds = savedInstanceState?.getFloat(speedBundleKey) ?: 0f
        metricEngine.historyModel =
            if (savedInstanceState != null) savedInstanceState.getSerializable(historyBundleKey) as History
            else History()
        rangeProvider = RangeProvider()
        currentSpeed = savedInstanceState?.getFloat(handlerSpeedBundleKey) ?: 0f
        isHudEnable = savedInstanceState?.getBoolean(isHudShowingBundleKey) ?: false
        rangeProvider.lastSpeedRange =
            if (savedInstanceState != null && savedInstanceState.containsKey(speedRangeKey))
                savedInstanceState.getSerializable(speedRangeKey) as SpeedRange
            else null
        rideTimeView.base =
            SystemClock.elapsedRealtime() - metricEngine.historyModel.rideTimeInMilliseconds
        rideTimeView.setOnChronometerTickListener {
            it.text = getRideTime(SystemClock.elapsedRealtime() - it.base)
        }
        gaugeView.setValue(currentSpeed)
        textClock.setOnClickListener(this)
        settingButton.setOnClickListener(this)
        hudButton.setOnClickListener(this)
        showAd()
        if (isHudEnable) enableHud()
        rangeProvider.lastSpeedRange?.let { onSpeedRangeChange(it, false) }
        updateRideInfo(
            metricEngine.historyModel.maxSpeedInMetersPerSeconds,
            metricEngine.historyModel.getAvgSpeed(),
            metricEngine.historyModel.distanceInMeters
        )
        gaugeView.applyTheme(DataManager.instance.isLightTheme())
        onMetricTypeChanges()
    }

    //init ringtone if accept  storage permission
    private fun initRingtoneManager() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //play ringtone when rich new interval
    private fun playRingtone() {
        if (ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
        ringtone?.play()
    }

    /** load speed intervals
     *  calculate speed ranges according intervals
     *  show speed ranges in gauge
     */
    private fun showSpeedLimits() {
        Thread {
            rangeProvider.generateRange(DataManager.instance.getAllSpeedIntervals())
            if (rangeProvider.listRanges.size > 0)
                runOnUiThread { gaugeView.setSpeedRanges(rangeProvider.listRanges) }
        }.start()
    }

    /*init activity values
    get metric type if user selected in settings
    if user not select before - get default value from resource
    */
    private fun getSavedMetricType() =
        MetricType.getMetricTypeByBool(
            DataManager.instance.isShowResultInMiles() ?: resources.getBoolean(R.bool.showInMiles)
        )

    /**
     * Set max speed on gauge view according to result in miles or kilometers
     * @param maxSpeed max speed according to result in miles or kilometers
     * @param totalNick count of total nick according to result in miles or kilometers
     */
    override fun updateGaugeView(maxSpeed: Float, totalNick: Int) {
        gaugeView.setMaxValue(maxSpeed)
        gaugeView.setTotalNicks(totalNick)
    }

    /**
     * Set max speed on gauge view according to result in miles or kilometers
     * @param maxSpeed user max speed during ride
     * @param avgSpeed user avg speed during ride
     * @param distance user distance during ride
     */
    override fun updateRideInfo(maxSpeed: Float, avgSpeed: Float, distance: Float) {
        maxSpeedView.setMetricValue(getSpeedText(metricType, "%.0f", maxSpeed))
        avgSpeedView.setMetricValue(getSpeedText(metricType, "%.1f", avgSpeed))
        hudDistanceView.text = getShortDistanceText(metricType, distance)
    }

    /**get user ride time
     * @return user ride time in milliseconds
     */
    override fun getRideTime() = SystemClock.elapsedRealtime() - rideTimeView.base

    //update metric type, speed intervals, theme from ActivitySettings
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.SETTING_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data?.extras?.containsKey(AppConstants.METRIC_TYPE_EXTRA) == true) {
                metricType =
                    data.extras!!.getSerializable(AppConstants.METRIC_TYPE_EXTRA) as MetricType
                updateRideInfo(
                    metricEngine.historyModel.maxSpeedInMetersPerSeconds,
                    metricEngine.historyModel.getAvgSpeed(),
                    metricEngine.historyModel.getAvgSpeed()
                )
                onMetricTypeChanges()
            }
            if (data?.extras?.containsKey(AppConstants.SPEED_INTERVALS_EXTRA) == true) {
                rangeProvider.generateRange(data.extras!!.getParcelableArrayList(AppConstants.SPEED_INTERVALS_EXTRA)!!)
                rangeProvider.getNewSpeedRange(destinationSpeed)?.let {
                    onSpeedRangeChange(it, false)
                }
                gaugeView.setSpeedRanges(rangeProvider.listRanges)
            }
            if (data?.extras?.containsKey(AppConstants.THEME_EXTRA) == true) {
                val isLightTheme = data.extras!!.getBoolean(AppConstants.THEME_EXTRA)
                setBackgroundByTheme(isLightTheme)
                gaugeView.applyTheme(isLightTheme)
            }
        }
    }

    //Show ad in application
    private fun showAd() {
        //Check if pro version
        val sp = getSharedPreferences("my_prefs", Activity.MODE_PRIVATE)
        isProVersion = sp.getBoolean(AppConstants.IS_PRO_VERSION, false)
        if (!isProVersion) {
            hudButton.visibility = View.GONE
            proButton.setOnClickListener(this)
            getSpeedometerApplication().showAd()
        } else { // Hide banner,hud button if Pro version
            hudButton.setOnClickListener(this)
            proButton.visibility = View.GONE
            banner_view?.visibility = View.GONE
            showSpeedLimits()
        }
        more_info?.setOnClickListener { showLandingPage() }
        if (close_landingpage != null) {
            close_landingpage.setOnClickListener {
                banner_view.visibility = View.GONE
            }
        }
    }

    /**
     *Show distance and speed on ui
     * @param speed user speed
     */
    @SuppressLint("SetTextI18n")
    override fun updateSpeed(speed: Float) {
        destinationSpeed = calcSpeed(metricType, speed)
        gaugeView.setSpeedValueText("%.0f".format(destinationSpeed))
        rangeProvider.getNewSpeedRange(destinationSpeed)?.let {
            onSpeedRangeChange(it, true)
        }
    }

    /**trigger when user rich new speed range
     * @param currentSpeedRange - new user range
     * @param withSound - flag is need to play sound if enable in range
     */
    private fun onSpeedRangeChange(currentSpeedRange: SpeedRange, withSound: Boolean) {
        if (currentSpeedRange.isEnableSound && withSound) {
            playRingtone()
        }
        gaugeView.setTextColor(currentSpeedRange.rangeColor)
    }

    /**
     *Start update gauge thread
     *Check is location permission is granted
     * if granted register location permission
     *Check is location provider by GPS enabled
     * if location provider by GPS not provider show dialog to open location setting
     */
    override fun onResume() {
        super.onResume()
        if (speedChangerThread.state == Thread.State.NEW) {
            speedChangerThread.start()
        }
        if (!permissionUtils.hasPermission(this) && needNeedAskPermissions) {
            permissionUtils.askPermission(this,
                object : PermissionUtils.OnPermissionResult {
                    override fun permissionResult(grantedPermissions: Array<out String>) {
                        if (grantedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            grantedPermissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                        ) {
                            registerLocationListener()
                            startChronometer()
                        }
                        if (grantedPermissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            initRingtoneManager()
                        }
                        needNeedAskPermissions = false
                    }
                })
        } else if (permissionUtils.hasLocationPermission(this)) {
            registerLocationListener()
            startChronometer()
        } else if (permissionUtils.hasStoragePermission(this)) {
            initRingtoneManager()
        }
        if (!isGPSEnabled()) {
            if (needOpenSettings) {
                showLocationSettingDialog()
                needOpenSettings = false
            }
        }
    }

    //update views by metric type
    private fun changeMetricResult() {
        updateSpeed(metricEngine.speedInMetersPerSeconds)
        updateGaugeView(metricType.maxSpeed, metricType.totalPreNick)
    }

    //start ride chronometer
    private fun startChronometer() {
        rideTimeView.start()
    }

    /**
     *Check is enabled provider by GPS or not
     *@return is enabled provider by GPS or not
     */
    private fun isGPSEnabled() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    //Show dialog to open location setting
    private fun showLocationSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.dialog_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.open_setting)) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    /**
     * Unregister location listener when application is paused
     * Stop update speed gaugeю
     * Save to preference is need show metric result in miles
     * Stop zeroing speed handler
     */
    override fun onPause() {
        super.onPause()
        unRegisterLocationListener()
        speedChangerThread.interrupt()
        metricEngine.stopClearSpeedHandler()
        val history = metricEngine.getRideHistory()
        if (history.distanceInMeters > 0f) {
            history.id = DataManager.instance.insertHistoryModel(history)
        }
    }

    //Remove location updates
    private fun unRegisterLocationListener() {
        locationManager.removeUpdates(metricEngine)
    }

    //Register location updates
    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            AppConstants.UPDATE_LOCATION_TIME,
            AppConstants.UPDATE_LOCATION_DISTANCE,
            metricEngine
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            AppConstants.UPDATE_LOCATION_TIME,
            AppConstants.UPDATE_LOCATION_DISTANCE,
            metricEngine
        )
    }

    //save application data when application paused
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(distanceBundleKey, metricEngine.distanceInMeters)
        outState.putFloat(speedBundleKey, metricEngine.speedInMetersPerSeconds)
        outState.putFloat(handlerSpeedBundleKey, currentSpeed)
        outState.putSerializable(historyBundleKey, metricEngine.getRideHistory())
        outState.putBoolean(isHudShowingBundleKey, isHudEnable)
        rangeProvider.lastSpeedRange?.let { outState.putSerializable(speedRangeKey, it) }
        super.onSaveInstanceState(outState)
    }

    //Increase or decrease gauge view every 50 milliseconds by step 0.2 points
    override fun run() {
        while (!speedChangerThread.isInterrupted) {
            if (destinationSpeed >= currentSpeed) {
                currentSpeed += 0.2f
                if (currentSpeed >= destinationSpeed) {
                    currentSpeed = destinationSpeed
                }
            } else {
                currentSpeed -= 0.2f
                if (currentSpeed <= destinationSpeed) {
                    currentSpeed = destinationSpeed
                }
            }
            runOnUiThread {
                gaugeView.moveToValue(currentSpeed)
            }
            try {
                Thread.sleep(20)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //Listener for radio group: show location in miles or kilometers
//If current speed more max speed set max speed in gauge
    private fun onMetricTypeChanges() {
        if (currentSpeed > metricType.maxSpeed) {
            currentSpeed = metricType.maxSpeed
        }
        changeMetricResult()
        gaugeView.setValue(currentSpeed)
        gaugeView.setMetricTypeText(getString(metricType.perHourText))
    }

    override fun closeDialog(boughtPremium: Boolean) {
        if (boughtPremium) {
            recreate()
        } else {
            landingPageDialog.dismiss()
        }
    }

    private fun showLandingPage() {
        val fm = supportFragmentManager
        landingPageDialog = LandingpageDialog.newInstance("More information")
        landingPageDialog.show(fm, "fragment_landingpage")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.settingButton -> {
                val intent = Intent(this, ActivitySetting::class.java)
                intent.putExtra(AppConstants.METRIC_TYPE_EXTRA, metricType)
                intent.putExtra(AppConstants.IS_PRO_VERSION, isProVersion)
                startActivityForResult(intent, AppConstants.SETTING_REQUEST_CODE)
            }
            R.id.textClock -> {
                startActivity(Intent(this, ActivityHistory::class.java))
            }
            R.id.proButton -> {
                showLandingPage()
            }
            R.id.hudButton -> {
                isHudEnable = !isHudEnable
                if (isHudEnable) enableHud()
                else disableHud()
            }
        }
    }

    //enable hud mode
    private fun enableHud() {
        mirrorLayout.scaleY = -1f
    }

    //disable hud mode
    private fun disableHud() {
        mirrorLayout.scaleY = 1f
    }

    inner class RangeProvider {
        val listRanges = mutableListOf<SpeedRange>()
        var lastSpeedRange: SpeedRange? = null
        private val defaultRangeColor = getAppColor(R.color.mainGreenColor)

        /** get user range according user speed
         * if speed is rich max speed - return last item
         * @param speed - current user speed
         */
        fun getNewSpeedRange(speed: Float): SpeedRange? {
            if (listRanges.size == 0) {
                return null
            }
            listRanges.forEach { currentRange ->
                if (speed >= currentRange.from && speed < currentRange.to) {
                    lastSpeedRange?.let {
                        if (it == currentRange) {
                            return null
                        }
                    }
                    lastSpeedRange = currentRange
                    return lastSpeedRange
                }
            }
            if (speed >= metricType.maxSpeed) {
                lastSpeedRange = listRanges.last()
                return lastSpeedRange
            }
            return null
        }

        /** generate speed ranges from user intervals
         * @param speedIntervals - collection of user intervals
         */
        fun generateRange(speedIntervals: List<SpeedInterval>) {
            listRanges.clear()
            if (speedIntervals.isNotEmpty()) {
                val firsInterval = speedIntervals[0]
                listRanges.add(
                    SpeedRange(
                        0f,
                        calcSpeed(metricType, firsInterval.speedMetPerSeconds),
                        false,
                        defaultRangeColor
                    )
                )
                for (i in speedIntervals.indices) {
                    listRanges.add(
                        SpeedRange(
                            calcSpeed(metricType, speedIntervals[i].speedMetPerSeconds),
                            if (i < speedIntervals.size - 1)
                                calcSpeed(metricType, speedIntervals[i + 1].speedMetPerSeconds)
                            else metricType.maxSpeed,
                            speedIntervals[i].isEnableSound,
                            speedIntervals[i].intervalColor
                        )
                    )
                }
            }
        }
    }
}
