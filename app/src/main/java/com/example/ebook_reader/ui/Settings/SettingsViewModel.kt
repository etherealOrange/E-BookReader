package com.example.ebook_reader.ui.Settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.OtherSetting
import com.example.ebook_reader.ReadingSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor (
    @ApplicationContext val context: Context
) : ViewModel() {
    private var _otherConfig = MutableStateFlow<OtherSetting>(OtherSetting())
    private var _readingConfig = MutableStateFlow<ReadingSetting>(ReadingSetting())
    val otherConfig = _otherConfig.asStateFlow()
    val readingConfig = _readingConfig.asStateFlow()
    fun updateOtherConfig(other: OtherSetting) {
        _otherConfig.value = other
    }
    fun updateReadingConfig(reading:  ReadingSetting) {
        _readingConfig.value = reading
    }
}