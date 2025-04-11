package com.example.advancedlandslideapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.widget.RemoteViews
import com.example.advancedlandslideapp.R

const val CHANNEL_ID = "alert_channel"
const val NOTIFICATION_ID = 101

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showAlertNotification(context: Context, alertText: String) {
    // Use a dummy intent since we don't want to launch an activity.
    val dummyIntent = Intent()
    val pendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 0, dummyIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val collapsedView = RemoteViews(context.packageName, R.layout.notification_collapsed)
    // Inflate your custom notification layout using RemoteViews.
    val expandedView = RemoteViews(context.packageName, R.layout.notification_expanded)
    // Dynamically set the alert message.
    expandedView.setTextViewText(R.id.notification_text, alertText)

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setCustomContentView(collapsedView)
        .setCustomBigContentView(expandedView)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return@with
        }
        notify(NOTIFICATION_ID, builder.build())
    }
}