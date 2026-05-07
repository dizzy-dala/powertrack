package com.abdallah.powertrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.abdallah.powertrack.navigation.AppNavHost
import com.abdallah.powertrack.ui.theme.PowerTrackTheme
import com.abdallah.powertrack.worker.RefreshWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        setupBackgroundWork()
        
        enableEdgeToEdge()
        setContent {
            PowerTrackTheme {
                AppNavHost()
            }
        }
    }

    private fun setupBackgroundWork() {
        val workRequest = PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS)
            .build()
            
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "PowerRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
