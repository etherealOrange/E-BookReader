package com.example.ebook_reader

import android.content.Context
import com.example.ebook_reader.entities.ReadingSetting
import com.google.gson.Gson
import androidx.core.content.edit

class ConfigManager private constructor(context: Context){
    private val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveConfig(config : ReadingSetting){
        val json = gson.toJson(config)
        prefs.edit { putString("config", json) }
    }
    fun getConfig(): ReadingSetting {
        val json = prefs.getString("config", null) ?: return ReadingSetting()
        return gson.fromJson(json, ReadingSetting::class.java)
    }
    companion object{
        @Volatile private var instance : ConfigManager? =null
        fun getInstance(context: Context): ConfigManager =
            instance?:synchronized(this) {
                instance?: ConfigManager(context.applicationContext).also { instance=it }
            }

    }

}