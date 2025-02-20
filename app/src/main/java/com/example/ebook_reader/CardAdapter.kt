package com.example.ebook_reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(private val items: List<String>) : RecyclerView.Adapter<CardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.findViewById(R.id.BookTitle_tv_cardview)
        val chapterProgress: TextView = itemView.findViewById(R.id.ChapterProgress_tv_cardview)
        val readingProgress: ProgressBar = itemView.findViewById(R.id.ReadingProgress_pb_cardview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bookTitle.text = item
        // Set other views as needed
    }

    override fun getItemCount(): Int {
        return items.size
    }
}