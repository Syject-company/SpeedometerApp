package com.trackingdeluxe.speedometer.data.enums

import com.trackingdeluxe.speedometer.R
import java.io.Serializable

enum class MetricType constructor(
    val metricFullText: Int,
    val metricShortText: Int,
    val perHourText: Int,
    val maxSpeed: Float,
    val totalPreNick: Int,
    val boolValue: Boolean
) : Serializable {

    MILES(R.string.miles, R.string.miles_short, R.string.miles_per_hours, 120f, 140, true),
    KILOMETERS(R.string.kilometer,R.string.km_short,R.string.kilometer_per_hour,180f,210,false);

    companion object {
        fun getMetricTypeByBool(isMiles: Boolean) =
            if (isMiles) MILES
            else KILOMETERS
    }
}