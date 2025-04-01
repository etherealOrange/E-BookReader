package com.example.ebook_reader.ui.BookShelf

import androidx.recyclerview.widget.DiffUtil
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.FolderView

class BookAndFolderDiffCallBack: DiffUtil.ItemCallback<BookAndFolderItem>() {
    override fun areItemsTheSame(oldItem: BookAndFolderItem, newItem: BookAndFolderItem): Boolean {
        return when {
            oldItem is BookView && newItem is BookView ->
                return oldItem.bookId == newItem.bookId
            oldItem is FolderView && newItem is FolderView ->
                return oldItem.folderId == newItem.folderId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: BookAndFolderItem, newItem: BookAndFolderItem): Boolean {
        return oldItem == newItem
    }
}

class FolderDiffCallBack: DiffUtil.ItemCallback<FolderView>() {
    override fun areItemsTheSame(oldItem: FolderView, newItem: FolderView): Boolean {
        return oldItem.folderId == newItem.folderId
    }

    override fun areContentsTheSame(oldItem: FolderView, newItem: FolderView): Boolean {
        return oldItem == newItem
    }
}