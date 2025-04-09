package com.example.ebook_reader.ui.BookShelf

import androidx.recyclerview.widget.DiffUtil
import com.example.ebook_reader.Repository.BookShelf.BookAdapterUIState
import com.example.ebook_reader.Repository.BookShelf.FolderAdapterUIState
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.UIFolderView

class BookAndFolderDiffCallBack: DiffUtil.ItemCallback<BookAdapterUIState>() {
    override fun areItemsTheSame(oldItem: BookAdapterUIState, newItem: BookAdapterUIState): Boolean {
        return when {
            oldItem.item is BookView && newItem.item is BookView  ->
                return oldItem.item.bookId == newItem.item.bookId
            oldItem.item is UIFolderView && newItem.item is UIFolderView ->
                return oldItem.item.folder.folderId == newItem.item.folder.folderId
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: BookAdapterUIState, newItem: BookAdapterUIState): Boolean {
        return oldItem == newItem
    }
}

class FolderDiffCallBack: DiffUtil.ItemCallback<FolderAdapterUIState>() {
    override fun areItemsTheSame(oldItem: FolderAdapterUIState, newItem: FolderAdapterUIState): Boolean {
        return oldItem.folder.folder.folderId == newItem.folder.folder.folderId
    }

    override fun areContentsTheSame(oldItem: FolderAdapterUIState, newItem: FolderAdapterUIState): Boolean {
        return oldItem == newItem
    }
}