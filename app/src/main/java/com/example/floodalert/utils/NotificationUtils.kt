package com.example.floodalert.utils

import android.content.Context

class NotificationUtils constructor(var mContext: Context) {
    val NOTIFICATION_ID = 200
    val PUSH_NOTIFICATION = "pushNotification"
    val CHANNEL_ID = "myChannel"
    val CHANNEL_NAME = "myChannelName"
    val ACTIVITY = "activity"
    val activityMap:HashMap<String,Class<Any>> = HashMap()

    init {
//        activityMap.put("MainActivity", MainActivity.class)
    }
}