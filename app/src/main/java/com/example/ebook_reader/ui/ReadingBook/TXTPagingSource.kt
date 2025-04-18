package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import java.lang.Exception


class TXTPagingSource (
    private val index: GetChapterIndexes,
    private val reader: TxtReader,
    private val jump: Long,
    private val canJump: Boolean
): PagingSource<Long, ChapterPage>()
{

    override fun getRefreshKey(state: PagingState<Long, ChapterPage>): Long? {
        Log.d("TPS getRefreshKey","开始 getRefreshKey")
        if(canJump){
            Log.d("TPS getRefreshKey", "跳转到位置 $jump")
            return jump
        }
        else{
            return state.anchorPosition?.toLong()
        }
    }
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, ChapterPage> {
        return try {
            var pageNumber = params.key ?: 0
            var cacheNum = 1
            if (pageNumber < 0) {
                cacheNum = cacheNum+pageNumber.toInt()
                pageNumber =0
            }
            val chapterIndexes = index.getChapterIndexes(pageNumber,cacheNum.toLong())
            Log.d("TPS load","章节索引长度 ${chapterIndexes.size}  页码pageNumber $pageNumber")
            val millis = System.currentTimeMillis()

            var chapters = readChapters(chapterIndexes)
            Log.d("VMT refreshChapterFlow", "刷新章节   耗时 ${System.currentTimeMillis()-millis}ms")

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
                prevKey = if(pageNumber <=0 ) null else pageNumber - cacheNum,
                nextKey = if (chapters.size < cacheNum) null else pageNumber + cacheNum
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