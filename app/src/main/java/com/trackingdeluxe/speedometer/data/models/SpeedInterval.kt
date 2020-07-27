package com.trackingdeluxe.speedometer.data.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SpeedInterval() : Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var speedMetPerSeconds: Float = 0f
    var intervalColor: Int = 0
    var isEnableSound = true

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        speedMetPerSeconds = parcel.readFloat()
        intervalColor = parcel.readInt()
        isEnableSound = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeFloat(speedMetPerSeconds)
        dest.writeInt(intervalColor)
        dest.writeByte(
            if (isEnableSound) 1
            else 0
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpeedInterval> {
        override fun createFromParcel(parcel: Parcel): SpeedInterval {
            return SpeedInterval(parcel)
        }

        override fun newArray(size: Int): Array<SpeedInterval?> {
            return arrayOfNulls(size)
        }
    }
}
