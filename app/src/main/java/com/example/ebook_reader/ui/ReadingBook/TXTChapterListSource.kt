package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import java.lang.Exception

class TXTChapterListSource(
    private val index: GetChapterIndexes
)
    : PagingSource<Int, ChapterIndex>(){
    override fun getRefreshKey(state: PagingState<Int, ChapterIndex>): Int? {
        Log.d("TCLS getRefreshKey", state.anchorPosition.toString())
        return ( (state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChapterIndex> {
        return try {
            val pageNumber = params.key ?: 0
            val cacheNum = 10L
            var chapterIndexes = index.getChapterIndexes((pageNumber*cacheNum).toLong(),cacheNum)
            if(chapterIndexes.isEmpty()){
                chapterIndexes = listOf(ChapterIndex(
                    title = "书本不存在",
                    chapterOrder = 0,
                    startLine = 0,
                    endLint = 0
                ))
            }
            LoadResult.Page(
                data = chapterIndexes,
                prevKey = if(pageNumber <= 0 ) null else pageNumber -1,
                nextKey = if (chapterIndexes.size < cacheNum) null else pageNumber + 1
            )
        }catch (e: Exception){
            Log.d("TCLS load","发生错误 ${e.message}")
            LoadResult.Error(e)
        }
    }


}