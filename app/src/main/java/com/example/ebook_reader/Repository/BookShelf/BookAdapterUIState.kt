package com.example.ebook_reader.Repository.BookShelf

import com.example.ebook_reader.entities.BookAndFolderItem

data class BookAdapterUIState(
    val item: BookAndFolderItem,
    val isSelected: Boolean
)
