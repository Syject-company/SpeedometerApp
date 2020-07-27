package com.trackingdeluxe.speedometer

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.trackingdeluxe.speedometer.data.DataManager
import com.trackingdeluxe.speedometer.data.db.database.AppDataBase
import com.trackingdeluxe.speedometer.data.db.DBHelperImpl
import com.trackingdeluxe.speedometer.data.prefs.PrefsHelperImpl

class SpeedometerApplication : Application() {
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate() {
        super.onCreate()
        DataManager.instance.init(DBHelperImpl(getRoomDataBase()), PrefsHelperImpl(getPrefs()))
        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(
                    "10E8FE4BC5FB47EF4496A35E85DAFEBD",
                    "ABCDEF012345",
                    "2D94208239D87881C922A8A27DD21FFE"))
                .build()
        )
    }

    private fun getRoomDataBase() =
        Room.databaseBuilder(applicationContext, AppDataBase::class.java, "database")
            .allowMainThreadQueries()
            .build()

    private fun getPrefs() =
        getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)

    /**
     *Load ad
     *@param interstitialAd interstitial Ad object
     *@param adRequest ad request
     */
    fun loadAd(interstitialAd: InterstitialAd, adRequest: AdRequest) {
        this.interstitialAd = interstitialAd
        AdRequest.Builder().build()
        this.interstitialAd?.loadAd(adRequest)
    }

    //Show ad
    fun showAd() {
        interstitialAd?.let {
            if (it.isLoaded) {
                it.show()
            }
        }
    }
}
