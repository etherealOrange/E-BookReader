package com.example.ebook_reader.Repository.DataStatistic

import android.util.Log
import com.example.ebook_reader.DAO.DataInfoDao
import com.example.ebook_reader.Gap
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DataRepository @Inject constructor(
    private val dao: DataInfoDao
) {
    //默认前一个月 不使用范围
    private var startTime: Long = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    private var endTime: Long = 0L
    fun useRangeTime(gap: Gap) {
        if(gap.startY ==startTime && gap.endY==endTime){
            return
        }
        startTime = gap.startY
        endTime = gap.endY
        _allDuration.update { dao.sumBookRecordToEnd(startTime,endTime) }
        _allPageDedu.update { dao.selectPageDeduplicationToEnd(startTime,endTime) }
        _allBookMarkNum.update { dao.sumBookMarkToEnd(startTime,endTime) }
    }
    fun noUseRangeTime(start: Long) {
        if(start == startTime){
            return
        }
        startTime = start
        _allDuration.update { dao.sumBookRecord(startTime) }
        _allPageDedu.update { dao.selectPageDeduplication(startTime) }
        _allBookMarkNum.update { dao.sumBookMark(startTime) }
    }

    private fun Flow<Long?>.minWith(other:Flow<Long?>):Flow<Long?>{
        return combine(other) { a,b->
           when{
               a==null -> b
               b==null -> a
               else -> minOf(a,b)
           }
        }
    }

    private var _earliestTime = dao.selectEarliestTimeOfRecord()
        .minWith(dao.selectEarliestTimeOfPageDedup())
        .minWith(dao.selectEarliestTimeOfBookMark())
    val earliestTime get()= _earliestTime






    //数据源
    private var _allDuration = MutableStateFlow(dao.sumBookRecord(startTime))
    val allDuration = _allDuration.flatMapLatest { it }
    private var _allBookMarkNum = MutableStateFlow(dao.sumBookMark(startTime))
    val allBookMarkNum = _allBookMarkNum.flatMapLatest { it }
    private var _allPageDedu = MutableStateFlow(dao.selectPageDeduplication(startTime))
    val allPageLong = _allPageDedu.flatMapLatest {
        it.map {
            it.groupBy { it.bookId }
                .mapValues { (_,group)->
                    var end = -1L
                    var sum =0L
                    group.groupBy { it.pageStart }.mapValues { (_,group)->group.maxBy { it.pageEnd } }
                        .values
                        .sortedBy { it.pageStart }
                        .forEach {
                            if(sum==0L){
                                sum+=it.pageEnd-it.pageStart
                                end= it.pageEnd
                            }else if(it.pageEnd-end>0){
                                if(it.pageStart <= end+1){
                                    sum +=it.pageEnd-end
                                    end = it.pageEnd
                                }else {
                                    sum += it.pageEnd - it.pageStart
                                    end = it.pageEnd
                                }
                            }
                        }
                    sum
                }
        }
    }
    val perPageTime: Flow<List<PerPageTime>> = allDuration.combine(allPageLong) {
            du,pages->
        du.map {
                now->
            val pages = pages.getOrElse(now.bookId){null}
            if(pages==null || pages==0L){
                PerPageTime(
                    bookId = now.bookId,
                    time = 0L
                )
            }else{
                PerPageTime(
                    bookId = now.bookId,
                    time = (now.duration.toDouble()/pages).toLong()
                )
            }

        }
    }

    fun getBookView(bookId: Long): Flow<BookView?> = dao.selectBook(bookId)

    fun getBookViews(bookIds:List<Long>): Flow<List<BookView>> = dao.selectBooks(bookIds)





}