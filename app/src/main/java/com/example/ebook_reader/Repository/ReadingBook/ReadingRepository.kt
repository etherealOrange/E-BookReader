package com.example.ebook_reader.Repository.ReadingBook

import android.util.Log
import com.example.ebook_reader.DAO.ChapterInfoDao
import com.example.ebook_reader.entities.BookMarkView
import com.example.ebook_reader.entities.BookRecord
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.PageDeduplication
import com.example.ebook_reader.ui.TimeRecorder.BookRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepository@Inject constructor (
    private val chapterDao: ChapterInfoDao
    ) {


    suspend fun getPagesDeduplication(bookId: Long): List<PageDeduplication> {
        return chapterDao.selectPageDeduplicationFromBookId(bookId)
    }

    /**
     * 插入阅读记录
     */
    fun insertPageDeduplication(pageDeduplication: List<PageDeduplication>) {
        CoroutineScope(Dispatchers.IO).launch {
            if(pageDeduplication.isEmpty()) return@launch
            val bookId = pageDeduplication[0].bookId
            val orgin = getPagesDeduplication(bookId).map { it.pageStart }
            val needRm = orgin.filterNot { it in pageDeduplication.map { it.pageStart } }
            needRm.forEach {
                chapterDao.deletePageDeduplication(bookId, it)
            }
            val insertRD = pageDeduplication.filterNot { it.pageStart in orgin }
            insertRD.forEach {
                chapterDao.insertPageDeduplication(
                    PageDeduplication(
                        bookId = it.bookId,
                        pageStart = it.pageStart,
                        pageEnd = it.pageEnd,
                    )
                )
            }
            pageDeduplication
                .filterNot { it.pageStart in insertRD.map { it.pageStart }}
                .forEach {
                chapterDao.updatePageDeduplication(bookId, it.pageStart, it.pageEnd)
            }
        }
    }

    /**
     * 插入已读页数
     */
    fun insertBookRecord(bookRecord: BookRecord) {
        CoroutineScope(Dispatchers.IO).launch {
            chapterDao.insertBookRecord(bookRecord)
        }
    }

    /**
     * 更新读到哪里了
     */
    fun updateCurrentPage(bookId: Long, chapterOrder: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            chapterDao.updateCurrentPage(bookId, chapterOrder)
        }
    }
    /**
     * 获取当前章节的书签信息
     */
    private var _currentBookMark: MutableStateFlow<BookMarkView?> = MutableStateFlow(null)
    val currentBookMark: StateFlow<BookMarkView?> get() = _currentBookMark
    //获取当前章节的书签信息
    suspend fun setCurrentBookMark(bookId: Long,order: Long) {
        _currentBookMark.value = chapterDao.currentBookMark(bookId,order)
    }
    /**
     * 开启 切换 获取书签信息
     */
    suspend fun getBookMarkSrc(bookId: Long, order: Long): BookMarkSrc {
        return chapterDao.getBookMarkSrc(bookId,order)
    }

    /**
     * 获取书签是否存在
     */
    suspend fun isBookMarkExist(bookId: Long, chapterOrder: Long): Boolean {
        val count = chapterDao.selectBookMarkIsExist(bookId, chapterOrder)
        Log.d("RR isBookMarkExist","书签是否存在 $count")
        return count > 0
    }

    /**
     * 获取章节列表
     */
    suspend fun getChaptersFromBookId(bookId: Long): List<ChapterView> {
        val list = chapterDao.selectAllChapterFromBookId(bookId = bookId)
        Log.d("RR getChaptersFromBookId","列表长度: ${list.size} \n")
        return list
    }
    /**
     * 插入书签信息
     */
    suspend fun insertBookMark(bookMark: BookMarkView) {
        Log.d("RR insertBookMark","插入书签 bookId: ${bookMark.bookId} chapterOrder: ${bookMark.chapterOrder} ")
        chapterDao.insertBookMark(bookMark)
    }
    /**
     * 删除书签信息
     */
    suspend fun deleteBookMark(bookId: Long, chapterOrder: Long) {
        Log.d("RR deleteBookMark","删除书签 bookId: $bookId chapterOrder: $chapterOrder")
        chapterDao.deleteBookMark(bookId, chapterOrder)
    }
    /**
     * 更新书签信息
     */
    suspend fun updateBookMark(bookId: Long, chapterOrder: Long, content: String) {
        Log.d("RR updateBookMark","更新书签 bookId: $bookId chapterOrder: $chapterOrder ")
        chapterDao.updateBookMark(bookId, chapterOrder, content)
    }


}