package com.example.ebook_reader.DAO

import androidx.room.Dao
import androidx.room.Query
import com.example.ebook_reader.Repository.DataStatistic.BookMarksNum
import com.example.ebook_reader.Repository.DataStatistic.Duration
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.flow.Flow

@Dao
interface DataInfoDao {

    //获取总时长
    @Query("Select bookId,SUM(duration) as duration From BookRecord where timeOfRecord >= :nowTime  " +
            "Group By bookId")
    fun sumBookRecord(nowTime: Long): Flow<List<Duration>>


    @Query("Select bookId, count(1)as num From BookMarksInfo Where timeOfRecord >= :nowTime group by bookId")
    fun sumBookMark(nowTime: Long): Flow<List<BookMarksNum>>

    @Query("select * from PageDeduplication where timeOfRecord >= :nowTime")
    fun selectPageDeduplication(nowTime: Long): Flow<List<PageDeduplication>>

    @Query("SELECT * From BooksInfo where bookId = :bookId")
    fun selectBook(bookId: Long): Flow<BookView?>

}