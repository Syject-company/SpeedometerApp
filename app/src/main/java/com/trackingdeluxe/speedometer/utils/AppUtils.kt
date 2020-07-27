package com.trackingdeluxe.speedometer.utils

import java.lang.StringBuilder

class AppUtils {

    companion object {
        /**
         * calculate and build ride time
         * @param milliseconds - ride time in milliseconds
         * @param minutesText - text equivalent of minutes
         * @param hoursText - text equivalent of hours
         * @param dayText - text equivalent of day
         */
        fun getTimeFromMilliseconds(
            milliseconds: Long,
            minutesText: String,
            hoursText: String,
            dayText: String
        ): String {
            val minutes = (milliseconds / (1000 * 60) % 60).toInt()
            val hours = (milliseconds / (1000 * 60 * 60) % 24).toInt()
            val days = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
            val sb = StringBuilder()
            if (days > 0) {
                sb.append("$days$dayText ")
            }
            return sb.append("$hours$hoursText $minutes$minutesText").toString()
        }
    }
}