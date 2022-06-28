package com.candy.ytgecko

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class MediaEdit : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            ACTION_STOP -> {
                mediaStore.stop()
            }
            ACTION_PAUSE -> {
                mediaStore.pause()
            }
            ACTION_PREVIOUS -> {
                mediaStore.previousTrack()
            }
            ACTION_RESUME -> {
                mediaStore.play()
            }
            ACTION_NEXT -> {
                mediaStore.nextTrack()
            }
            ACTION_OPEN -> {
                val newintent = Intent(context, MainActivity::class.java)
                newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                newintent.action = Intent.ACTION_MAIN
                newintent.addCategory(Intent.CATEGORY_LAUNCHER)
                ContextCompat.startActivity(context, newintent, null)
                val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context.sendBroadcast(it)
            }
        }
    }
}