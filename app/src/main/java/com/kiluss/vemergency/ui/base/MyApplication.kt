package com.kiluss.vemergency.ui.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kiluss.vemergency.constant.CHANEL_ID
import com.kiluss.vemergency.utils.SharedPrefManager

/**
 * Created by sonlv on 11/4/2022
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedPrefManager.init(this)
        makeNotificationChannel(CHANEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
    }

    private fun makeNotificationChannel(id: String?, name: String?, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance)
            channel.setShowBadge(true) // set false to disable badges, Oreo exclusive
            val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.createNotificationChannel(channel)
        }
    }
}