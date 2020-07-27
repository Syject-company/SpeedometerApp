package com.trackingdeluxe.speedometer.data.db

import com.trackingdeluxe.speedometer.data.db.dao.HistoryDao
import com.trackingdeluxe.speedometer.data.db.dao.SpeedIntervalDao

interface DBHelper : HistoryDao, SpeedIntervalDao