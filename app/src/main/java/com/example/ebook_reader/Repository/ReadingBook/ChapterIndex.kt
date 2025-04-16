package com.example.ebook_reader.Repository.ReadingBook

data class ChapterIndex(
    val title: String,
    val chapterOrder: Long,
    val startLine: Long,
    val endLint: Long
)
