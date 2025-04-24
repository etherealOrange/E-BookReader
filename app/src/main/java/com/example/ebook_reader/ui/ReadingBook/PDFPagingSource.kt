package com.example.ebook_reader.ui.ReadingBook

import android.graphics.pdf.PdfRenderer
import androidx.paging.PagingSource
import androidx.paging.PagingState

class PDFPagingSource(
    private val pdf:PdfRenderer?
):PagingSource<Long,Long>() {
    override fun getRefreshKey(state: PagingState<Long, Long>): Long? {
        return state.anchorPosition?.toLong()
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Long> {
        val page = params.key ?: 0
        if(pdf==null)return LoadResult.Page(
            data = listOf(),
            prevKey = null,
            nextKey = null
        )
        return LoadResult.Page(
            data = listOf(page),
            prevKey = if(page>0) page - 1 else null,
            nextKey = if(page < pdf.pageCount-1) page+1 else null
        )
    }
}