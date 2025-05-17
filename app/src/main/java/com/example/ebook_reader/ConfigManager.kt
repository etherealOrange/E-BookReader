package com.example.ebook_reader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.ebook_reader.ReadingSetting
import com.google.gson.Gson
import androidx.core.content.edit
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

class ConfigManager private constructor(context: Context){
    private val prefs = context.getSharedPreferences("ReadingConfig", Context.MODE_PRIVATE)
    private val others = context.getSharedPreferences("OtherConfig", Context.MODE_PRIVATE)
    private val dayToCreate = context.getSharedPreferences("DayToCreate", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val testTimeCreate = LocalDate.now()
        .minusYears(3)
        .withDayOfYear(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant().toEpochMilli()

    fun saveDayToCreate(){
        if(getDayToCreate()==-1L){
//            dayToCreate.edit { putLong("DayToCreate", System.currentTimeMillis()) }
            dayToCreate.edit { putLong("DayToCreate",testTimeCreate) }
        }
    }
    fun getDayToCreate(): Long {
        return dayToCreate.getLong("DayToCreate", -1L)
    }

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
        Log.d("ConfigManager", "saveOtherConfig: 保存配置 $json")
    }
    fun getOtherConfig(): OtherSetting{
        val json = others.getString("OtherConfig", null) ?: return OtherSetting()
        Log.d("ConfigManager", "getOtherConfig: 得到新的配置 $json")
        return gson.fromJson(json, OtherSetting::class.java)
    }
    //导出配置到Download
    fun exportConfig(context: Context,configAll: ConfigAll){
        val fileName = "E-Book-config.json"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
        }
        try {
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )?:throw IOException("无法创建文件")
            context.contentResolver.openOutputStream(uri)?.use {
                val configAll = ConfigAll(
                    readingConfig = configAll.readingConfig,
                    otherConfig = configAll.otherConfig
                )
                val json = Gson().toJson(configAll)
                it.write(json.toByteArray())
            }
            Toast.makeText(context,"导出成功 $fileName", Toast.LENGTH_LONG).show()
        }catch (e: Exception){
            Toast.makeText(context,"导出失败 ${e.message}", Toast.LENGTH_LONG).show()
        }

    }
    //导入配置
    fun importConfig(context: Context, uri: Uri): Result<ConfigAll>{
        val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
        if (jsonString == null) {
            return Result.failure(IOException("无法读取文件"))
        }
        val s = Gson().fromJson(jsonString, ConfigAll::class.java)
            ?:return Result.failure(IOException("无法解析文件"))
        return Result.success(s)
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

data class ConfigAll(
    var readingConfig: ReadingSetting = ReadingSetting(),
    var otherConfig: OtherSetting = OtherSetting()
)