package com.example.ebook_reader.ui.ReadingBook

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.databinding.ActivityReadingTxtBinding
import com.example.ebook_reader.entities.BookView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class Reading_TXT : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingTxtBinding
    private val viewModel : ViewModelOnTXT by viewModels()
    private lateinit var bottomDialog: TXTBottomDialog
    private val configManager = ConfigManager.getInstance(this)

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadingTxtBinding.inflate(layoutInflater)
        setContentView(bind.root)

        viewModel.updateConfig(configManager.getConfig())

        bottomDialog = TXTBottomDialog()




        Log.d("RB","onCreate 成功创建")
        //设置章节的Adapter
        val chapterAdapter = TXTChapterAdapter(this,viewModel.config.value)

        viewModel.config.debounce(300).launchLifeScopeCollectLatest {
            chapterAdapter.updatePageConfig(it)
        }

        //同步章节数据
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.chapterFlow.launchLifeScopeCollectLatest { Log.d("RT ","$it"); chapterAdapter.submitData(it) }
        }

        bind.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.HORIZONTAL,false)
            this.adapter = chapterAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        bind.settingReadingBtn.setOnClickListener {
            bottomDialog.show(supportFragmentManager,"BottomDialog")
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d("RB","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
        configManager.saveConfig(viewModel.config.value)
        Log.d("RB","onStop 停止")
    }

    override fun onResume() {
        super.onResume()
        Log.d("RB","onResume 恢复")
    }

    override fun onPause() {
        super.onPause()
        Log.d("RB","onResume 暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RB","onDestroy 销毁")
    }
}