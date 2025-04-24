package com.example.ebook_reader.ui.DataStatistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Repository.DataStatistic.DataRepository
import com.example.ebook_reader.Repository.DataStatistic.PerPageTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStatisticViewModel @Inject constructor(
    private val Repo: DataRepository

) : ViewModel() {
    //总时长
    val allDuration = Repo.allDuration
        .stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )
    //总书签数
    val allMarkNum = Repo.allBookMarkNum.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )
    //总页数 分散
    val allPageDedu = Repo.allPageDedu.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )
    //每本书 读的页数
    val allPagesLong = MutableStateFlow<Map<Long, Long>>(emptyMap())
    //每本书 每页时间
    val allPerPageTime = allDuration.combine(allPagesLong) {
            du,pages->
        du.map {
                now->
            PerPageTime(
                bookId = now.bookId,
                time = (now.duration/ pages.getOrElse(now.bookId){1L}).toLong()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )


    val longTimeBook = Repo.longTimeBook.map {
        book->
        if(book==null) return@map null
        BookDataUI(
            book = book,
            long = allDuration.value.first { it.bookId==book.bookId }.duration,
            bookMarkNum = null,
            perPageTime = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = null
    )
    val longPerPageTimeBook = Repo.longPerPageTimeBook.map {
        book->
        if(book==null) return@map null
        BookDataUI(
            book = book,
            long = null,
            bookMarkNum = null,
            perPageTime = allPerPageTime.value.first { it.bookId==book.bookId }.time
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = null
    )
    val bookMarksMostBook = Repo.bookMarksMostBook.map {
        book->
        if(book==null) return@map null
        BookDataUI(
            book = book,
            long = null,
            bookMarkNum = allMarkNum.value.first { it.bookId==book.bookId }.num,
            perPageTime = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = null
    )


    val totalTime = MutableStateFlow<Long>(0L)
    val totalPages = MutableStateFlow<Long>(0L)
    val totalPerPageTime = totalTime.combine(totalPages) {
        du,lo->
        if(totalPages.value==0L) 0L
        else (totalTime.value/totalPages.value).toLong()
    }
    val totalBookMarks = MutableStateFlow<Long>(0L)



    init {
        viewModelScope.launch {
            launch {
                allPageDedu.collectLatest {
                    val ls = mutableMapOf<Long, Long>()
                    it.groupBy { it.bookId }.forEach {
                        var sum = 0L
                        it.value.forEach {
                            sum += it.pageEnd - it.pageStart
                        }
                        ls.put(it.key, sum)
                    }
                    allPagesLong.update { ls }
                }
            }
            launch {
                allDuration.collectLatest {
                    totalTime.value = it.sumOf { it.duration }
                }
            }
            launch {
                allPagesLong.collectLatest {
                    var sum = 0L
                    it.forEach { sum += it.value }
                    totalPages.value = sum
                }
            }
            launch {
                allMarkNum.collectLatest {
                    totalBookMarks.value = it.sumOf { it.num }
                }
            }
            launch {
                allDuration.collectLatest {
                    it.maxByOrNull { it.duration }?.let { that ->
                        Repo.updateLongTimeBookId(that.bookId)
                    }
                }
            }
            launch {
                allPerPageTime.collectLatest {
                    it.maxByOrNull { it.time }?.let { that ->
                        Repo.updateLongPerPageTimeBookId(that.bookId)

                    }
                }
            }
            launch {
                allMarkNum.collectLatest {
                    it.maxByOrNull { it.num }?.let { that ->
                        Repo.updateBookMarksMostBookId(that.bookId)
                    }
                }

            }
        }
    }



}