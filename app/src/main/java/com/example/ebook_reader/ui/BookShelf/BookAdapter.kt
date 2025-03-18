package com.example.ebook_reader.ui.BookShelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.CardviewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookAdapter (private val bookShelf: BookShelf):ListAdapter<BookAndFolderItem, BookAdapter.ViewHolder>(
    BookAndFolderDiffCallBack()
    )
{
    //是否处于编辑模式 的Boolean
    private var isInEditModel = false
    //选中的书本id 和 文件夹id
    private val _selectedBooks_id = mutableListOf<Long>()
    private val _selectedFolder_id = mutableListOf<Long>()
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewBinding): RecyclerView.ViewHolder(binding.root){
        //获取当前ViewHolder的位置的ItemId 和 是否是书本
        private var thisItemId: Long = 0
        private var isCurrentRefBook = false
        init {
            //让CheckBox不可点击
            binding.CDSelectedCheckBox.isClickable=false
            binding.root.setOnClickListener {
                when (isInEditModel){
                    true->{
                        //编辑模式下的点击事件
                        inEditModelClickChange()
                    }
                    false->{
                        //跳转到阅读界面 或 文件夹界面
                    }
                }
            }
        }
        //编辑模式下的点击事件
        private fun inEditModelClickChange(){
            //现在的Holder是书本还是文件夹
            when (isCurrentRefBook){
                true->{
                    //查看书本是否已经被选中
                    when(_selectedBooks_id.contains(thisItemId)){
                        //已经被选中 去除选中状态 移出选中的书本id
                        true->{
                            binding.CDSelectedCheckBox.isChecked=false
                            _selectedBooks_id.remove(thisItemId)
                        }
                        //未被选中 添加选中状态 添加选中的书本id
                        false->{
                            binding.CDSelectedCheckBox.isChecked=true
                            _selectedBooks_id.add(thisItemId)
                        }
                    }
                }
                false->{
                    //查看文件夹是否已经被选中
                    when(_selectedFolder_id.contains(thisItemId)){
                        //已经被选中 去除选中状态 移出选中的文件夹id
                        true->{
                            binding.CDSelectedCheckBox.isChecked=false
                            _selectedFolder_id.remove(thisItemId)
                        }
                        //未被选中 添加选中状态 添加选中的文件夹id
                        false->{
                            binding.CDSelectedCheckBox.isChecked=true
                            _selectedFolder_id.add(thisItemId)
                        }
                    }
                }
            }
        }
        //给当前ViewHolder提供当前位置的ItemId 和 是否是书本
        fun getHolderCurrentPositionAndIsRefBook(itemId: Long, isRefBook: Boolean){
            thisItemId = itemId
            isCurrentRefBook = isRefBook
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    init {
        //延迟初始化监听是否处于编辑模式, 实时更改isInEditModel 清空已选择的内容 并通知修改视图
        bookShelf.lifecycleScope.launch {
            bookShelf.isEditModel.collectLatest {
                isInEditModel = it
                _selectedBooks_id.clear()
                _selectedFolder_id.clear()
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    //绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        var itemId: Long = 0
        //关闭CheckBox
//        holder.binding.CDSelectedCheckBox.visibility = View.GONE
        //处理Book和Folder视图的绑定 并设置itemId
        when(item){
            is BookView ->{
                bindBook(holder, item)
                itemId = item.uid
            }
            is FolderView -> {
                bindFolder(holder, item)
                itemId = item.uid
            }
        }
        //给当前ViewHolder提供当前位置的ItemId 和 是否是书本 为了在点击事件中使用
        holder.getHolderCurrentPositionAndIsRefBook(itemId, item is BookView)
    }

    //判断是否在编辑模式下 显示或隐藏CheckBox 以及显示情况下是否选中
    private fun showCheckBox(binding: CardviewBinding,itemId: Long,isRefBook: Boolean){
        //根据是否处于编辑模式显示CheckBox
        when (isInEditModel){
            false ->binding.CDSelectedCheckBox.visibility = View.GONE
            true ->{
                binding.CDSelectedCheckBox.visibility = View.VISIBLE
                //判断当前书本或文件夹是否已经被选中
                //是书本且书本id被选中 或 是文件夹且文件夹id被选中
                when( (isRefBook and  _selectedBooks_id.contains(itemId))
                        or (!isRefBook and _selectedFolder_id.contains(itemId))){
                    true->binding.CDSelectedCheckBox.isChecked=true
                    false->binding.CDSelectedCheckBox.isChecked=false
                }
            }
        }
    }
    //绑定 FolderView
    private fun bindFolder(
        holder: ViewHolder,
        view: FolderView
    ) {
        val binding = holder.binding
        //显示编辑模式下的CheckBox
        showCheckBox(binding,view.uid,false)
        //显示书本 而非文件夹
        binding.CardViewBook.visibility = View.GONE
        binding.CardViewFolder.visibility = View.VISIBLE
        //图片加载逻辑
//        binding.CDFolderCoverIV
        binding.CDFolderNameTV.text = view.name
        val containBooksText = "共${view.booksNum}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
    }

    //绑定 BookView
    private fun bindBook(
        holder: ViewHolder,
        view: BookView
    ) {
        val binding = holder.binding
        //显示编辑模式下的CheckBox
        showCheckBox(binding,view.uid,true)
        //显示文件夹 而不是书本
        binding.CardViewFolder.visibility = View.GONE
        binding.CardViewBook.visibility = View.VISIBLE
        //图片加载逻辑
//        binding.CDBookCoverIV
        binding.CDBookTitleTV.text = view.name
        val chapterProgressText = "读到第${view.currentChapter}章/总共${view.totalChapter}章"
        binding.CDChapterProgressTV.text = chapterProgressText
        val readProgress =
            (view.currentChapter.toFloat() / view.totalChapter.toFloat() * 100).toInt()
        binding.CDReadProgressPB.progress = readProgress
    }


}