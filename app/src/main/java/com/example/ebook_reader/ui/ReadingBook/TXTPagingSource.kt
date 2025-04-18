package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import kotlinx.coroutines.runBlocking
import java.lang.Exception


class TXTPagingSource (
    private val index: GetChapterIndexes,
    private val reader: TxtReader
): PagingSource<Int, ChapterPage>()
{

    override fun getRefreshKey(state: PagingState<Int, ChapterPage>): Int? {
        Log.d("TPS getRefreshKey", state.anchorPosition.toString())
        return ( (state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChapterPage> {
        return try {
            val pageNumber = params.key ?: 0
            val cacheNum = 10L
            val chapterIndexes = index.getChapterIndexes((pageNumber*cacheNum).toLong(),cacheNum)
            Log.d("TPS load","章节索引 ${chapterIndexes.size}  页码 $pageNumber")
            var chapters = readChapters(chapterIndexes)
            chapters.forEach {
                Log.d("TPS load","章节内容 ${it.title} ${it.order} ${it.partOrder} \n ${it.content}")
            }
            if(chapters.isEmpty()){
                chapters = listOf(
                    ChapterPage(
                        title = "书本不存在",
                        order = 0,
                        partOrder = 0,
                        content = "章节内容不存在"
                    )
                )
            }
            LoadResult.Page(
                data = chapters,
                prevKey = if(pageNumber <= 0 ) null else pageNumber -1,
                nextKey = if (chapters.size < cacheNum) null else pageNumber + 1
            )
        }catch (e: Exception){
            Log.d("TPS load","发生错误 ${e.message}")
            LoadResult.Error(e)
        }
    }
    private fun readChapters(index: List<ChapterIndex>):List<ChapterPage>{
        return mutableListOf<ChapterPage>().apply {
            val len = index.size
            if(len==0) return@apply
            Log.d("TPS readChapters","开始分段读取TXT ${index[0]}  读取列表 长度 $len")
            index.forEach {
                var page = ChapterPage(
                    title = it.title,
                    order = it.chapterOrder,
                    partOrder = it.partOrder,
                    content = ""
                )
                val content = reader.readManyLines(it.startByte,it.endByte)
//                Log.d("TPS readChapters","章节内容 ${it.title} ${it.chapterOrder} ${it.partOrder} \n ${content.substring(0,20)}")
                page = page.copy(content = content)
                add(page)
            }
        }.sortedBy { it.order}
    }

}