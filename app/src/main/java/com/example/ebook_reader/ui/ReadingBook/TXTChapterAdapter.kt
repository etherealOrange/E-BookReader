package com.example.ebook_reader.ui.ReadingBook

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import com.example.ebook_reader.databinding.FragmentTxtPageBinding
import com.example.ebook_reader.entities.ReadingSetting

class TXTChapterAdapter(
    private val context: Context
    , private var config:ReadingSetting)
    : PagingDataAdapter<ChapterPage, TXTChapterAdapter.ViewHolder>(DIFF_CALLBACK){
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
        val bind = holder.binding

        getItem(position)?.let {
            bind.TextView.textSize = config.textSize.toFloat()
            bind.TextView.lineHeight = spToPx(config.textSize + config.lineSpacing).toInt()
            bind.TextView.letterSpacing = config.letterSpacing.toFloat()/100

            bind.ChapterTitle.text = it.title
            bind.TextView.text = it.content

        } ?: run {
            Log.d("TCA onBindViewHolder","章节不存在")
            bind.ChapterTitle.text = "章节不存在"
            bind.TextView.text = "章节内容不存在"
        }
    }

    private fun spToPx(int: Int): Float{
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            int.toFloat(),
            context.resources.displayMetrics
        )
    }
    fun updatePageConfig(config: ReadingSetting){
        Log.d("TCA updatePageConfig","更新了config为 $config")
        this.config=config
        notifyItemRangeChanged(0,itemCount)
    }

    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChapterPage>() {
            override fun areItemsTheSame(oldItem: ChapterPage, newItem: ChapterPage): Boolean {
                return oldItem.order == newItem.order
            }
            override fun areContentsTheSame(oldItem: ChapterPage, newItem: ChapterPage): Boolean {
                return oldItem == newItem
            }
        }
    }
    inner class ViewHolder(val binding: FragmentTxtPageBinding):
        RecyclerView.ViewHolder(binding.root)

}