package com.example.ebook_reader.entities

data class UIFolderView(
    val folder: FolderView,
    val booksNum: Long,
): BookAndFolderItem()