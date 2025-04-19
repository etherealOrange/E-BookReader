package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.InterfacePackage.ReadingBook.RefreshChapterFlow
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.databinding.FragmentChapterListItemBinding

class TXTChapterListAdapter(
    private val gotoChapter : RefreshChapterFlow
)
    : PagingDataAdapter<ChapterIndex, TXTChapterListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val bind = FragmentChapterListItemBinding.inflate(
            LayoutInflater.from(parent.context)
            ,parent
            ,false
        )
        return ViewHolder(bind)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val bind = holder.bind
        getItem(position)?.let {
            holder.setOrder(it.chapterOrder)
            if(it.partOrder>0){
                val title = it.title+"part ${it.partOrder}"
                bind.chapterName.text = title
            }else{
                bind.chapterName.text = it.title
            }
        }?: run {
            Log.d("TCLA onBindViewHolder","章节不存在")
            bind.chapterName.text = "章节不存在"
        }
    }


    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChapterIndex>() {
            override fun areItemsTheSame(oldItem: ChapterIndex, newItem: ChapterIndex): Boolean {
                return oldItem.chapterOrder == newItem.chapterOrder
            }
            override fun areContentsTheSame(oldItem: ChapterIndex, newItem: ChapterIndex): Boolean {
                return oldItem == newItem
            }
        }
    }
    inner class ViewHolder(val bind: FragmentChapterListItemBinding)
        : RecyclerView.ViewHolder(bind.root) {
        private var order = 0L

        init {
            bind.chapterName.setOnClickListener {
                gotoChapter.refreshChapterFlow(order)
            }
        }

        fun setOrder(o: Long){
            order = o
        }
    }


}