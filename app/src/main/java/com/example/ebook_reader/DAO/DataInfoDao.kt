package com.example.ebook_reader.DAO

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.ebook_reader.Repository.DataStatistic.BookMarksNum
import com.example.ebook_reader.Repository.DataStatistic.Duration
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Dao
interface DataInfoDao {

    //获取数据到现在
    @Query(
        "Select bookId,SUM(duration) as duration From BookRecord where timeOfRecord >= :startTime  " +
            "Group By bookId")
    fun sumBookRecord(startTime: Long): Flow<List<Duration>>

    @Query("Select bookId, count(1)as num From BookMarksInfo Where timeOfRecord >= :startTime group by bookId")
    fun sumBookMark(startTime: Long): Flow<List<BookMarksNum>>

    @Query("select * from PageDeduplication where timeOfRecord >= :startTime")
    fun selectPageDeduplication(startTime: Long): Flow<List<PageDeduplication>>

    //获取一段时间时间
    @Query(
        "Select bookId,SUM(duration) as duration From BookRecord where timeOfRecord >= :startTime And timeOfRecord <= :endTime  " +
                "Group By bookId")
    fun sumBookRecordToEnd(startTime: Long,endTime: Long): Flow<List<Duration>>

    @Query("Select bookId, count(1)as num From BookMarksInfo Where timeOfRecord >= :startTime and timeOfRecord <= :endTime group by bookId")
    fun sumBookMarkToEnd(startTime: Long,endTime: Long): Flow<List<BookMarksNum>>

    @Query("select * from PageDeduplication where timeOfRecord >= :startTime and timeOfRecord <= :endTime")
    fun selectPageDeduplicationToEnd(startTime: Long,endTime: Long): Flow<List<PageDeduplication>>


    @Query("SELECT * From BooksInfo where bookId = :bookId")
    fun selectBook(bookId: Long): Flow<BookView?>

    @Query("select * from BooksInfo where bookId in (:bookIds) ")
    fun selectBooks(bookIds: List<Long>): Flow<List<BookView>>

    //返回最早的记录时间
    @Query("select timeOfRecord from BookRecord order by timeOfRecord limit 1")
    fun selectEarliestTimeOfRecord(): Flow<Long?>
    @Query("select timeOfRecord from PageDeduplication order by timeOfRecord limit 1")
    fun selectEarliestTimeOfPageDedup(): Flow<Long?>
    @Query("select timeOfRecord from BookMarksInfo order by timeOfRecord limit 1")
    fun selectEarliestTimeOfBookMark(): Flow<Long?>

}