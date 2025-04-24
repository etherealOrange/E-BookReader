package com.example.ebook_reader.ui.ReadingBook

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.InterfacePackage.ReadingBook.LookBookMark
import com.example.ebook_reader.Repository.ReadingBook.BookMarkUI
import com.example.ebook_reader.Repository.ReadingBook.MakeBookMark
import com.example.ebook_reader.databinding.ActivityReadingTxtBinding
import com.example.ebook_reader.databinding.InputBookmarkBinding
import com.example.ebook_reader.databinding.LookTextViewBinding
import com.example.ebook_reader.ui.DialogBuilderFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last

@AndroidEntryPoint
class Reading_TXT : ExtendAppCompatActivity()
, MakeBookMark
, LookBookMark{
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
        viewModel.updateConfig(configManager.getReadingConfig())
        bottomDialog = TXTBottomDialog()
        Log.d("RB","onCreate 成功创建")








        //设置章节的Adapter
        val chapterAdapter = TXTChapterAdapter(this,viewModel.config.value,this)

        viewModel.config.debounce(300).launchLifeScopeCollectLatest {
            chapterAdapter.updatePageConfig(it)
        }

        //同步章节数据
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.chapterFlow.launchLifeScopeCollectLatest { chapterAdapter.submitData(it)
            }
        }
        //章节跳转
        viewModel.jumpOfChapter.notify.launchLifeScopeCollectLatest {
            chapterAdapter.refresh()
        }
        bind.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.HORIZONTAL,false)
            this.adapter = chapterAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }
        //获取实时 翻页后的章节位置
        bind.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        Log.d("RB","当前章节位置 $position")
                        val item = chapterAdapter.peek(position)
                        Log.d("RB","当前章节 ${item?.title} 顺序 ${item?.order}")
                        viewModel.updatePos(item?.order?:0L)
                        viewModel.recorder.plusPage(item?.order?:viewModel.currentChapterPos.value)
                    }
                }
            }
        )

        val chapterListAdapter = TXTChapterListAdapter(viewModel)
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.chapterListFlow.launchLifeScopeCollectLatest {
                bind.ChapterTitle.text = viewModel.book.title
                chapterListAdapter.submitData(it)  }
        }
        //章节列表跳转
        viewModel.jumpOfList.notify.launchLifeScopeCollectLatest {
            chapterListAdapter.refresh()
        }

        bind.chapterList.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.VERTICAL,false)
            this.adapter = chapterListAdapter
        }

        //书签的Adapter
        val bookMarkAdapter = TXTBookMarkAdapter(viewModel,this)
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.bookMarkFlow.launchLifeScopeCollectLatest {
                bookMarkAdapter.submitData(it)
            }
        }
        bind.bookmarkList.apply {
            layoutManager = LinearLayoutManager(this@Reading_TXT, LinearLayoutManager.VERTICAL,false)
            this.adapter = bookMarkAdapter
        }

        viewModel.jumpOfBookMark.notify.launchLifeScopeCollectLatest {
            bookMarkAdapter.refresh()
        }


        //在打开书签抽屉时，更新书签列表位置
        bind.main.addDrawerListener(
            object :DrawerLayout.DrawerListener{
                override fun onDrawerOpened(drawerView: View) {
                    val layoutParams = drawerView.layoutParams as DrawerLayout.LayoutParams
                    if(layoutParams.gravity == GravityCompat.END) {
                        viewModel.refreshBookMarkFlow(viewModel.currentChapterPos.value)
                    }
                }
                override fun onDrawerSlide(
                    drawerView: View,
                    slideOffset: Float) {}
                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {}
            }
        )

        //设置定位 书签按钮
        bind.jumpToBookmark.setOnClickListener {
            viewModel.refreshBookMarkFlow(viewModel.currentChapterPos.value)
        }

        //设置章节列表的 底部 底部 按钮
        bind.toTopListBTN.setOnClickListener {
            viewModel.refreshChapterListFlow(true)
        }
        bind.toBottomListBTN.setOnClickListener {
            viewModel.refreshChapterListFlow(false)
        }

        //打开左侧抽屉  章节导航
        bind.chaptersReadingBtn.setOnClickListener {
            bind.main.openDrawer(GravityCompat.START)
        }
        //打开右边抽屉  书签导航
        bind.lookBookmarkReadingBtn.setOnClickListener {
            bind.main.openDrawer(GravityCompat.END)
            viewModel.refreshBookMarkFlow(viewModel.currentChapterPos.value)
        }

        //底部抽屉
        bind.settingReadingBtn.setOnClickListener {
            bottomDialog.show(supportFragmentManager,"BottomDialog")
        }

    }


    override fun makeBookMark(pos: Long) {
        val input = InputBookmarkBinding.inflate(layoutInflater)
        input.editText.setText(viewModel.currentBookMark.value?.content?:"")
        DialogBuilderFactory.build(context = this, title = "创建或修改书签",input.root,
            { _,_->
                viewModel.changeBookMark(input.editText.text.toString())
            },
            {_,_->
                Log.d("RT","取消书签创建")
            }
        ).show()
    }
    override fun look(bookMark: BookMarkUI) {
        val textView = LookTextViewBinding.inflate(layoutInflater)
        textView.text.text = bookMark.content
        DialogBuilderFactory.build(context = this,title = bookMark.title,textView.root,
            {_,_->},
            {_,_->}
        ).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d("RB","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
        configManager.saveReadingConfig(viewModel.config.value)
        viewModel.updateRecord()
        Log.d("RB","onStop 停止")
    }

    override fun onResume() {
        super.onResume()
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
            viewModel.recorder.start(viewModel.currentChapterPos.value)
            return@launchLifeScopeCollectLatest
        }
        Log.d("RB","onResume 恢复")
    }

    override fun onPause() {
        super.onPause()
        viewModel.isInitFinished.filter { it }.launchLifeScopeCollectLatest {
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