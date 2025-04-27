package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetBookMarks
import com.example.ebook_reader.Repository.ReadingBook.BookMarkUI

class TXTBookMarkSource(
    private val index: GetBookMarks,
    private val jump: JumpSolve,
):PagingSource<Long, BookMarkUI>() {
    override fun getRefreshKey(state: PagingState<Long, BookMarkUI>): Long? {
        Log.d("TBMS getRefreshKey", "计划中 要 canJump ${jump.canJump} jump ${jump.toPosition}")
        if (jump.canJump) {
            Log.d("TBMS getRefreshKey", "跳转到位置 ${jump.toPosition}")
            return jump.toPosition
        }
        return index.getCurrentPos()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, BookMarkUI> {
        return try {
            var pageNumber = params.key ?: 0
            var transfer = index.getBookMarks(pageNumber.toLong())
            if(transfer.bookMark == null){
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = transfer.preId,
                    nextKey = transfer.nextId
                )
            }
            LoadResult.Page(
                data = listOf(BookMarkUI(
                    order = transfer.order,
                    title = transfer.title,
                    content = transfer.bookMark.content
                )),
                prevKey = transfer.preId,
                nextKey = transfer.nextId
            )
        }catch (e: Exception){
            Log.d("TBMS load","发生错误 ${e.message}")
            LoadResult.Error(e)
        }
    }


}