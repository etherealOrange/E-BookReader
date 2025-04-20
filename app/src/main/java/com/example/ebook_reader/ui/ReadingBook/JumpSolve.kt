package com.example.ebook_reader.ui.ReadingBook

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JumpSolve {
    private var _jump: Long = 0L
    private var _canJump: Boolean = false
    val toPosition get() = _jump
    val canJump get() = _canJump
    fun setJump(position: Long) {
        _jump = position
        _canJump = true
    }

    /**
     * 准确使用方法
     * ```
     * chapterListFlow = Pager(
     *                     config = PagingConfig(
     *                         pageSize = 20,
     *                         maxSize = 60,
     *                         prefetchDistance = 10,
     *                         enablePlaceholders = false
     *                     ),
     *                     pagingSourceFactory = { TXTChapterListSource(this@ViewModelOnTXT,listCanJump,listJump)}
     *                 ).also { listJump= 0L ; listCanJump = false }
     * ```
     */
    fun resetJump() {
        _jump = 0L
        _canJump = false
    }
    private val _notify = MutableStateFlow<Boolean>(false)
    val notify: StateFlow<Boolean> get() = _notify
    fun notifyChange() {
        _notify.value = !_notify.value
    }
}