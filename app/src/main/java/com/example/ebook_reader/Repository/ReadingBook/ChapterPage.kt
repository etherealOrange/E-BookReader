package com.example.ebook_reader.Repository.ReadingBook

data class ChapterPage(
    val title: String,
    val order: Long,
    val partOrder:Long,
    val content: String,
)
