package com.trackingdeluxe.speedometer.adapters

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.data.models.SpeedInterval
import com.trackingdeluxe.speedometer.ui.base.BaseActivity
import java.util.*

class SpeedIntervalAdapter(context: BaseActivity) : AppBaseAdapter<SpeedInterval>(context) {
    lateinit var onSpeedLimitItemChanges: OnSpeedLimitItemChanges

    override fun getLayoutId() = R.layout.item_speed_interval

    fun changeShowResultInMiles(metricType: MetricType) {
        this.metricType = metricType
        notifyDataSetChanged()
    }

    interface OnSpeedLimitItemChanges {
        fun onSpeedIntervalRemoved(item: SpeedInterval)

        fun onSpeedIntervalUpdated(item: SpeedInterval)
    }

    override fun addItem(item: SpeedInterval) {
        items.add(item)
        items.sortWith(Comparator { o1, o2 -> o1.speedMetPerSeconds.compareTo(o2.speedMetPerSeconds) })
        notifyDataSetChanged()
    }

    override fun createHolder(inflate: View) =
        object : BaseItem<SpeedInterval>(inflate) {
            init {
                setIsRecyclable(false)
            }

            private val deleteSpeedLimit = itemView.findViewById<ImageView>(R.id.deleteSpeedLimit)
            private val soundControl = itemView.findViewById<ImageView>(R.id.soundControl)
            private val speedLimitNumber = itemView.findViewById<TextView>(R.id.speedLimitNumber)
            private val speedLimit = itemView.findViewById<TextView>(R.id.speedInterval)

            @SuppressLint("SetTextI18n")
            override fun bind(t: SpeedInterval) {
                applyItemColor(t.intervalColor)
                speedLimit.text = context.getSpeedText(metricType, "%.0f", t.speedMetPerSeconds)
                speedLimitNumber.text =
                    "${getString(R.string.speed_limit_position)}${adapterPosition + 1}"
                deleteSpeedLimit.setOnClickListener {
                    onSpeedLimitItemChanges.onSpeedIntervalRemoved(t)
                    removeItemByPosition(t, this)
                }
                soundControl.setImageResource(getSoundButtonImage(t.isEnableSound))
                soundControl.setOnClickListener {
                    t.isEnableSound = !t.isEnableSound
                    onSpeedLimitItemChanges.onSpeedIntervalUpdated(t)
                    soundControl.setImageResource(getSoundButtonImage(t.isEnableSound))
                }
                itemView.invalidate()
            }

            private fun applyItemColor(intervalColor: Int) {
                deleteSpeedLimit.setColorFilter(intervalColor)
                soundControl.setColorFilter(intervalColor)
                speedLimitNumber.setTextColor(intervalColor)
                speedLimit.setTextColor(intervalColor)
            }

            fun getSoundButtonImage(isEnableSound: Boolean) =
                if (isEnableSound) R.drawable.ic_sound_enabled
                else R.drawable.ic_sound_disable
        }
}