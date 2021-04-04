package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val msg:String = intent!!.getStringExtra("toastMessage").toString()
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show()
    }
}