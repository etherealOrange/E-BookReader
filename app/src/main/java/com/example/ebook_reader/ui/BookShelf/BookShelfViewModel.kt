package com.example.ebook_reader.ui.BookShelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
sealed class BookAndFolderItem {}
data class BookView(
    val uid: Long,
    val name: String,
    val currentChapter: Int,
    val currentPage: Int,
    val totalChapter: Int,
    val cover: String,
):BookAndFolderItem()
data class FolderView(
    val uid: Long,
    val name: String,
    val booksNum: Long,
    val cover: String,
):BookAndFolderItem()

//负责数据的 获取 处理 打包 更新 添加 删除
class BookShelfViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<BookAndFolderItem>>(emptyList())
    val items: StateFlow<List<BookAndFolderItem>> = _items

    val folderItem: StateFlow<List<FolderView>> = _items.map { i ->
        i.filterIsInstance<FolderView>()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    init {
        loadBooks()
    }
    private fun loadBooks() {
        viewModelScope.launch {
            // 示例数据，实际应从数据库或网络获取
            var mockBooks = listOf(
                FolderView(
                    uid = 1,
                    name = "Android开发",
                    booksNum = 5,
                    cover = "https://img"
                ),
                BookView(
                    uid = 2,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 3,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 4,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 5,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 6,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 11,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),BookView(
                    uid = 12,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 13,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
                BookView(
                    uid = 14,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
            )
            _items.value = mockBooks
            delay(5000)
            mockBooks = listOf(
                FolderView(
                    uid = 2,
                    name = "Android开发",
                    booksNum = 5,
                    cover = "https://img"
                ),
                BookView(
                    uid = 7,
                    name = "Android开发艺术探索2",
                    currentChapter = 1,
                    currentPage = 14,
                    totalChapter = 20,
                    cover = "https://img"
                ),
            )
            _items.value = mockBooks

        }

    }
    fun addBook(book: BookView) {

    }
    fun handleBookClick(book: BookView) {
        // 处理点击事件

    }

}