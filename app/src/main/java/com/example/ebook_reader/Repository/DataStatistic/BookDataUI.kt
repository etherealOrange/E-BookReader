package com.example.ebook_reader.Repository.DataStatistic

import com.example.ebook_reader.entities.BookView

data class BookDataUI (
    val book: BookView?,
    val long: Long?,
    val bookMarkNum: Long?,
    val perPageTime: Long?,
)

