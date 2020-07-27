package com.trackingdeluxe.speedometer.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.core.content.ContextCompat
import com.trackingdeluxe.speedometer.R


class RingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    private val startAnglePoint = 130f
    private val strokeWidth = 5f
    private var paint: Paint = Paint()
    private var rectF = RectF()
    var angle: Float = 280f

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = ContextCompat.getColor(context, R.color.mainGreenColor)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(rectF, startAnglePoint, angle, false, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        rectF.set(
            strokeWidth / 2f,
            strokeWidth / 2f,
            width - strokeWidth / 2f,
            height - strokeWidth / 2f
        )
    }

    internal class CircleAngleAnimation(private val ring: RingView) :
        Animation() {

        override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val angle = ring.angle + (280 - ring.angle) * interpolatedTime
            ring.angle = angle
            ring.requestLayout()
        }
    }
}