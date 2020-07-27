package com.trackingdeluxe.speedometer.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.ui.custom.RingView.CircleAngleAnimation


class RideInfoCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {
    private var ringView: RingView
    private var textView: TextView

    init {
        View.inflate(context, R.layout.custom_hud_item, this)
        ringView = findViewById(R.id.ringView)
        textView = findViewById(R.id.metricValue)
        textView.text = "0"
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RideInfoCircleView)
        if (attributes.getBoolean(R.styleable.RideInfoCircleView_isAnimatedRings, false)) {
            ringView.angle = 0f
            ringView.invalidate()
        }
        findViewById<TextView>(R.id.bottomText).text =
            attributes.getString(R.styleable.RideInfoCircleView_bottomText)
        attributes.recycle()
    }

    //start ring increase animation
    fun startRingAnimation() {
        ringView.startAnimation(CircleAngleAnimation(ringView).apply {
            duration = 5000
        })
    }

    /** set metric value in ring
     * @param value - text value of metric
     */
    fun setMetricValue(value: CharSequence) {
        textView.text = value
    }
}