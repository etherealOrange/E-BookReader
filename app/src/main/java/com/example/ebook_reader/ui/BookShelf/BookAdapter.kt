package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.CardviewBinding
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.FolderView


class BookAdapter (private val viewModel: BookShelfViewModel):ListAdapter<BookAndFolderItem, BookAdapter.ViewHolder>(
    BookAndFolderDiffCallBack()
    )
{
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewBinding): RecyclerView.ViewHolder(binding.root){
        //获取当前ViewHolder的位置的ItemId 和 是否是书本
        private var thisItemId: Long = 0
        private var isCurrentRefBook = false
        init {
            //让CheckBox不可点击
            binding.CDSelectedCheckBox.isClickable=false
            binding.root.setOnClickListener {
                when (viewModel.isEditModel.value){
                    true->{
                        //编辑模式下的点击事件
                        inEditModelClickChange()
                    }
                    false->{
                        //非编辑模式下的点击事件
                        notInEditModelClickChange()
                    }
                }
            }
        }

        /**
         * 在非编辑模式下的点击事件
         * 跳转到阅读界面 或 文件夹界面
         */
        private fun notInEditModelClickChange(){
            //跳转到阅读界面 或 文件夹界面
            when (isCurrentRefBook){
                true->{
                    //跳转到阅读界面
                    Log.d("BookAdapter-notInEditModelClickChange", "notInEditModelClickChange: $thisItemId")
                }
                false->{
                    //跳转到文件夹界面
                    viewModel.updateInWhichFolder(thisItemId)
                    viewModel.goIntoFolder()
                }
            }
        }
        //编辑模式下的点击事件
        private fun inEditModelClickChange(){
            //现在的Holder是书本还是文件夹
            when (isCurrentRefBook){
                true->{
                    //查看书本是否已经被选中
                    when(viewModel.selectedBooksId.value.contains(thisItemId)){
                        //已经被选中 去除选中状态 移出选中的书本id
                        true->{
                            binding.CDSelectedCheckBox.isChecked=false
                            viewModel.removeSelectedBooksId(thisItemId)
                        }
                        //未被选中 添加选中状态 添加选中的书本id
                        false->{
                            binding.CDSelectedCheckBox.isChecked=true
                            viewModel.addSelectedBooksId(thisItemId)
                        }
                    }
                }
                false->{
                    //查看文件夹是否已经被选中
                    when(viewModel.selectedFolderId.value.contains(thisItemId)){
                        //已经被选中 去除选中状态 移出选中的文件夹id
                        true->{
                            binding.CDSelectedCheckBox.isChecked=false
                            viewModel.removeSelectedFolderId(thisItemId)
                        }
                        //未被选中 添加选中状态 添加选中的文件夹id
                        false->{
                            binding.CDSelectedCheckBox.isChecked=true
                            viewModel.addSelectedFolderId(thisItemId)
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


    //绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        var itemId: Long = 0

        //处理Book和Folder视图的绑定 并设置itemId
        when(item){
            is BookView ->{
                bindBook(holder, item)
                itemId = item.bookId
            }
            is FolderView -> {
                bindFolder(holder, item)
                itemId = item.folderId
            }
        }
        //给当前ViewHolder提供当前位置的ItemId 和 是否是书本 为了在点击事件中使用
        holder.getHolderCurrentPositionAndIsRefBook(itemId, item is BookView)
    }

    //判断是否在编辑模式下 显示或隐藏CheckBox 以及显示情况下是否选中
    private fun showCheckBox(binding: CardviewBinding,itemId: Long,isRefBook: Boolean){
        //根据是否处于编辑模式显示CheckBox
        when (viewModel.isEditModel.value){
            false ->binding.CDSelectedCheckBox.visibility = View.GONE
            true ->{
                binding.CDSelectedCheckBox.visibility = View.VISIBLE
                //判断当前书本或文件夹是否已经被选中
                //是书本且书本id被选中 或 是文件夹且文件夹id被选中
                when( (isRefBook and  viewModel.selectedBooksId.value.contains(itemId))
                        or (!isRefBook and viewModel.selectedFolderId.value.contains(itemId))){
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
        showCheckBox(binding,view.folderId,false)
        //显示书本 而非文件夹
        binding.CardViewBook.visibility = View.GONE
        binding.CardViewFolder.visibility = View.VISIBLE
        //图片加载逻辑
//        binding.CDFolderCoverIV
        binding.CDFolderNameTV.text = view.title
        val containBooksText = "共${viewModel.getBooksNumInFolder(view.folderId)}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
    }

    //绑定 BookView
    private fun bindBook(
        holder: ViewHolder,
        view: BookView
    ) {
        val binding = holder.binding
        //显示编辑模式下的CheckBox
        showCheckBox(binding,view.bookId,true)
        //显示文件夹 而不是书本
        binding.CardViewFolder.visibility = View.GONE
        binding.CardViewBook.visibility = View.VISIBLE
        //图片加载逻辑
//        binding.CDBookCoverIV
        binding.CDBookTitleTV.text = view.title
        val chapterProgressText = "读到第${view.currentPage}页/总共${view.totalPages}页"
        binding.CDChapterProgressTV.text = chapterProgressText
        val readProgress =
            (view.currentPage.toFloat() / view.totalPages.toFloat() * 100).toInt()
        binding.CDReadProgressPB.progress = readProgress
    }

    //取消全选
    fun cancelAllSelected(){
        viewModel.clearSelectedBooksId()
        viewModel.clearSelectedFolderId()
        notifyItemRangeChanged(0,itemCount)
    }
    //全部选择
    fun selectedAllSelected(){
        for (item in currentList){
            when (item){
                is BookView->viewModel.addSelectedBooksId(item.bookId)
                is FolderView->viewModel.addSelectedFolderId(item.folderId)
            }
        }
        notifyItemRangeChanged(0,itemCount)
    }
}