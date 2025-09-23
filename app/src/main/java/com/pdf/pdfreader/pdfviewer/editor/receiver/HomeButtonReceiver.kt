package com.pdf.pdfreader.pdfviewer.editor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pdf.pdfreader.pdfviewer.editor.notification.NotificationManager

class HomeButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == intent.action) {
            val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
            if (reason != null && reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                try {
                    NotificationManager(context).showCallUseAppNotification()
                } catch (e: Exception) {
                    Log.e("HomeButtonReceiver", "Error showing call use app notification: ${e.message}")
                    // Optionally log the error or handle it as needed
                }
            }
        }
    }

    companion object {
        private const val SYSTEM_DIALOG_REASON_KEY = "reason"
        private const val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
    }
}
