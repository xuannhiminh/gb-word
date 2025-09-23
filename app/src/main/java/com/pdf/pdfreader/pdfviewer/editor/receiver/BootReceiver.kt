package com.pdf.pdfreader.pdfviewer.editor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.ezteam.baseproject.utils.PreferencesUtils
import com.pdf.pdfreader.pdfviewer.editor.common.PresKey
import com.pdf.pdfreader.pdfviewer.editor.service.NotificationForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "BootReceiver triggered by: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && !PreferencesUtils.getBoolean(PresKey.GET_START, true)) {
            // Schedule notifications after the device has booted
            val serviceIntent = Intent(context, NotificationForegroundService::class.java)
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error starting NotificationForegroundService: ${e.message}")
            }
        }
    }
}
