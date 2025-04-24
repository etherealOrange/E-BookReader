package com.example.ebook_reader.ui.TimeRecorder

import android.util.Log
import com.example.ebook_reader.Repository.ReadingBook.ReadingRepository
import com.example.ebook_reader.entities.BookRecord
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet


class BookRecorder(
    private val pages: List<PageDeduplication>,
    private val bookId: Long
)
{

    //开始时间
    private var recordOfStart = 0L

    //阅读总页数
    private var pagesInRecord = CopyOnWriteArraySet<Long>()
    /**
     * key: startPage
     * value: endPage
     * 这里的键值对对应了PageDeduplication中的字段 需要读取
     * 记录进入的时候需要去重处理
     */
    private var recordOfPages = CopyOnWriteArrayList<Range>()

    //阅读总时间 可以暂停所以需要记录
    private var recordOfResult = 0L


    init {
        pages.forEach {
            recordOfPages.add(Range(it.pageStart,it.pageEnd))
        }
    }
    private fun isInRange(pos: Long): Boolean{
        recordOfPages
            .sortedBy { it.start }
            .findLast { it.start<= pos }?.let {
                if (it.end >= pos){
                    return true
                }
            }
        return false
    }

    /**
     * 开始计时和页数
     */
    fun start(pos: Long){
        //开始计时
        recordOfStart = System.currentTimeMillis()
        plusPage(pos)
        Log.d("BRD","开始 当前的页数: $pagesInRecord")
        recordOfPages.forEach {
            Log.d("BRD","开始 当前的范围: $it")
        }
    }

    fun insertRecord(repo: ReadingRepository) {
        recordOfResult += System.currentTimeMillis() - recordOfStart
        repo.insertBookRecord(
            bookRecord = BookRecord(
                bookId = bookId,
                timeOfRecord = System.currentTimeMillis(),
                duration = recordOfResult
            )
        )
        mergeRange()
        Log.d("BRD","获取 当前的页数: $pagesInRecord")
        recordOfPages.forEach {
            Log.d("BRD","获取 当前的范围: $it")
        }
        val ls = recordOfPages
        Log.d("BRD", "总时长: $recordOfResult ${recordOfResult/1000}")
        if (ls.isNotEmpty()) {
            repo.insertPageDeduplication(
                ls.map {
                    PageDeduplication(
                        bookId = bookId,
                        pageStart = it.start,
                        pageEnd = it.end,
                    )
                }
            )
        }
    }
    /**
     * 暂停计时
     */
    fun pause(){
        //结束计时
        recordOfResult += System.currentTimeMillis() - recordOfStart
        recordOfStart = System.currentTimeMillis()
        Log.d("BRD","暂停了,当前时间: $recordOfResult")
    }

    /**
     * 翻页增加页数
     */
    fun plusPage(pos: Long){
        if(!isInRange(pos)){
            pagesInRecord.add(pos)
        }
        if(pagesInRecord.size > 100){
            mergeRange()
        }
        Log.d("BRD","添加 当前的页数: $pagesInRecord")
        recordOfPages.forEach {
            Log.d("BRD","添加 当前的范围: $it")
        }
    }
    //重新计算重合的页数
    private fun mergeRange(){
        Log.d("BRD","开始合并页数")
        synchronized(this) {
            if(pagesInRecord.isEmpty())return
            val sorted = pagesInRecord.map { Range(it,it) }
            val merge = (sorted+recordOfPages)
                .toMutableList()
                .sortedWith (compareBy<Range> {it.start  }.thenBy { it.end })
            val res = mutableListOf<Range>()
            res.add(merge[0])
            for (i in 1 until merge.size){
                //开头比最后大 开新的
                if(merge[i].start > res.last().end +1) {
                    res.add(merge[i])
                //开头比最后大一 接上
                }else if(merge[i].start == res.last().end +1 ){
                    res.last().end = merge[i].end
                }else if(merge[i].end > res.last().end){
                    res.last().end = merge[i].end
                }
            }
            recordOfPages = CopyOnWriteArrayList(res)
        }
    }

}
data class Range(
    val start: Long,
    var end: Long
)