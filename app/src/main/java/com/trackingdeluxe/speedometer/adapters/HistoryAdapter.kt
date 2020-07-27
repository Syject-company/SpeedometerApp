package com.trackingdeluxe.speedometer.adapters

import android.view.View
import android.widget.TextView
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.ui.base.BaseActivity

class HistoryAdapter(context: BaseActivity) : AppBaseAdapter<History>(context) {
    lateinit var onHistoryItemChanges: OnHistoryItemChanges

    override fun getLayoutId() = R.layout.item_history

    interface OnHistoryItemChanges {
        fun onHistoryRemoved(item: History)

        fun updateTotalMetrics()
    }

    override fun createHolder(inflate: View) =
        object : BaseItem<History>(inflate) {

            init {
                setIsRecyclable(false)
            }

            val deleteHistory = inflate.findViewById<View>(R.id.deleteHistoryItem)
            val rideDate = inflate.findViewById<TextView>(R.id.itemRideDate)
            val totalRideTime = inflate.findViewById<TextView>(R.id.itemTotalRideTimeValue)
            val maxSpeed = inflate.findViewById<TextView>(R.id.itemMaxSpeedValue)
            val avgSpeed = inflate.findViewById<TextView>(R.id.itemAvgValue)
            val distance = inflate.findViewById<TextView>(R.id.itemDistanceValue)

            override fun bind(t: History) {
                maxSpeed.text =
                    context.getSpeedText(metricType, "%.0f", t.maxSpeedInMetersPerSeconds)
                avgSpeed.text = context.getSpeedText(metricType, "%.1f", t.getAvgSpeed())
                deleteHistory.setOnClickListener {
                    onHistoryItemChanges.onHistoryRemoved(t)
                    removeItemByPosition(t, this)
                }
                distance.text = context.getShortDistanceText(metricType, t.distanceInMeters)
                totalRideTime.text = context.getRideTime(t.rideTimeInMilliseconds)
                rideDate.text = t.date
                onHistoryItemChanges.updateTotalMetrics()
            }
        }
}