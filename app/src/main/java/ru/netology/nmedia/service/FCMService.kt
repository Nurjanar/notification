package ru.netology.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MessageData

class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(message: RemoteMessage) {
        val contentJson = message.data["content"]
        if (contentJson != null) {
            val messageData = gson.fromJson(contentJson, MessageData::class.java)
            val recipientId = messageData.recipientId
            val appAuth = AppAuth.getInstance()
            val currentUserId = appAuth.authStateFlow.value.id

            when {
                recipientId == currentUserId -> {
                    showNotification(messageData.content)
                }

                recipientId == 0L -> {
                    appAuth.sendPushToken()
                }

                recipientId != null && recipientId != currentUserId -> {
                    appAuth.sendPushToken()
                }

                recipientId == null -> {
                    showNotification(messageData.content)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(content: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Новое уведомление")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }
}
