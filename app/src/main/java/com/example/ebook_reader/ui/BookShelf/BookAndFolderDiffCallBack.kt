package com.example.ebook_reader.ui.BookShelf

import androidx.recyclerview.widget.DiffUtil

class BookAndFolderDiffCallBack: DiffUtil.ItemCallback<BookAndFolderItem>() {
    override fun areItemsTheSame(oldItem: BookAndFolderItem, newItem: BookAndFolderItem): Boolean {
        return when {
            oldItem is BookView && newItem is BookView ->
                return oldItem.uid == newItem.uid
            oldItem is FolderView && newItem is FolderView ->
                return oldItem.uid == newItem.uid
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: BookAndFolderItem, newItem: BookAndFolderItem): Boolean {
        return oldItem == newItem
    }
}

class FolderDiffCallBack: DiffUtil.ItemCallback<FolderView>() {
    override fun areItemsTheSame(oldItem: FolderView, newItem: FolderView): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: FolderView, newItem: FolderView): Boolean {
        return oldItem == newItem
    }
}