package com.trackingdeluxe.speedometer.data.models

import java.io.Serializable

class SpeedRange constructor(
    val from: Float,
    var to: Float,
    val isEnableSound: Boolean,
    val rangeColor: Int
) : Serializable