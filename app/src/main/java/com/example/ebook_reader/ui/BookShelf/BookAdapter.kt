package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterChangePosition
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterOpenActivity
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterSelectedControl
import com.example.ebook_reader.R
import com.example.ebook_reader.Repository.BookShelf.BookAdapterUIState
import com.example.ebook_reader.Repository.BookShelf.FolderAdapterUIState
import com.example.ebook_reader.databinding.CardviewBinding
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.UIFolderView

class BookAdapter (
    private val openActivity: BooksAdapterOpenActivity,
    private val changePosition: BooksAdapterChangePosition,
    private val selectedControl: BooksAdapterSelectedControl
):ListAdapter<BookAdapterUIState, BookAdapter.ViewHolder>(
    BookAndFolderDiffCallBack()
    )
{
    private var _isEditModel = false
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewBinding): RecyclerView.ViewHolder(binding.root){
        //获取当前ViewHolder的位置的ItemId 和 是否是书本
        private var thisItemId: Long = 0
        private var isCurrentRefBook = false
        private var book: BookView? = null
        init {
            binding.root.setOnClickListener {
                when (_isEditModel){
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
         *
         * 跳转到阅读界面 或 文件夹界面
         */
        private fun notInEditModelClickChange(){
            //跳转到阅读界面 或 文件夹界面
            when (isCurrentRefBook){
                true->{
                    //跳转到阅读界面
                    openActivity.openActivity(book!!)
                }
                false->{
                    //跳转到文件夹界面
                    changePosition.goIntoFolder(thisItemId)
                }
            }
        }
        //编辑模式下的点击事件
        private fun inEditModelClickChange(){
            when (isCurrentRefBook){
                true->selectedControl.switchSelectedBooksId(thisItemId)
                false->selectedControl.switchSelectedFolderId(thisItemId)
            }
        }
        //给当前ViewHolder提供当前位置的ItemId 和 是否是书本
        fun getHolderCurrentPositionAndIsRefBook(itemId: Long, item: BookAdapterUIState){
            thisItemId = itemId
            when(item.item){
                is BookView->{
                    isCurrentRefBook = true
                    book = item.item
                }
                else -> {
                    isCurrentRefBook = false
                }
            }
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
        when(item.item){
            is BookView ->{
                bindBook(holder, item.item )
                itemId = item.item.bookId
            }
            is UIFolderView -> {
                bindFolder(holder, item.item)
                itemId = item.item.folder.folderId
            }
        }
        //设置CheckBox可见性
        when(_isEditModel){
            true->{
                holder.binding.CDSelectedCheckBox.visibility= View.VISIBLE
            }
            false->{
                holder.binding.CDSelectedCheckBox.visibility= View.GONE
            }
        }
        //让CheckBox不可点击
        holder.binding.CDSelectedCheckBox.isClickable=false
        //设置CheckBox是否点击
        holder.binding.CDSelectedCheckBox.isChecked = item.isSelected
        //给当前ViewHolder提供当前位置的ItemId 和 是否是书本 为了在点击事件中使用
        holder.getHolderCurrentPositionAndIsRefBook(itemId, item)
    }

    fun setMode(isEditModel: Boolean){
        _isEditModel = isEditModel
        notifyItemRangeChanged(0,itemCount)
    }


    //绑定 FolderView
    private fun bindFolder(
        holder: ViewHolder,
        view: UIFolderView
    ) {
        val binding = holder.binding

        //显示书本 而非文件夹
        binding.CardViewBook.visibility = View.GONE
        binding.CardViewFolder.visibility = View.VISIBLE
        //图片加载逻辑
        val coverUrl = view.folder.coverUrl
        if(coverUrl.isNotEmpty()){
            binding.CDFolderCoverIV.load(coverUrl.toUri())
        }
        else{
            binding.CDFolderCoverIV.load(R.drawable.icons8_folder)
        }
        binding.CDFolderNameTV.text = view.folder.title
        val containBooksText = "共${view.booksNum}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
    }

    //绑定 BookView
    private fun bindBook(
        holder: ViewHolder,
        view: BookView
    ) {
        val binding = holder.binding

        //显示文件夹 而不是书本
        binding.CardViewFolder.visibility = View.GONE
        binding.CardViewBook.visibility = View.VISIBLE
        //图片加载逻辑
        val coverUrl = view.coverUrl
        if(coverUrl.isNotEmpty()){
            binding.CDBookCoverIV.load(coverUrl.toUri())
        }
        else{
            binding.CDBookCoverIV.load(R.drawable.icons8_book)
        }
        binding.CDBookTitleTV.text = view.title
        val chapterProgressText = "读到第${view.currentPage}页/总共${view.totalPages}页"
        binding.CDChapterProgressTV.text = chapterProgressText
        val readProgress =
            (view.currentPage.toFloat() / view.totalPages.toFloat() * 100).toInt()
        binding.CDReadProgressPB.progress = readProgress
    }

}