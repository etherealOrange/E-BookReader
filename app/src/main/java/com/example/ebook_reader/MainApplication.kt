package com.example.ebook_reader

import android.app.Application
import com.example.ebook_reader.ui.TimeRecorder.BookRecorder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ConfigManager.getInstance(this)
    }

}