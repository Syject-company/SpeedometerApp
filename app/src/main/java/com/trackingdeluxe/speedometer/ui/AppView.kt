package com.trackingdeluxe.speedometer.ui

interface AppView {

    /**
     *Show distance and speed on ui
     * @param speed user speed
     */
    fun updateSpeed(speed: Float)

    /**
     * Set max speed on gauge view according to result in miles or kilometers
     * @param maxSpeed max speed according to result in miles or kilometers
     * @param totalNick count of total nick according to result in miles or kilometers
     */
    fun updateGaugeView(maxSpeed: Float, totalNick: Int)

    /**
     * Set max speed on gauge view according to result in miles or kilometers
     * @param maxSpeed user max speed during ride
     * @param avgSpeed user avg speed during ride
     * @param distance user distance during ride
     */
    fun updateRideInfo(maxSpeed: Float, avgSpeed: Float, distance: Float)

    /**get user ride time
     * @return user ride time in milliseconds
     */
    fun getRideTime(): Long
}