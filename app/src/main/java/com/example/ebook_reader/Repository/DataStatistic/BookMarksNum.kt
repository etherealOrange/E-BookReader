package com.example.ebook_reader.Repository.DataStatistic

import androidx.room.Entity

@Entity
data class BookMarksNum (
    val bookId: Long,
    val num: Long,
)