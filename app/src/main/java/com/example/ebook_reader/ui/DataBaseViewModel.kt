package com.example.ebook_reader.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Repository.DataStatistic.DataRepository
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn

abstract class DataBaseViewModel(
    private val Repo: DataRepository
)
    : ViewModel() {
    //总时长
    val allDuration = Repo.allDuration
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(500),
            initialValue = emptyList()
        )
    //总书签数
    val allMarkNum = Repo.allBookMarkNum
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(500),
            initialValue = emptyList()
        )
    //总页数 分散
    val allPagesLong = Repo.allPageLong
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(500),
            initialValue = emptyMap()
        )
    val allPerPageTime = Repo.perPageTime
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(500),
            initialValue = emptyList()
        )
    protected val empty = BookDataUI(
        book = null,
        long = null,
        bookMarkNum = null,
        perPageTime = null
    )

}