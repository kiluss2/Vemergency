package com.kiluss.vemergency.ui.base

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.kiluss.vemergency.constant.CHANEL_ID

/**
 * Created by sonlv on 11/4/2022
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        makeNotificationChannel(CHANEL_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT)
        Fire.init("YOUR_SERVER_KEY_HERE")
    }

    fun makeNotificationChannel(id: String?, name: String?, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance)
            channel.setShowBadge(true) // set false to disable badges, Oreo exclusive
            val notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            notificationManager.createNotificationChannel(channel)
        }
    }
}