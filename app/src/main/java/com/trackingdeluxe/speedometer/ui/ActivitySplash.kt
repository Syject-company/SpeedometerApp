package com.trackingdeluxe.speedometer.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.messaging.FirebaseMessaging
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.ui.base.BaseActivity
import com.trackingdeluxe.speedometer.utils.AppConstants
import com.trackingdeluxe.speedometer.utils.PermissionUtils
import java.util.*

class ActivitySplash : BaseActivity(), PurchasesUpdatedListener {

    var isProVersion = false
    private var billingClient: BillingClient? = null
    var alreadyOpened = false

    override fun init(savedInstanceState: Bundle?) {
        val sp = getSharedPreferences("my_prefs", Activity.MODE_PRIVATE)
        isProVersion = sp.getBoolean("concierge", false)
        updateProStatus()
        val sharedPref: SharedPreferences = getSharedPreferences("app_settings", 0)
        if (sharedPref.getBoolean("first_time", true)) {
            // App was opened for the first time
            startActivity(Intent(this, PolicyActivity::class.java))
            finish()
        } else {
            permissionUtils.askPermission(this,
                object : PermissionUtils.OnPermissionResult {
                    override fun permissionResult(grantedPermissions: Array<out String>) {
                        if (!isProVersion) {
                            loadAD()
                        } else {
                            startAppActivity()
                        }
                    }
                })
        }
    }

    override fun isPortraitOrientation() = true

    private fun loadAD() {
        val interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.adUnitId)
        interstitialAd.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                startAppActivity()
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                startAppActivity()
            }
        }
        getSpeedometerApplication().loadAd(interstitialAd, AdRequest.Builder().build())

        // Adding manual Admob timeout after 8 + 2 seconds
        val timer = object: CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if(!alreadyOpened) {
                    startAppActivity()
                }
            }
        }
        timer.start()
    }

    override fun getLayoutID() = R.layout.splash_activity

    fun startAppActivity() {
        if(!alreadyOpened) {
            alreadyOpened = true
            Handler().postDelayed({
                startActivity(Intent(this, ActivityApp::class.java))
                finish()
            }, 2000)
        }

    }

    private fun updateProStatus() {
        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchasesResult: Purchase.PurchasesResult =
                        billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)
                    val result = purchasesResult.purchasesList
                    var isPro = false
                    if (result != null) {
                        for (purchase in result) {
                            val sku = purchase.sku
                            if (AppConstants.ABO_YEARLY == sku || AppConstants.ABO_MONTHLY == sku) {
                                isPro = true
                            }
                        }
                    }
                    val sp = getSharedPreferences("my_prefs", Activity.MODE_PRIVATE)
                    sp.edit().putBoolean("concierge", isPro).apply()

                    if (isPro) {
                        FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic("non-purchasers")
                        FirebaseMessaging.getInstance()
                            .unsubscribeFromTopic("non-purchasers-german")
                    } else {
                        if (Locale.getDefault().language == "de") {
                            FirebaseMessaging.getInstance()
                                .subscribeToTopic("non-purchasers-german")
                        } else {
                            FirebaseMessaging.getInstance()
                                .subscribeToTopic("non-purchasers")
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() { // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

    override fun onPurchasesUpdated(p0: BillingResult?, p1: MutableList<Purchase>?) {

    }
}