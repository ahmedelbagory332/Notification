package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.RemoteInput


class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val remoteInput: Bundle = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput != null) {
            val replyText = remoteInput.getString("key_text_reply")
            val answer = Message(replyText!!, null)
            MainActivity.MESSAGES.add(answer)
            MainActivity.sendMessage(context!!)
        }
    }
}