package com.example.ebook_reader.Repository.BookShelf

import com.example.ebook_reader.entities.UIFolderView

data class FolderAdapterUIState(
    val folder: UIFolderView,
    val isSelected: Boolean
)
