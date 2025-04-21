package com.example.ebook_reader.ui.Settings

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.OtherSetting
import com.example.ebook_reader.R
import com.example.ebook_reader.ReadingSetting
import com.example.ebook_reader.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Settings : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var bind : FragmentSettingsBinding
    private lateinit var configManager : ConfigManager
    private lateinit var otherConfig : OtherSetting
    private lateinit var readingConfig : ReadingSetting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentSettingsBinding.inflate(inflater,container,false)
        configManager = ConfigManager.getInstance(inflater.context)
        otherConfig = configManager.getOtherConfig()
        readingConfig = configManager.getReadingConfig()
        return bind.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置字体大小
        bind.textSizeEdit.setText(readingConfig.textSize.toString())
        bind.textSizeEdit.doOnTextChanged {
            text,_,_,_ ->
            val size = text.toString().toIntOrNull()
            if (size == null) {
                bind.textSizeEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (size in 20..35){
                readingConfig.textSize = size
            }else{
                bind.textSizeEdit.error = "请输入范围在20-35之间的数字"
            }
        }
        //设置行高
        bind.textLineSpacingEdit.setText(readingConfig.lineSpacing.toString())
        bind.textLineSpacingEdit.doOnTextChanged {
            text,_,_,_->
            val lineSpacing = text.toString().toIntOrNull()
            if( lineSpacing == null) {
                bind.textLineSpacingEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (lineSpacing in 0..30){
                readingConfig.lineSpacing = lineSpacing
            }else{
                bind.textLineSpacingEdit.error = "请输入范围在0-30之间的数字"
            }
        }

        //设置字间距
        bind.textLetterSpacingEdit.setText(readingConfig.letterSpacing.toString())
        bind.textLetterSpacingEdit.doOnTextChanged { text, _, _, _ ->
            val letterSpacing = text.toString().toIntOrNull()
            if (letterSpacing == null) {
                bind.textLetterSpacingEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (letterSpacing in 0..30) {
                readingConfig.letterSpacing = letterSpacing
            } else {
                bind.textLetterSpacingEdit.error = "请输入范围在0-30之间的数字"
            }
        }

        //设置提醒时间
        bind.timeToNotifyEdit.setText(otherConfig.sleepTime.toString())
        bind.timeToNotifyEdit.doOnTextChanged {
            text,_,_,_->
            val sleepTime = text.toString().toIntOrNull()
            if (sleepTime == null) {
                bind.timeToNotifyEdit.error = "请输入数字"
                return@doOnTextChanged
            }
            if (sleepTime in 0..480){
                otherConfig.sleepTime = sleepTime
            }else{
                bind.timeToNotifyEdit.error = "请输入范围在0-480之间的数字"
            }
        }

        //切换 白天主题
        bind.toDayThemeBtn.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
            requireActivity().recreate()
        }
        //切换 夜晚主题
        bind.toNightThemeBtn.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
            requireActivity().recreate()
        }




    }



    override fun onStart() {
        super.onStart()
        otherConfig = configManager.getOtherConfig()
        readingConfig = configManager.getReadingConfig()
    }

    override fun onPause() {
        super.onPause()
        configManager.saveOtherConfig(otherConfig)
        configManager.saveReadingConfig(readingConfig)
    }


}