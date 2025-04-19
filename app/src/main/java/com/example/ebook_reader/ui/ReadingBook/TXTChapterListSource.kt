package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import java.lang.Exception

class TXTChapterListSource(
    private val index: GetChapterIndexes,
    private val canJump: Boolean,
    private val jump: Long = 0L
)
    : PagingSource<Int, ChapterIndex>(){
    override fun getRefreshKey(state: PagingState<Int, ChapterIndex>): Int? {
        Log.d("TCLS getRefreshKey", "计划中 要 canJump $canJump jump $jump")
        if (canJump) {
            Log.d("TCLS getRefreshKey", "跳转到位置 $jump")
            return jump.toInt()
        }
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChapterIndex> {
        return try {
            var pageNumber = params.key ?: 0
            var cacheNum = 1
            if(pageNumber < 0){
                pageNumber = 0
                cacheNum = cacheNum + pageNumber
            }
            var chapterIndexes = index.getChapterIndexes(pageNumber.toLong(),cacheNum.toLong())
                .sortedBy { it.chapterOrder }
            if(chapterIndexes.isEmpty()){
                chapterIndexes = listOf(ChapterIndex(
                    title = "书本不存在",
                    chapterOrder = 0,
                    startByte = 0,
                    endByte = 0,
                    partOrder = 0,
                ))
            }
            LoadResult.Page(
                data = chapterIndexes,
                prevKey = if(pageNumber <= 0 ) null else pageNumber - cacheNum,
                nextKey = if (chapterIndexes.size < cacheNum) null else pageNumber + cacheNum
            )
        }catch (e: Exception){
            Log.d("TCLS load","发生错误 ${e.message}")
            LoadResult.Error(e)
        }
    }


}