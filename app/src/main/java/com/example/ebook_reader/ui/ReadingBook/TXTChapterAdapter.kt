package com.example.ebook_reader.ui.ReadingBook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import com.example.ebook_reader.databinding.FragmentTxtPageBinding

class TXTChapterAdapter : PagingDataAdapter<ChapterPage, TXTChapterAdapter.ViewHolder>(DIFF_CALLBACK){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val bind = FragmentTxtPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(bind)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        getItem(position)?.let {
            holder.binding.ChapterTitle.text = it.title
            holder.binding.TextView.text = it.content
        } ?: run {
            holder.binding.ChapterTitle.text = "章节不存在"
            holder.binding.TextView.text = "章节内容不存在"
        }
    }

    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChapterPage>() {
            override fun areItemsTheSame(oldItem: ChapterPage, newItem: ChapterPage): Boolean {
                return oldItem.title == newItem.title
            }
            override fun areContentsTheSame(oldItem: ChapterPage, newItem: ChapterPage): Boolean {
                return oldItem == newItem
            }
        }
    }
    inner class ViewHolder(val binding: FragmentTxtPageBinding):
        RecyclerView.ViewHolder(binding.root)

}