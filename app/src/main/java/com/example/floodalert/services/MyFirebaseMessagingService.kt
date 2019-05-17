package com.example.floodalert.services

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.floodalert.MainActivity
import com.example.floodalert.Utils.NotificationUtil
import com.example.floodalert.ViewActivity
import com.example.floodalert.models.NotificationVO
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    val TAG = "MyFirebaseIdService"
    val TOPIC_GLOBAL = "global"
    val TITTLE = "title"
    val EMPTY = ""
    val MESSAGE = "message"
    val ACTION = "action"
    val DATA = "data"
    val ACTION_DESTINATION = "action_destination"

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage?.from}")

        // Check if message contains a data payload.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            var data = remoteMessage.data
            handleData(data)

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob()
            } else {
                // Handle message within 10 seconds
//                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage?.notification?.let {
            Log.d(TAG, "Message NotificationVO Body: ${it.body}")
            var notification = remoteMessage.notification
            handleNotification(notification!!)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        val builder = StringBuilder()
        builder.append("Refreshed Token: ")
            .append(token)
        Log.d(TAG, builder.toString())

//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
//            .addOnCompleteListener { task ->
//                var msg = "Succesful subscribed to " + TOPIC_GLOBAL
//                if (!task.isSuccessful) {
//                    msg = "Failed to subscribe to " + TOPIC_GLOBAL
//                }
//                Log.d(TAG, msg)
//                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//            }
    }

    fun handleNotification(RemoteMsgNotification: RemoteMessage.Notification){
        var message: String = RemoteMsgNotification.body.toString()
        var title: String = RemoteMsgNotification.title.toString()
        var notificationVO = NotificationVO(title,message,"","")

        var resultIntent = Intent(applicationContext, ViewActivity::class.java)
        var notificationUtil = NotificationUtil(applicationContext)
        resultIntent.putExtra("message",message)
        notificationUtil.displayNotification(notificationVO, resultIntent)
        notificationUtil.playNotificationSound()
    }

    private fun handleData(data: Map<String, String>){
        var title: String = data.get(TITTLE).toString()
        var message: String = data.get(MESSAGE).toString()
        var action: String = data.get(ACTION).toString()
        var actionDestination: String = data.get(ACTION_DESTINATION).toString()
        var notificationVO = NotificationVO(title,message,action,actionDestination)

        var resultIntent = Intent(applicationContext, ViewActivity::class.java)
        resultIntent.putExtra("message",message)

        var notificationUtil = NotificationUtil(applicationContext)
        notificationUtil.displayNotification(notificationVO, resultIntent)
        notificationUtil.playNotificationSound()
    }
}
