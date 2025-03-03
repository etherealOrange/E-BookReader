package com.example.ebook_reader.ui.BookShelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.CardviewBinding

class BookAdapter :ListAdapter<BookAndFolderItem, BookAdapter.ViewHolder>(
    BookDiffCallBack()
    )
{
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    //绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        //关闭CheckBox
        holder.binding.CDSelectedCheckBox.visibility = View.GONE
        //处理Book和Folder视图的绑定
        when(item){
            is BookView -> bindBook(holder, item)
            is FolderView -> bindFolder(holder, item)
        }
    }
    //绑定 FolderView
    private fun bindFolder(
        holder: ViewHolder,
        view: FolderView
    ) {
        val binding = holder.binding
        //选择要显示的视图
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

    override fun getItemCount() = currentList.size
}