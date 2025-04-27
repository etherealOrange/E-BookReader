package com.example.ebook_reader.ui.TimeTravel

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterOpenActivity
import com.example.ebook_reader.R
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Tools
import com.example.ebook_reader.databinding.StatisticBookItemBinding
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.ui.TimeTravel.TimeTravelViewModel.Mode
import java.io.File

class TopTenBookAdapter(
    private val open: BooksAdapterOpenActivity
): ListAdapter<BookDataUI, TopTenBookAdapter.ViewHolder>(DIFF) {

    private var mode = Mode.Duration
    inner class ViewHolder(val bind: StatisticBookItemBinding): RecyclerView.ViewHolder(bind.root){
        private var book: BookView? = null
        fun setItem(book: BookView?){
            this.book = book
        }
        init {
            bind.root.setOnClickListener {
                book?.let {
                    open.openActivity(it)
                }
            }
        }

    }
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val bind = holder.bind
        getItem(position)?.let {
            holder.setItem(it.book)
            it.book?.let {
                bind.title.text = it.title
                if(File(it.coverUrl).isFile){
                    bind.image.load(it.coverUrl)
                }else{
                    bind.image.load(R.drawable.ic_launcher_foreground)
                }

                val s = "页数 :${it.currentPage}/${it.totalPages}"
                bind.chapter.text = s
            }
            when(mode){
                Mode.BookMark -> {
                    bind.showSpecial.text = Tools.bookMarkNumS(it.bookMarkNum?:0L)
                }
                Mode.Duration -> {
                    bind.showSpecial.text = Tools.longBookTimeS(it.long?:0L)
                }
                Mode.PerPageTime -> {
                    bind.showSpecial.text = Tools.longPerPageTimeS(it.perPageTime?:0L)
                }
            }
        }

    }


    fun switchModeTo(mode: Mode){
        this.mode = mode
        notifyItemRangeChanged(0, itemCount)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val bind = StatisticBookItemBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(bind)
    }
    companion object{
        val DIFF = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BookDataUI>(){
            override fun areItemsTheSame(oldItem: BookDataUI, newItem: BookDataUI): Boolean {
                return oldItem.book?.bookId == newItem.book?.bookId
            }

            override fun areContentsTheSame(oldItem: BookDataUI, newItem: BookDataUI): Boolean {
                return oldItem == newItem
            }
        }
    }
}