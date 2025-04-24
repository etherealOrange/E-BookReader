package com.example.ebook_reader.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.ebook_reader.Repository.ReadingBook.BookMarkSrc
import com.example.ebook_reader.entities.BookMarkView
import com.example.ebook_reader.entities.BookRecord
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.PageDeduplication
import com.example.ebook_reader.ui.TimeRecorder.BookRecorder
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterInfoDao {
    //插入 多个 章节信息
    @Insert
    suspend fun insertChapters(chapters: List<ChapterView>)
    //更新书本信息读到第几章了
    @Query("UPDATE BooksInfo SET currentPage = :chapterOrder Where bookId = :bookId")
    suspend fun updateCurrentPage(bookId: Long,chapterOrder: Long)

    //bookId 全部章节
    @Query("SELECT * FROM ChaptersInfo Where bookId = :bookId ORDER BY chapterOrder ASC")
    suspend fun selectAllChapterFromBookId(bookId: Long):List<ChapterView>

    //根据bookId 获取已经阅读的章节范围
    @Query("SELECT * FROM PageDeduplication Where bookId = :bookId ORDER BY pageStart ASC")
    suspend fun selectPageDeduplicationFromBookId(bookId: Long):List<PageDeduplication>

    //插入阅读记录
    @Insert
    suspend fun insertBookRecord(bookRecord: BookRecord)

    //插入阅读书页记录
    @Insert
    suspend fun insertPageDeduplication(pageDeduplication: PageDeduplication)
    //删除多余的书页记录
    @Query("Delete FROM PageDeduplication Where bookId = :bookId and pageStart = :pageStart")
    suspend fun deletePageDeduplication(bookId: Long, pageStart: Long)
    //更新
    @Query("UPDATE PageDeduplication SET pageEnd = :pageEnd Where bookId = :bookId and pageStart = :pageStart")
    suspend fun updatePageDeduplication(bookId: Long, pageStart: Long, pageEnd: Long)


    //实时更新的现在书签信息
    @Query("SELECT * FROM BookMarksInfo Where bookId = :bookId And chapterOrder =:order  ORDER BY chapterOrder ASC")
    suspend fun currentBookMark(bookId: Long,order: Long): BookMarkView?

    //根据bookId 和 chapterOrder 获取章节 和前后的信息
    @Transaction
    suspend fun getBookMarkSrc(bookId: Long, order: Long): BookMarkSrc {
        val bookMark = selectBookMarkFromBookId(bookId, order)
        val preId = preBookMarkFromOrder(bookId, order)
        val nextId = nextBookMarkFromOrder(bookId, order)
        return BookMarkSrc(
            bookMark = bookMark,
            preId = preId,
            nextId = nextId
        )
    }
    //根据bookId 和 Order 获取书签信息
    @Query("SELECT * FROM BookMarksInfo Where bookId = :bookId And chapterOrder = :order ORDER BY chapterOrder ASC")
    suspend fun selectBookMarkFromBookId(bookId: Long,order: Long):BookMarkView?
    //获取 order 书签下一条 书签的order
    @Query("SELECT chapterOrder FROM BookMarksInfo Where bookId = :bookId And chapterOrder > :order ORDER BY chapterOrder ASC LIMIT 1")
    suspend fun nextBookMarkFromOrder(bookId: Long,order: Long):Long?
    //获取 order 书签上一条 书签的order
    @Query("SELECT chapterOrder FROM BookMarksInfo Where bookId = :bookId And chapterOrder < :order ORDER BY chapterOrder DESC LIMIT 1")
    suspend fun preBookMarkFromOrder(bookId: Long,order: Long):Long?

    //根据bookId 和 chapterOrder 查看书签是否存在
    @Query("SELECT count(1) FROM BookMarksInfo Where bookId = :bookId And chapterOrder = :chapterOrder")
    suspend fun selectBookMarkIsExist(bookId: Long, chapterOrder: Long):Int
    //插入书签信息
    @Insert
    suspend fun insertBookMark(bookMark: BookMarkView)
    //删除书签信息
    @Query("DELETE FROM BookMarksInfo Where bookId = :bookId and chapterOrder = :chapterOrder")
    suspend fun deleteBookMark(bookId: Long, chapterOrder: Long)
    //更新书签信息
    @Query("UPDATE BookMarksInfo SET content = :content Where bookId = :bookId and chapterOrder = :chapterOrder")
    suspend fun updateBookMark(bookId: Long, chapterOrder: Long, content: String)

}