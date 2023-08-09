package com.videoengager.demoapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Random
import java.util.UUID

class PushNotificationsReceiverService : FirebaseMessagingService() {
    val CHANNEL_ID = "14bafbfc-22f8-11ee-be56-0242ac120002"

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("PushNotifications", message.data.toString())
        if(message.notification!=null && message.data.isNotEmpty() && message.data.containsKey("veurl")) {
            //send custom notification when App is on foreground
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0,
                Intent(this, VEActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    action = Intent.ACTION_VIEW
                    data = Uri.parse(message.data.get("veurl"))
                }, PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message.notification?.title)
                .setContentText(message.notification?.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            createNotificationChannel(CHANNEL_ID)
            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                    notify(Random().nextInt(), builder.build())
                }
            }
        }
    }

    private fun createNotificationChannel(channelID : String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, "VideoCall channel", importance).apply {
                description = ""
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = applicationContext.getSystemService( Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}

