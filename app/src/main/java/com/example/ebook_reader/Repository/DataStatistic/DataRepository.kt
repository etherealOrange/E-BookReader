package com.example.ebook_reader.Repository.DataStatistic

import com.example.ebook_reader.DAO.DataInfoDao
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class DataRepository @Inject constructor(
    private val dao: DataInfoDao
) {
    val currentTime = LocalDate.now()
        .withDayOfMonth(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    val longTimeBookId = MutableStateFlow<Long?>(null)
    val longPerPageTimeBookId = MutableStateFlow<Long?>(null)
    val bookMarksMostBookId = MutableStateFlow<Long?>(null)

    val longTimeBook = longTimeBookId.filterNotNull().flatMapLatest {
        dao.selectBook(it)
    }
    val longPerPageTimeBook = longPerPageTimeBookId.filterNotNull().flatMapLatest {
        dao.selectBook(it)
    }
    val bookMarksMostBook = bookMarksMostBookId.filterNotNull().flatMapLatest {
        dao.selectBook(it)
    }


    val allDuration = dao.sumBookRecord(currentTime)
    val allBookMarkNum = dao.sumBookMark(currentTime)
    val allPageDedu = dao.selectPageDeduplication(currentTime)

    fun updateLongTimeBookId(id: Long) {
        longTimeBookId.update { id }
    }
    fun updateLongPerPageTimeBookId(id: Long) {
        longPerPageTimeBookId.update { id }
    }
    fun updateBookMarksMostBookId(id: Long) {
        bookMarksMostBookId.update { id }
    }





}