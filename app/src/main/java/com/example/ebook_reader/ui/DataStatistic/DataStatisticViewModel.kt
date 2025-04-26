package com.example.ebook_reader.ui.DataStatistic

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Repository.DataStatistic.DataRepository
import com.example.ebook_reader.ui.DataBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class DataStatisticViewModel @Inject constructor(
    private val Repo: DataRepository

) : DataBaseViewModel(Repo) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val longTimeBook = allDuration
        .flatMapLatest {
            if (it.isEmpty()) {
                flowOf(null)
            } else {
                val du = it.maxByOrNull {
                    it.duration
                }!!
                Repo.getBookView(du.bookId).map {
                    if (it == null) {
                        null
                    } else {
                        BookDataUI(
                            book = it,
                            long = du.duration,
                            bookMarkNum = null,
                            perPageTime = null
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(500),
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val longPerPageTimeBook = allPerPageTime
        .flatMapLatest {
            if(it.isEmpty()){
                flowOf(null)
            }
            else{
                val per = it.maxByOrNull { it.time }!!
                Repo.getBookView(per.bookId).map{
                    if(it==null){
                        null
                    }else{
                        BookDataUI(
                            book = it,
                            long = null,
                            bookMarkNum = null,
                            perPageTime = per.time,
                        )
                    }
                }
            }
        }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(500),
        initialValue = null
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    val bookMarksMostBook = allMarkNum.flatMapLatest {
        if(it.isEmpty()){
            flowOf(null)
        }else{
            val mark = it.maxByOrNull { it.num }!!
            Repo.getBookView(mark.bookId).map{
                if(it==null){
                    null
                }else{
                    BookDataUI(
                        book = it,
                        long = null,
                        bookMarkNum = mark.num,
                        perPageTime = null
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(500),
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

    fun oneMonth(){
        Repo.noUseRangeTime(
            LocalDate.now()
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        )
    }
    fun sixMonth(){
        Repo.noUseRangeTime(
            LocalDate.now()
            .minusMonths(6)
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        )
    }
    fun oneYear(){
        Repo.noUseRangeTime(
            LocalDate.now()
            .minusYears(1)
            .withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        )
    }

    init {
        viewModelScope.launch {
            launch {
                allDuration.collectLatest {
                    totalTime.value = it.sumOf { it.duration }
                }
            }
            launch {
                allPagesLong.collectLatest {
                    totalPages.value = it.values.sumOf { it }
                }
            }
            launch {
                allMarkNum.collectLatest {
                    totalBookMarks.value = it.sumOf { it.num }
                }
            }
        }
    }



}