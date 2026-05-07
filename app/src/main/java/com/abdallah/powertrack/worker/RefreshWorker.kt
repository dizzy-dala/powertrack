package com.abdallah.powertrack.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import java.util.Locale

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RefreshWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = context.getSharedPreferences("powertrack", Context.MODE_PRIVATE)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        
        val units = prefs.getFloat("remaining_units", 0f)
        val daily = prefs.getFloat("daily_usage", 1.0f)
        
        val subtraction = daily / 4f
        val newUnits = (units - subtraction).coerceAtLeast(0f)
        
        // Update local prefs
        prefs.edit().putFloat("remaining_units", newUnits).apply()
        
        // Sync with Firestore if logged in
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).update("remainingUnits", newUnits)
        }
        
        if (newUnits < 5f && units >= 5f) {
            showLowUnitsNotification(newUnits)
        }
        
        return Result.success()
    }

    private fun showLowUnitsNotification(units: Float) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "low_units_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Low Units Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Low Power Units!")
            .setContentText("You only have ${String.format(Locale.getDefault(), "%.2f", units)} units left. Please top up.")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1, notification)
    }
}
