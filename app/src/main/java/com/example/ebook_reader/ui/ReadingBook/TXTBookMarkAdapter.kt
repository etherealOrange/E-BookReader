package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.InterfacePackage.ReadingBook.DeleteBookMark
import com.example.ebook_reader.InterfacePackage.ReadingBook.LookBookMark
import com.example.ebook_reader.Repository.ReadingBook.BookMarkUI
import com.example.ebook_reader.databinding.FragmentTxtBookmarkItemBinding


class TXTBookMarkAdapter(
    private val deleteBookMark: DeleteBookMark,
    private val lookBookMark: LookBookMark,
)
: PagingDataAdapter<BookMarkUI, TXTBookMarkAdapter.ViewHolder>(DIFF_CALLBACK){


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val bind = holder.bind
        getItem(position)?.let {
            holder.setItem(it)
            Log.d("TBMA onBindViewHolder","书签顺序号 ${it.order} 名称 ${it.title}  请求Position $position")
            bind.txtBookmarkName.text = it.title
            bind.txtBookmarkContent.text = it.content
        }?:run{
            bind.txtBookmarkName.text = "书签还不存在"
            bind.txtBookmarkContent.text = "书签内容也不存在"
        }
    }

    inner class ViewHolder(val bind: FragmentTxtBookmarkItemBinding): RecyclerView.ViewHolder(bind.root){
        private var item: BookMarkUI? = null
        fun setItem(item: BookMarkUI){
            this.item = item
        }
        init {
            bind.lookBookmarkBtn.setOnClickListener {
                if(it.isVisible && item != null){
                    lookBookMark.look(item!!)
                }
            }
            bind.deleteBookmarkBtn.setOnClickListener {
                if(it.isVisible && item != null){
                    deleteBookMark.delete(item!!.order)
                }
            }
        }

    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val bind = FragmentTxtBookmarkItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(bind)
    }
    companion object{
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookMarkUI>() {
            override fun areItemsTheSame(
                oldItem: BookMarkUI,
                newItem: BookMarkUI
            ): Boolean {
                return oldItem.order == newItem.order
            }
            override fun areContentsTheSame(
                oldItem: BookMarkUI,
                newItem: BookMarkUI
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
