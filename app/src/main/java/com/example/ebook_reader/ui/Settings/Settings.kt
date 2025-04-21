package com.example.ebook_reader.ui.Settings

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.res.Configuration
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.ebook_reader.ConfigAll
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.OtherSetting
import com.example.ebook_reader.R
import com.example.ebook_reader.ReadingSetting
import com.example.ebook_reader.databinding.FragmentSettingsBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import okio.IOException

@AndroidEntryPoint
class Settings : ExtendFragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var bind : FragmentSettingsBinding
    private lateinit var configManager : ConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSettingsBinding.inflate(inflater,container,false)
        configManager = ConfigManager.getInstance(inflater.context)
        viewModel.updateOtherConfig(configManager.getOtherConfig())
        viewModel.updateReadingConfig(configManager.getReadingConfig())
        return bind.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //动态更新UI
        viewModel.readingConfig.launchLifeScopeCollectLatest {
            val textSize = it.textSize.toString()
            val letterSpacing = it.letterSpacing.toString()
            val lineSpacing = it.lineSpacing.toString()
            if(bind.textSizeEdit.text.toString() != textSize){
                bind.textSizeEdit.setText(textSize)
            }
            if (bind.textLetterSpacingEdit.text.toString() != letterSpacing){
                bind.textLetterSpacingEdit.setText(letterSpacing)
            }
            if (bind.textLineSpacingEdit.text.toString() != lineSpacing){
                bind.textLineSpacingEdit.setText(lineSpacing)
            }
        }
        viewModel.otherConfig.launchLifeScopeCollectLatest {
            val sleepTime = it.sleepTime.toString()
            val nightMode = !it.dayTheme
            val isNightMode = Configuration.UI_MODE_NIGHT_YES == (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)

            Log.d("Settings", "现在的nightTheme: $nightMode")
            Log.d("Settings", "onViewCreated: $isNightMode")
            Log.d("Settings", "onViewCreated: ${Configuration.UI_MODE_NIGHT_YES} ${requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK}")

            if (bind.timeToNotifyEdit.text.toString() != sleepTime){
                bind.timeToNotifyEdit.setText(sleepTime)
            }
            if (nightMode != isNightMode) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                requireActivity().recreate()
            }
        }
        //设置字体大小
        bind.textSizeEdit.doOnTextChanged {
            text,_,_,_ ->
            val size = text.toString().toIntOrNull()
            if (size == null) {
                bind.textSizeEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (size in 20..35){
                viewModel.updateReadingConfig(viewModel.readingConfig.value.copy(textSize = size))
            }else{
                bind.textSizeEdit.error = "请输入范围在20-35之间的数字"
            }
        }
        //设置行高
        bind.textLineSpacingEdit.doOnTextChanged {
            text,_,_,_->
            val lineSpacing = text.toString().toIntOrNull()
            if( lineSpacing == null) {
                bind.textLineSpacingEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (lineSpacing in 0..30){
                viewModel.updateReadingConfig(viewModel.readingConfig.value.copy(lineSpacing = lineSpacing))
            }else{
                bind.textLineSpacingEdit.error = "请输入范围在0-30之间的数字"
            }
        }

        //设置字间距
        bind.textLetterSpacingEdit.doOnTextChanged { text, _, _, _ ->
            val letterSpacing = text.toString().toIntOrNull()
            if (letterSpacing == null) {
                bind.textLetterSpacingEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (letterSpacing in 0..30) {
                viewModel.updateReadingConfig(viewModel.readingConfig.value.copy(letterSpacing = letterSpacing))
            } else {
                bind.textLetterSpacingEdit.error = "请输入范围在0-30之间的数字"
            }
        }

        //设置提醒时间
        bind.timeToNotifyEdit.doOnTextChanged {
            text,_,_,_->
            val sleepTime = text.toString().toIntOrNull()
            if (sleepTime == null) {
                bind.timeToNotifyEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (sleepTime in 0..480){
                viewModel.updateOtherConfig(viewModel.otherConfig.value.copy(sleepTime = sleepTime))
            }else{
                bind.timeToNotifyEdit.error = "请输入范围在0-480之间的数字"
            }
        }

        //切换 白天主题
        bind.toDayThemeBtn.setOnClickListener {
            Log.d("Settings","点击了白天主题 ${viewModel.otherConfig.value.dayTheme}")
            viewModel.updateOtherConfig(viewModel.otherConfig.value.copy(dayTheme = true))
        }
        //切换 夜晚主题
        bind.toNightThemeBtn.setOnClickListener {
            viewModel.updateOtherConfig(viewModel.otherConfig.value.copy(dayTheme = false))
            Log.d("Settings","点击了夜晚主题 ${viewModel.otherConfig.value.dayTheme}")
        }

        //导出配置
        bind.saveConfig.setOnClickListener {
            configManager.exportConfig(requireContext(),
                ConfigAll(
                    readingConfig = viewModel.readingConfig.value,
                    otherConfig = viewModel.otherConfig.value
                )
            )
        }
        //导入配置
        bind.loadConfig.setOnClickListener {
            jsonPickerLauncher.launch(arrayOf("application/json"))
        }

    }
    private val jsonPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri->
        if(uri!=null){
            configManager.importConfig(requireContext(),uri)
                .onSuccess {
                    viewModel.updateOtherConfig(it.otherConfig)
                    viewModel.updateReadingConfig(it.readingConfig)
                    Toast.makeText(requireContext(),"导入成功 ", Toast.LENGTH_LONG).show()
                }
                .onFailure {
                    Toast.makeText(requireContext(),"导入失败 ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        configManager.saveOtherConfig(viewModel.otherConfig.value)
        configManager.saveReadingConfig(viewModel.readingConfig.value)
    }
}