package com.application.personal_budget_app.data.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.application.personal_budget_app.R

object ReminderNotifier {
    private const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 1

    @SuppressLint("MissingPermission") // vérifiée juste au-dessus
    fun show(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        createChannel(context)
        // Ouvre l'app sur son écran de départ (verrouillage compris).
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        val tap = PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_calendar)
            .setContentTitle("Ta journée n'est pas encore renseignée")
            .setContentText("Une dépense, ou « aucune dépense » : quelques secondes suffisent.")
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    /** Sans effet si le canal existe déjà. L'utilisateur peut le couper dans les réglages Android. */
    private fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Rappel quotidien", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Un rappel le soir si ta journée n'est pas renseignée"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
