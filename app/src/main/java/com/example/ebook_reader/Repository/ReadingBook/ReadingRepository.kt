package com.example.ebook_reader.Repository.ReadingBook

import android.util.Log
import com.example.ebook_reader.DAO.BooksAndFoldersInfoDao
import com.example.ebook_reader.DAO.ChapterInfoDao
import com.example.ebook_reader.entities.ChapterView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepository@Inject constructor (
    private val chapterDao: ChapterInfoDao
    ) {

    /**
     * 获取章节列表
     */
    suspend fun getChaptersFromBookId(bookId: Long): List<ChapterView> {
        val list = chapterDao.selectAllChapterFromBookId(bookId = bookId)
        Log.d("RR getChaptersFromBookId","列表长度: ${list.size} \n")
        return list
    }

}