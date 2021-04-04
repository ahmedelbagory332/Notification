package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationMediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.sendBroadcast( Intent("TRACKS_TRACKS")
            .putExtra("actionname", intent!!.action))
    }
}