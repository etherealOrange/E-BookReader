package com.example.ebook_reader.ui.Settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.ebook_reader.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor (
    @ApplicationContext val context: Context
) : ViewModel() {
    val configManager = ConfigManager.getInstance(context)



}