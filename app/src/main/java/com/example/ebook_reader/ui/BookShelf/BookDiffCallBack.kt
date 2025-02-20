package com.example.ebook_reader.ui.BookShelf

import androidx.recyclerview.widget.DiffUtil

class BookDiffCallBack: DiffUtil.ItemCallback<BookView>() {
    override fun areItemsTheSame(oldItem: BookView, newItem: BookView): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: BookView, newItem: BookView): Boolean {
        return oldItem == newItem
    }
}