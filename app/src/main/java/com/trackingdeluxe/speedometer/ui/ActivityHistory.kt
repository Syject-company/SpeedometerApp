package com.trackingdeluxe.speedometer.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.adapters.HistoryAdapter
import com.trackingdeluxe.speedometer.data.DataManager
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_history.*

class ActivityHistory : BaseActivity(), View.OnClickListener,
    HistoryAdapter.OnHistoryItemChanges {
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var metricType: MetricType

    override fun getLayoutID() = R.layout.activity_history

    override fun isPortraitOrientation() = true

    override fun init(savedInstanceState: Bundle?) {
        initValues()
        initViews()
        initButtonListener()
        loadHistory()
    }

    //init button listener
    private fun initButtonListener() {
        clearHistoryButton.setOnClickListener(this)
    }

    /*init activity values
    get metric type if user selected in settings
    if user not select before - get default value from resource
    */
    private fun initValues() {
        metricType = MetricType.getMetricTypeByBool(
            DataManager.instance.isShowResultInMiles() ?: resources.getBoolean(R.bool.showInMiles)
        )
    }

    //init activity views
    //show empty total info
    private fun initViews() {
        showTotalMetricValues(null)
        initHistoryAdapter()
    }

    //init history adapter
    private fun initHistoryAdapter() {
        historyAdapter = HistoryAdapter(this)
        historyAdapter.onHistoryItemChanges = this
        historyAdapter.metricType = metricType
    }
    //load and show history from database
    private fun loadHistory() {
        Thread {
            val historyList = DataManager.instance.getAllHistory()
            runOnUiThread {
                totalAVGSpeedView.startRingAnimation()
                totalMaxSpeedView.startRingAnimation()
                showTotalMetricValues(calcTotalMetricValues(historyList))
                showHistory(historyList)
            }
        }.start()
    }

    /**calculate total ride metrics from all history items
     * @param historyItemsList - collection of history items
    */
    private fun calcTotalMetricValues(historyItemsList: List<History>): TotalMetricValues {
        val totalMetricValues = TotalMetricValues()
        historyItemsList.forEach { historyModel ->
            if (historyModel.maxSpeedInMetersPerSeconds > totalMetricValues.maxTotalSpeed) {
                totalMetricValues.maxTotalSpeed = historyModel.maxSpeedInMetersPerSeconds
            }
            totalMetricValues.avgTotalSpeed =
                if (totalMetricValues.avgTotalSpeed == 0f) historyModel.getAvgSpeed()
                else (totalMetricValues.avgTotalSpeed + historyModel.getAvgSpeed()) / 2
            totalMetricValues.totalRideTime += historyModel.rideTimeInMilliseconds
            totalMetricValues.totalDistance += historyModel.distanceInMeters
        }
        return totalMetricValues
    }

    /**show total ride metrics from all history items
     * @param totalMetricValues - calculated total ride history, can bee null if history collection is null
     */
    private fun showTotalMetricValues(totalMetricValues: TotalMetricValues?) {
        totalMaxSpeedView.setMetricValue(
            getSpeedText(metricType, "%.0f", totalMetricValues?.maxTotalSpeed)
        )
        totalAVGSpeedView.setMetricValue(
            getSpeedText(metricType, "%.1f", totalMetricValues?.avgTotalSpeed)
        )
        totalRideTimeView.text = getRideTime(totalMetricValues?.totalRideTime)
        totalDistanceView.text = getShortDistanceText(metricType, totalMetricValues?.totalDistance)
    }

    /**show history items on ui
     * @param historyItemsList - collections of history items from db
     */
    private fun showHistory(historyItemsList: List<History>) {
        historyAdapter.addItems(historyItemsList.toList())
        historyView.layoutManager = object : LinearLayoutManager(this) {
            override fun isAutoMeasureEnabled() = true
        }
        historyView.adapter = historyAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clearHistoryButton -> {
                DataManager.instance.removeAllHistory()
                historyAdapter.clearItems()
                showTotalMetricValues(null)
            }
        }
    }

    inner class TotalMetricValues {
        var totalRideTime: Long = 0
        var maxTotalSpeed = 0f
        var avgTotalSpeed = 0f
        var totalDistance = 0f
    }

    /**trigger ro delete history item from database if user press delete item in recyclerView holder
     * @param item - history model to delete from database
      */
    override fun onHistoryRemoved(item: History) {
        DataManager.instance.deleteHistoryModel(item)
    }

    //trigger update total ride info if history item was removed
    override fun updateTotalMetrics() {
        showTotalMetricValues(calcTotalMetricValues(historyAdapter.items))
    }
}