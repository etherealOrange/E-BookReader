package com.example.ebook_reader.ui.ReadingBook

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.entities.BookView

abstract class BaseReadingViewModel
    (private val savedStateHandle: SavedStateHandle): ViewModel() {

    private val _book = savedStateHandle.get<BookView>("book")
    val book get() = _book?: BookView(
        bookId = -1L,
        title = "没有书本",
        bookUrl = "",
        bookType = BookType.TXT,
        coverUrl = "",
        currentPage = 0L,
        totalPages = 0L,
        folderId = null,
    )
}