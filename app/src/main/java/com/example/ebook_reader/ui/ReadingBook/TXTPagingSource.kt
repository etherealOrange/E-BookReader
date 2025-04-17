package com.example.ebook_reader.ui.ReadingBook

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import javax.inject.Inject


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
            if(chapters.isEmpty()){
                chapters = listOf(
                    ChapterPage(
                        title = "书本不存在",
                        order = 0,
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
                    content = ""
                )
                //去掉多余的标题
                reader.readNextLines(1)
                val len = it.endLint - it.startLine
                val content = reader.readNextLines(len)
                page = page.copy(content = content.toString())
                add(page)
            }
        }.sortedBy { it.order}

    }

}