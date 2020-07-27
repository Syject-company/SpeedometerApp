package com.trackingdeluxe.speedometer

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.ui.AppView
import com.trackingdeluxe.speedometer.utils.AppConstants

class MetricEngine : LocationListener, Runnable {
    private lateinit var appView: AppView
    lateinit var historyModel: History
    private var clearSpeedHandler: Handler = Handler()
    private var lastLocation: Location? = null
    var distanceInMeters: Float = 0f
    var speedInMetersPerSeconds: Float = 0f

    /**
     *Update metric values when receive user location
     *@param currentLocation current user location
     */
    private fun updateMetrics(currentLocation: Location) {
        stopClearSpeedHandler()
        runClearSpeedHandler()
        if (currentLocation.speed > 1f) {
            speedInMetersPerSeconds = currentLocation.speed
            lastLocation?.let { distanceInMeters += it.distanceTo(currentLocation) }
            lastLocation = currentLocation
            updateHistory()
        }
        updateMetricUI()
    }

    //Update metric ui
    private fun updateMetricUI() {
        appView.updateSpeed(speedInMetersPerSeconds)
        appView.updateRideInfo(
            historyModel.maxSpeedInMetersPerSeconds,
            historyModel.getAvgSpeed(),
            historyModel.distanceInMeters
        )
    }

    //Run zeroing speed handler
    private fun runClearSpeedHandler() {
        clearSpeedHandler.postDelayed(this, AppConstants.UPDATE_LOCATION_TIME * 5)
    }

    //Stop zeroing speed handler
    fun stopClearSpeedHandler() {
        clearSpeedHandler.removeCallbacksAndMessages(null)
    }

    /**
     *On location receiver listener
     *@param location current user location
     */
    override fun onLocationChanged(location: Location?) {
        location?.let { updateMetrics(it) }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {
    }

    /**
     *Attach application View
     *@param appView application view interface
     */
    fun attachView(appView: AppView) {
        this.appView = appView
    }

    //Zeroing user speed
    override fun run() {
        if (speedInMetersPerSeconds != 0f) {
            speedInMetersPerSeconds = 0f
            historyModel.speedSumInMeters++
            updateMetricUI()
        }
    }

    //update ride info according current metrics
    private fun updateHistory() {
        historyModel.distanceInMeters = distanceInMeters
        historyModel.speedChangesCount += 1
        historyModel.speedSumInMeters += speedInMetersPerSeconds
        if (historyModel.maxSpeedInMetersPerSeconds < speedInMetersPerSeconds) {
            historyModel.maxSpeedInMetersPerSeconds = speedInMetersPerSeconds
        }
    }

    //getRide info to save in db
    fun getRideHistory(): History {
        historyModel.rideTimeInMilliseconds = appView.getRideTime()
        return historyModel
    }
}