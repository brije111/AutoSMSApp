package com.example.autosmsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyAlarmReceiver : BroadcastReceiver() {

    companion object {
        val REQUEST_CODE = 12345
        val ACTION = "com.example.autosmsapp.alarm"
    }


    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, MyIntentService::class.java)
        i.putExtra("phone", intent.getStringExtra("phone"))
        i.putExtra("msg", intent.getStringExtra("msg"))
        context.startService(i)
    }
}
