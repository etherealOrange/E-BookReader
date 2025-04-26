package com.example.ebook_reader.ui.TimeTravel

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.InterfacePackage.TimeTravel.UpdateGap
import com.example.ebook_reader.databinding.YearItemBinding
import java.time.LocalDate

class YearAdapter(
private val update: UpdateGap
): ListAdapter<Int, YearAdapter.ViewHolder>(YearDiffUtil) {

    val nowYear = LocalDate.now().year
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bind = holder.bind
        getItem(position)?.let {
            holder.setItem(it)
            val s = (nowYear-it).toString()
            bind.yearBtn.setText(s)
        }
    }

    inner class ViewHolder(val bind: YearItemBinding): RecyclerView.ViewHolder(bind.root){
        private var item = 0
        fun setItem(year: Int){
            this.item = year
        }
        init {
            bind.yearBtn.setOnClickListener {
                //设置现在显示的年份
                update.updateGap(item)
            }
        }

    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val bind = YearItemBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }


    companion object{
        val YearDiffUtil = object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }
}