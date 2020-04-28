package com.zaidan.removebg

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import net.khirr.android.privacypolicy.PrivacyPolicyDialog

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        privacyPolicy()
    }

    private fun privacyPolicy() {
        val privacy = PrivacyPolicyDialog(this,
            "https://localhost/terms",
            "https://localhost/privacy")

        privacy.addPoliceLine("This application uses a unique user identifier for advertising purposes, it is shared with third-party companies.")
        privacy.addPoliceLine("This application sends error reports, installation and send it to a server of the Fabric.io company to analyze and process it.")
        privacy.addPoliceLine("This application requires internet access and must collect the following information: Installed applications and history of installed applications, ip address, unique installation id, token to send notifications, version of the application, time zone and information about the language of the device.")
        privacy.addPoliceLine("All details about the use of data are available in our Privacy Policies, as well as all Terms of Service links below.")

        privacy.onClickListener = object:PrivacyPolicyDialog.OnClickListener{
            override fun onAccept(isFirstTime: Boolean) {
                Log.e("MainActivity", "Policies accepted")
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }

            override fun onCancel() {
                Log.e("MainActivity", "Policies accepted")
                finish()
            }
        }
        privacy.title = "Terms of Service"
        privacy.acceptButtonColor = ContextCompat.getColor(this, R.color.colorAccepted)
        privacy.show()
    }
}