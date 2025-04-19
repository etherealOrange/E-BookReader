package com.example.ebook_reader.ui.ReadingBook

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.viewModels
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.R
import com.example.ebook_reader.databinding.ActivityReadingTxtBinding
import com.example.ebook_reader.entities.BookView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class Reading_TXT : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingTxtBinding
    private val viewModel : ViewModelOnTXT by viewModels()
    private lateinit var bottomDialog: TXTBottomDialog
    private val configManager = ConfigManager.getInstance(this)

    @SuppressLint("ClickableViewAccessibility")
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
            viewModel.chapterFlow.launchLifeScopeCollectLatest { chapterAdapter.submitData(it)
            }
        }
        //章节跳转
        viewModel.changePosition.launchLifeScopeCollectLatest {
            chapterAdapter.refresh()
        }

        bind.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.HORIZONTAL,false)
            this.adapter = chapterAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        val chapterListAdapter = TXTChapterListAdapter(viewModel)
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.chapterListFlow.launchLifeScopeCollectLatest {
                bind.ChapterTitle.text = viewModel.book.title
                chapterListAdapter.submitData(it)  }
        }
        //章节列表跳转
        viewModel.changeListPosition.launchLifeScopeCollectLatest {
            chapterListAdapter.refresh()
        }

        bind.chapterList.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.VERTICAL,false)
            this.adapter = chapterListAdapter
        }




        //设置章节列表的 底部 底部 按钮
        bind.toTopListBTN.setOnClickListener {
            viewModel.refreshChapterListFlow(true)
        }
        bind.toBottomListBTN.setOnClickListener {
            viewModel.refreshChapterListFlow(false)
        }

        //设置左侧抽屉  默认打开的是左边的视图, open打开右边 close打开左边
        bind.chaptersReadingBtn.setOnClickListener {
            bind.main.open()
        }



        //底部抽屉
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