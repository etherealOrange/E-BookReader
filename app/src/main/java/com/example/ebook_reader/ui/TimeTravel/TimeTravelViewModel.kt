package com.example.ebook_reader.ui.TimeTravel

import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Gap
import com.example.ebook_reader.InterfacePackage.TimeTravel.UpdateGap
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Repository.DataStatistic.DataRepository
import com.example.ebook_reader.Tools
import com.example.ebook_reader.ui.DataBaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimeTravelViewModel @Inject constructor(
    private val Repo: DataRepository
) : DataBaseViewModel(Repo)
, UpdateGap
{
    @OptIn(ExperimentalCoroutinesApi::class)
    private val longTimeBooks = allDuration
        .flatMapLatest {
            if (it.isEmpty()) {
                flowOf(listOf(null))
            } else {
//                it.forEach {
//                    Log.d("TTVM","bookId: ${it.bookId} duration: ${it.duration} ")
//                }
                val du = it.sortedByDescending { it.duration }.take(10)
                Repo.getBookViews(du.map { it.bookId }).map {
                    if (it.isEmpty()) {
                        listOf(null)
                    } else {
                       it.map{
                            BookDataUI(
                                book = it,
                                long = du.firstOrNull { i -> i.bookId == it.bookId }?.duration,
                                bookMarkNum = null,
                                perPageTime = null
                            )
                        }.sortedByDescending { it.long }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(500),
            initialValue = listOf(null)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val longPerPageTimeBook = allPerPageTime
        .flatMapLatest {
            if(it.isEmpty()){
                flowOf(listOf(null))
            }
            else{
                val per = it.sortedByDescending { it.time }.take(10)
                Repo.getBookViews(per.map{it.bookId}).map{
                    if(it.isEmpty()){
                        listOf(null)
                    }else{
                        it.map{
                            BookDataUI(
                                book = it,
                                long = null,
                                bookMarkNum = null,
                                perPageTime = per.firstOrNull { i -> i.bookId == it.bookId }?.time
                            )
                        }.sortedByDescending { it.perPageTime }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(500),
            initialValue = listOf(null)
        )
    @OptIn(ExperimentalCoroutinesApi::class)
    private val bookMarksMostBook = allMarkNum.flatMapLatest {
        if(it.isEmpty()){
            flowOf(listOf(null))
        }else{
            val mark = it.sortedByDescending { it.num }.take(10)
            Repo.getBookViews(mark.map{it.bookId}).map{
                if(it.isEmpty()){
                    listOf(null)
                }else{
                    it.map{
                        BookDataUI(
                            book = it,
                            long = null,
                            bookMarkNum = mark.firstOrNull { i -> i.bookId == it.bookId }?.num,
                            perPageTime = null
                        )
                    }.sortedByDescending { it.bookMarkNum }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(500),
        initialValue = listOf(null)
    )

    private val _currentRangeBooks = MutableStateFlow(longTimeBooks)
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentRangeBooks = _currentRangeBooks.flatMapLatest { it }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(500),
        initialValue = listOf(null)
    )



    val earliestTime = Repo.earliestTime.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(500),
        initialValue = null
    )
    private val _yearList = MutableStateFlow<List<Int>>(listOf(1))
    val yearList = _yearList.asStateFlow()

    private val _currentGap = MutableStateFlow<Gap>(Tools.makeYearGap(0))
    val currentGap = _currentGap.asStateFlow()
    override fun updateGap(pos: Int){
        _currentGap.value = Tools.makeYearGap(pos)
    }

    private val _currentMode = MutableStateFlow<Mode>(Mode.Duration)
    val currentMode = _currentMode.asStateFlow()

    fun updateCurrentMode(mode: Mode){
        _currentMode.value = mode
        _currentRangeBooks.value = when(mode){
            Mode.Duration -> longTimeBooks
            Mode.BookMark -> bookMarksMostBook
            Mode.PerPageTime -> longPerPageTimeBook
        }
    }



    enum class Mode{
        BookMark,
        PerPageTime,
        Duration
    }



    init {
        viewModelScope.launch {
            launch {
                earliestTime.collectLatest{
                    if(it==null){
                        _yearList.value = listOf(0)
                    }else{
                        var stYear = Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .year
                        Log.d("TTVM","earliest year: $stYear")
                        val yearLen = LocalDate.now().year - stYear
                        if(yearLen<0){
                            _yearList.value = listOf(0)
                        }
                        _yearList.value = (0..yearLen).toList()
                    }
                }
            }
            launch {
                currentGap.collectLatest {
                    Repo.useRangeTime(it)
                }
            }

        }

    }

}
