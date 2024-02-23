package son.example.recordscreendemo

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationManager(
    private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "android.playground.media_projection.notification.channel.id"
        private const val AUDIO_RECORD_PERMISSION_NOTIFICATION_ID = 1001
    }

    fun createNotificationChannel() {
        val notificationChannel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        ).setName(
            "android.playground.media_projection.notification.channel.name"
        ).setVibrationEnabled(true)
            .build()
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(notificationChannel)
    }

    fun getForegroundServiceNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_camera)
            .setContentTitle("Android Playground")
            .setContentText("Screen recording...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setShowWhen(true)
            .build()
    }

    fun showAudioPermissionRequiredNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        val actionIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context,
            1,
            actionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        val notification = NotificationCompat.Builder(
            context,
            "android.playground.media_projection.notification.channel.id"
        )
            .setSmallIcon(R.drawable.ic_camera)
            .setContentTitle("Android Playground")
            .setContentText("Audio permission is required. Please enable it in system settings or reopen the app again and enable permission before starting recording.")
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .setStyle(NotificationCompat.BigTextStyle())
            .setShowWhen(true)
            .addAction(0, "Settings", actionPendingIntent)
            .build()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(AUDIO_RECORD_PERMISSION_NOTIFICATION_ID, notification)
    }
}
