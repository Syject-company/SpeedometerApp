package com.trackingdeluxe.speedometer.utils

import com.trackingdeluxe.speedometer.data.enums.MetricType

class MetricUtils private constructor(private val metricType: MetricType) {

    companion object {
        private const val KM_PER_HOUR_MULTIPLY = 3.6
        private const val MP_PER_HOUR_MULTIPLY = 2.237237
        private const val METERS_IN_MILES = 1609
        private const val METERS_IN_KILOMETERS = 1000

        /**set metric type
         * @param metricType - set metric type to provide calculations
         * @return MetricUtils according metric type
         */
        fun byType(metricType: MetricType) = MetricUtils(metricType)
    }

    /** calculate speed according metric type from meters per seconds
     * @param speedInMetersPerSeconds  - speed in meter per seconds
     */
    fun calcSpeedFromMetersPerSecond(speedInMetersPerSeconds: Float) =
        if (metricType == MetricType.KILOMETERS) calcSpeedToKilometerPerHour(speedInMetersPerSeconds)
        else calcSpeedToMilesPerHour(speedInMetersPerSeconds)

    /** calculate distance according metric type from meters
     * @param distanceInMeters  - distance in meters
     */
    fun calcDistance(distanceInMeters: Float) =
        if (metricType == MetricType.KILOMETERS) calcDistanceInKilometersByMeters(distanceInMeters)
        else calcDistanceInMilesByMeters(distanceInMeters)

    /** calculate speed to meter per second from speed according speed by metricType
     * @param speed  - speed according metric type
     */
    fun calcSpeedToMeterPerSeconds(speed: Float) =
        if (metricType == MetricType.KILOMETERS) calcSpeedToMeterPerSecondsFromKmPerHour(speed)
        else calcSpeedToMeterPerSecondsFromMilesPerHour(speed)

    /** calculate speed to  miles per hour fom
     * @param speedInMetersPerSeconds  - speed in meter per seconds
     */
    private fun calcSpeedToMilesPerHour(speedInMetersPerSeconds: Float) =
        (speedInMetersPerSeconds * MP_PER_HOUR_MULTIPLY).toFloat()

    /** calculate speed to  kilometers per hour fom
     * @param speedInMetersPerSeconds  - speed in meter per seconds
     */
    private fun calcSpeedToKilometerPerHour(speedInMetersPerSeconds: Float) =
        (speedInMetersPerSeconds * KM_PER_HOUR_MULTIPLY).toFloat()

    /** calculate speed to meter per seconds from kilometers per hour
     * @param speedInKMPerHour  - speed in kilometer per hour
     */
    private fun calcSpeedToMeterPerSecondsFromKmPerHour(speedInKMPerHour: Float) =
        (speedInKMPerHour / KM_PER_HOUR_MULTIPLY).toFloat()

    /** calculate speed to meter per seconds from miles per hour
     * @param speedInMpPerHour  - speed in miles per hour
     */
    private fun calcSpeedToMeterPerSecondsFromMilesPerHour(speedInMpPerHour: Float) =
        (speedInMpPerHour / MP_PER_HOUR_MULTIPLY).toFloat()

    /** calculate distance from meter to miles
     * @param distanceInMeters  - user ride speed in meters
     */
    private fun calcDistanceInMilesByMeters(distanceInMeters: Float) =
        distanceInMeters / METERS_IN_MILES

    /** calculate distance from meter to kilometer
     * @param distanceInMeters  - user ride speed in meters
     */
    private fun calcDistanceInKilometersByMeters(distanceInMeters: Float) =
        distanceInMeters / METERS_IN_KILOMETERS
}
