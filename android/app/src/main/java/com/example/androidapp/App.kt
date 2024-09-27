package com.example.androidapp

import ai.perso.live.PersoLiveInitializer
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class App : Application() {

    companion object {
        const val channelId = "perso_live_channel"
        const val channelName = "Perso Live"

        private lateinit var instance: App
        fun getContext(): Context = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val notiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notiManager.getNotificationChannel(channelId) == null) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.setShowBadge(false)
            notiManager.createNotificationChannel(notificationChannel)
        }

        PersoLiveInitializer.init(this)
    }

}