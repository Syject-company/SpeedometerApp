package com.trackingdeluxe.speedometer.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trackingdeluxe.speedometer.data.db.dao.HistoryDao
import com.trackingdeluxe.speedometer.data.db.dao.SpeedIntervalDao
import com.trackingdeluxe.speedometer.data.models.History
import com.trackingdeluxe.speedometer.data.models.SpeedInterval

@Database(entities = [History::class, SpeedInterval::class], version = 1)
abstract class AppDataBase : RoomDatabase() {

    //get history dao
    abstract fun historyDao(): HistoryDao

    //get speed interval dao
    abstract fun speedIntervalDao(): SpeedIntervalDao
}