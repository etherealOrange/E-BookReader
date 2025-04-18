package com.example.ebook_reader.Repository.ReadingBook

data class ChapterIndex(
    val title: String,
    val chapterOrder: Long,
    val startByte: Long,
    val endByte: Long,
    val partOrder: Long
)
