package com.candy.ytgecko

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat


private const val CHANNEL_ID = "youtube_sound"
private const val CHANNEL_NAME = "YouTube"
private const val NOTIFICATION_ID= 10012
const val ACTION_NEXT = "next"
const val ACTION_STOP = "stop"
const val ACTION_PAUSE = "pause"
const val ACTION_RESUME = "resume"
const val ACTION_PREVIOUS = "previous"
const val ACTION_OPEN = "open"

fun hideNotification(context: Context){
    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    nManager.cancel(NOTIFICATION_ID)
}

fun notificationBuilder(context: Context, x: Int) {

    val stopIntent = Intent(context, MediaEdit::class.java)
    stopIntent.action = ACTION_STOP
    val stopPendingIntent = PendingIntent
        .getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val nextIntent = Intent(context, MediaEdit::class.java)
    nextIntent.action = ACTION_NEXT
    val nextPendingIntent = PendingIntent
        .getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val pauseIntent = Intent(context, MediaEdit::class.java)
    pauseIntent.action = ACTION_PAUSE
    val pausePendingIntent = PendingIntent
        .getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val resumeIntent = Intent(context, MediaEdit::class.java)
    resumeIntent.action = ACTION_RESUME
    val resumePendingIntent = PendingIntent
        .getBroadcast(context, 0, resumeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val previousIntent = Intent(context, MediaEdit::class.java)
    previousIntent.action = ACTION_PREVIOUS
    val previousPendingIntent = PendingIntent
        .getBroadcast(context, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val openIntent = Intent(context, MediaEdit::class.java)
    openIntent.action = ACTION_OPEN
    val openPendingIntent = PendingIntent
        .getBroadcast(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)


    val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val mediaSession = MediaSessionCompat(context, CHANNEL_NAME)
    mediaSession.setMetadata(
        MediaMetadataCompat.Builder()
            .build()
    )

    val notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lol)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ytimg))
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setChannelId(CHANNEL_ID)
            .setContentTitle(mediaTitle)
            .setContentText(mediaArtist)
            .addAction(R.drawable.ic_action_previous, "Previous",previousPendingIntent)
            .addAction(R.drawable.ic_pause, "Pause",pausePendingIntent)
            .addAction(R.drawable.ic_play_arrow, "Resume",resumePendingIntent)
            .addAction(R.drawable.ic_next, "Next",nextPendingIntent)
            .addAction(R.drawable.ic_stop, "Stop",stopPendingIntent)
            .setContentIntent(openPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0,x,3)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setColor(Color.rgb(128,28,28))
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)

    nManager.createNotificationChannel(CHANNEL_ID, CHANNEL_NAME, true)
    nManager.notify(NOTIFICATION_ID, notification.build())
}

private fun NotificationManager.createNotificationChannel(channelId: String, channelName: String, playSound: Boolean) {
    val channelImportance = if(playSound) NotificationManager.IMPORTANCE_DEFAULT
    else NotificationManager.IMPORTANCE_LOW
    val nChannel = NotificationChannel(channelId, channelName, channelImportance)
    nChannel.enableLights(true)
    nChannel.lightColor = Color.BLUE
    this.createNotificationChannel(nChannel)
}
