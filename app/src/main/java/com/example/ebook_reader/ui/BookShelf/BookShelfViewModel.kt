package com.example.ebook_reader.ui.BookShelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class BookView(
    val uid: String,
    val name: String,
    val currentChapter: Int,
    val currentPage: Int,
    val totalChapter: Int,
    val cover: String,
)

class BookShelfViewModel : ViewModel() {
    private val _books = MutableStateFlow<List<BookView>>(emptyList())
    val books: StateFlow<List<BookView>> = _books

    init {
        loadBooks()
    }
    private fun loadBooks() {
        viewModelScope.launch {
            // 示例数据，实际应从数据库或网络获取
            var mockBooks = listOf(
                BookView(
                    uid = "qwerty",
                    name = "Android开发艺术探索",
                    currentChapter = 5,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = "qwesdsarty",
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
            )
            _books.value = mockBooks
            delay(5000)
            mockBooks = listOf(
                BookView(
                    uid = "qwerty",
                    name = "Android开发艺术探索",
                    currentChapter = 5,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
            )
            _books.value = mockBooks

        }

    }
    fun addBook(book: BookView) {
        _books.value = _books.value + book
    }
    fun handleBookClick(book: BookView) {
        // 处理点击事件

    }

}