package com.example.ebook_reader.Repository.BookShelf

import com.example.ebook_reader.entities.ChapterView

data class ChapterModified (
    val chapters: List<ChapterView>,
    val totalPages: Int
)