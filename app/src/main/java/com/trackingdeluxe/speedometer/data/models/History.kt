package com.trackingdeluxe.speedometer.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity
class History : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var date: String = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())
    var speedSumInMeters: Float = 0f
    var speedChangesCount: Int = 0
    var maxSpeedInMetersPerSeconds: Float = 0f
    var distanceInMeters: Float = 0f
    var rideTimeInMilliseconds: Long = 0

    fun getAvgSpeed() =
        if (speedChangesCount == 0) 0f
        else speedSumInMeters / speedChangesCount
}