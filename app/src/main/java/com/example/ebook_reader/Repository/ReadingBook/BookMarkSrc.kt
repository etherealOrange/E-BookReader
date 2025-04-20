package com.example.ebook_reader.Repository.ReadingBook

import com.example.ebook_reader.entities.BookMarkView

data class BookMarkSrc(
    val bookMark: BookMarkView?,
    val preId: Long?,
    val nextId: Long?,
)
data class transferBookMark(
    val bookMark: BookMarkView?,
    val order: Long,
    val title:String,
    val preId: Long?,
    val nextId: Long?,
)
