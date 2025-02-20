package com.example.ebook_reader.ui.BookShelf

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.CardviewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.mutableListOf

class BookAdapter :ListAdapter<BookView, BookAdapter.ViewHolder>(
    BookDiffCallBack()
    )
{
    inner class ViewHolder(val binding: CardviewBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = getItem(position)
        with(holder.binding) {
            BookTitleTvCardview.text = book.name
            val chapterProgressText = "读到第${book.currentChapter}章/总共${book.totalChapter}章"
            ChapterProgressTvCardview.text = chapterProgressText
            val readProgress =
                (book.currentChapter.toFloat() / book.totalChapter.toFloat() * 100).toInt()
            ReadingProgressPbCardview.progress = readProgress

        }
    }
    override fun getItemCount() = currentList.size
}