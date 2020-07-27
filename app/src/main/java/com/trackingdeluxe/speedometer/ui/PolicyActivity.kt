package com.trackingdeluxe.speedometer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.trackingdeluxe.speedometer.R
import kotlinx.android.synthetic.main.content_policy.*

class PolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy)

        val link = findViewById<TextView>(R.id.pp_info)
        link.movementMethod = LinkMovementMethod.getInstance()

        button_privacy.setOnClickListener { button_view ->
            if (checkBox_privacy.isChecked) {
                val sharedPref: SharedPreferences = getSharedPreferences("app_settings", 0)
                val editor = sharedPref.edit()
                editor.putBoolean("first_time", false)
                editor.apply()

                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            } else {
                val snackbar = Snackbar.make(button_view.rootView, R.string.check_reminder,
                    Snackbar.LENGTH_LONG).setAction(" X ", null)
                snackbar.show()
            }
        }
    }
}
