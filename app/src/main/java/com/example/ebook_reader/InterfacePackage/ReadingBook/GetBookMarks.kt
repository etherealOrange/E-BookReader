package com.example.ebook_reader.InterfacePackage.ReadingBook

import com.example.ebook_reader.Repository.ReadingBook.BookMarkUI
import com.example.ebook_reader.Repository.ReadingBook.transferBookMark

interface GetBookMarks: GetCurrentPos {
    suspend fun getBookMarks(order : Long): transferBookMark
}