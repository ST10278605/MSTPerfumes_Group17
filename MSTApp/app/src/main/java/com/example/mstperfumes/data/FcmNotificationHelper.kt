package com.example.mstperfumes.data

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.mstperfumes.R
import com.example.mstperfumes.TrackParcelActivity
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Helper class for Firebase Cloud Messaging (FCM) Order Push Notifications.
 * Handles Notification Channel creation, Android 13+ permissions, FCM topic subscription,
 * and posting push notifications when order status changes to "In Transit" or "Delivered".
 */
object FcmNotificationHelper {

    private const val TAG = "FcmNotificationHelper"
    private const val CHANNEL_ID = "mst_order_updates"
    private const val CHANNEL_NAME = "MST Order Updates"
    private const val CHANNEL_DESC = "Notifications for perfume order dispatches and parcel deliveries"
    private const val NOTIFICATION_PERMISSION_REQUEST = 1001

    /**
     * Initializes FCM Notification Channel and subscribes to order topic.
     */
    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i(TAG, "FCM Notification Channel created successfully.")
        }
        subscribeToOrderTopic()
    }

    /**
     * Requests POST_NOTIFICATIONS permission on Android 13+ (API 33+).
     */
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST
                )
            }
        }
    }

    /**
     * Subscribes device to FCM topic for broadcast order status updates.
     */
    fun subscribeToOrderTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("mst_orders")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "Subscribed to FCM topic: mst_orders")
                    } else {
                        Log.w(TAG, "Failed to subscribe to FCM topic", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "FCM messaging not initialized yet", e)
        }
    }

    /**
     * Triggers a Push Notification when order status changes to "In Transit" or "Delivered".
     */
    fun triggerOrderStatusNotification(context: Context, orderCode: String, newStatus: String) {
        val title: String
        val body: String

        when (newStatus.lowercase()) {
            "in transit" -> {
                title = "🚚 Order Dispatched! ($orderCode)"
                body = "Great news! Your perfume parcel $orderCode is In Transit. Tap to track courier live!"
            }
            "delivered" -> {
                title = "🎉 Order Delivered! ($orderCode)"
                body = "Your luxury perfume package $orderCode has been delivered. Enjoy your fragrance!"
            }
            else -> return // Only notify for In Transit or Delivered
        }

        displaySystemNotification(context, title, body, orderCode, newStatus)
    }

    /**
     * Posts notification to the device system notification bar.
     */
    fun displaySystemNotification(
        context: Context,
        title: String,
        message: String,
        orderCode: String,
        status: String
    ) {
        initNotificationChannel(context)

        // Intent to open TrackParcelActivity when user taps notification
        val intent = Intent(context, TrackParcelActivity::class.java).apply {
            putExtra("orderCode", orderCode)
            putExtra("status", status)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            orderCode.hashCode(),
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (System.currentTimeMillis() % 10000).toInt() + orderCode.hashCode()
        
        try {
            notificationManager.notify(notificationId, notification)
            Log.i(TAG, "Push Notification posted for $orderCode [$status]")
        } catch (e: Exception) {
            Log.e(TAG, "Error posting notification", e)
        }
    }
}
