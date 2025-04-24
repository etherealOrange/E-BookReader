package com.example.ebook_reader.ui.ReadingBook

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.databinding.ActivityReadingPdfBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class Reading_PDF : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingPdfBinding
    private val viewModel : ViewModelOnPDF by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadingPdfBinding.inflate(layoutInflater)
        setContentView(bind.root)



        Log.d("RB","onCreate 成功创建")


        val pdfPageAdapter = PDFPageAdapter(viewModel)
        viewModel.page.launchLifeScopeCollectLatest {
            pdfPageAdapter.submitData(it)
        }
        bind.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Reading_PDF, LinearLayoutManager.HORIZONTAL, false)
            adapter = pdfPageAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }
        viewModel.currentPos.launchLifeScopeCollectLatest {
            val s = "${viewModel.book.title}(${it+1}/${viewModel.pdfSize})"
            bind.bookTitle.text = s
        }
        bind.recyclerView.addOnScrollListener(
            object: RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                        val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        val item = pdfPageAdapter.peek(position)
                        viewModel.updateCurrentPos(item?.toLong()?:0L)
                        viewModel.recorder.plusPage(item?.toLong()?:viewModel.currentPos.value)
                    }
                }
            }
        )


    }

    override fun onStart() {
        super.onStart()
        Log.d("RB","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
        viewModel.updateRecord()
        Log.d("RB","onStop 停止")
    }

    override fun onResume() {
        super.onResume()
        viewModel.isFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.recorder.start(viewModel.currentPos.value)
            return@launchLifeScopeCollectLatest
        }
        Log.d("RB","onResume 恢复")
    }

    override fun onPause() {
        super.onPause()
        viewModel.isFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.recorder.pause()
            return@launchLifeScopeCollectLatest
        }
        Log.d("RB","onResume 暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RB","onDestroy 销毁")
    }
}