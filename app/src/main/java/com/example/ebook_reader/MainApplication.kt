package com.example.ebook_reader

import android.app.Application
import com.example.ebook_reader.ui.TimeRecorder.BookRecorder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class MainApplication: Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        ConfigManager.getInstance(this)
    }

}