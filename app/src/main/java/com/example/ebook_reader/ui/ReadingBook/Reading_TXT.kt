package com.example.ebook_reader.ui.ReadingBook

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.databinding.ActivityReadingTxtBinding
import com.example.ebook_reader.entities.BookView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class Reading_TXT : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingTxtBinding
    private val viewModel : ViewModelOnTXT by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadingTxtBinding.inflate(layoutInflater)
        setContentView(bind.root)


        Log.d("RB","onCreate 成功创建")
        //设置章节的Adapter
        val chapterAdapter = TXTChapterAdapter()
        //同步章节数据
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.chapterFlow.launchLifeScopeCollectLatest { chapterAdapter.submitData(it) }
        }

        bind.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.HORIZONTAL,false)
            this.adapter = chapterAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }



    }

    override fun onStart() {
        super.onStart()
        Log.d("RB","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
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