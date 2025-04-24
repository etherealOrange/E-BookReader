package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.transformations
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetPDFPage
import com.example.ebook_reader.databinding.FragmentPdfPageBinding

class PDFPageAdapter(private val getPdf: GetPDFPage)
    : PagingDataAdapter<Long, PDFPageAdapter.ViewHolder>(DIFF_CALLBACK) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val bind = holder.bind
        getItem(position)?.let {
            val pic = getPdf.getPage(it)
            Log.d("PPA","正在加载了第${it}页 ")
            if(pic==null)return@let
            bind.imageView.load(pic){
                size(1080, 1920)
            }

            Log.d("PPA","加载了第${it}页 height ${pic.height} width ${pic.width}")
        }.also { Log.d("PPA","结束了") }
    }

    inner class ViewHolder(val bind: FragmentPdfPageBinding): RecyclerView.ViewHolder(bind.root){

    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val bind = FragmentPdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(bind)
    }
    companion object{
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Long>() {
            override fun areItemsTheSame(oldItem: Long, newItem: Long): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: Long, newItem: Long): Boolean {
                return oldItem==newItem
            }
        }
    }

}