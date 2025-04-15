package com.example.ebook_reader.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ebook_reader.entities.ChapterView
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterInfoDao {
    //插入 多个 章节信息
    @Insert
    suspend fun insertChapters(chapters: List<ChapterView>)

    //bookId 全部章节
    @Query("SELECT * FROM ChaptersInfo Where bookId = :bookId")
    suspend fun selectAllChapterFromBookId(bookId: Long):List<ChapterView>


}