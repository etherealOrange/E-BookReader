package com.example.ebook_reader

import android.content.Context
import com.example.ebook_reader.ReadingSetting
import com.google.gson.Gson
import androidx.core.content.edit

class ConfigManager private constructor(context: Context){
    private val prefs = context.getSharedPreferences("ReadingConfig", Context.MODE_PRIVATE)
    private val others = context.getSharedPreferences("OtherConfig", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveReadingConfig(config : ReadingSetting){
        val json = gson.toJson(config)
        prefs.edit { putString("ReadingConfig", json) }
    }
    fun getReadingConfig(): ReadingSetting {
        val json = prefs.getString("ReadingConfig", null) ?: return ReadingSetting()
        return gson.fromJson(json, ReadingSetting::class.java)
    }
    fun saveOtherConfig(other: OtherSetting){
        val json = gson.toJson(other)
        others.edit { putString("OtherConfig",json) }
    }
    fun getOtherConfig(): OtherSetting{
        val json = others.getString("OtherConfig", null) ?: return OtherSetting()
        return gson.fromJson(json, OtherSetting::class.java)
    }

    companion object{
        @Volatile private var instance : ConfigManager? =null
        fun getInstance(context: Context): ConfigManager =
            instance?:synchronized(this) {
                instance?: ConfigManager(context.applicationContext).also { instance=it }
            }

    }

}

/**
 * @param textSize 文字大小 范围 20sp - 35sp
 * @param letterSpacing 字间距 范围 0 - 30 应该转为 0.00f - 0.30f
 * @param lineSpacing 行间距 范围 0sp - 30sp
 */
data class ReadingSetting(
    var textSize: Int = 20,
    var letterSpacing: Int = 0,
    var lineSpacing: Int = 0,
)
/**
 *
 */
data class OtherSetting(
    var name:String = "你好",
    var dayTheme: Boolean = true,
    var sleepTime: Int = 20
)