package com.example.mstperfumes.services

import android.util.Log
import com.example.mstperfumes.data.FcmNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging (FCM) Push Notification Receiver Service.
 * Handles incoming push messages for order status changes ("In Transit" and "Delivered").
 */
class FcmMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Refreshed FCM Device Token: $token")
        // Store or subscribe token to topic for order notifications
        FcmNotificationHelper.subscribeToOrderTopic()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "FCM Message Received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "MST Perfumes Notification"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Your order status has been updated."

        val orderCode = remoteMessage.data["orderCode"] ?: "MST-ORDER"
        val status = remoteMessage.data["status"] ?: "In Transit"

        // Post system notification to notification bar
        FcmNotificationHelper.displaySystemNotification(
            context = this,
            title = title,
            message = body,
            orderCode = orderCode,
            status = status
        )
    }

    companion object {
        private const val TAG = "FcmMessagingService"
    }
}
