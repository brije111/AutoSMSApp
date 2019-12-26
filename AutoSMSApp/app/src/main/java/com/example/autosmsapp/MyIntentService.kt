package com.example.autosmsapp

import android.app.IntentService
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MyIntentService: IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val phone = intent!!.getStringExtra("phone")
        val msg = intent.getStringExtra("msg")

        Log.i("MyIntentService", "Service is running $phone $msg")
        Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show()
        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phone, null, msg, null, null)
        Toast.makeText(
            applicationContext, "SMS sent.",
            Toast.LENGTH_LONG
        ).show()
    }

}