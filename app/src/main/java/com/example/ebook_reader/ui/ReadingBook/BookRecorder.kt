package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import com.example.ebook_reader.Repository.ReadingBook.ReadingRepository
import com.example.ebook_reader.entities.BookRecord
import com.example.ebook_reader.entities.PageDeduplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet


class BookRecorder(
    private val bookId: Long,
    private val timeToNotify: Int,
)
{

    //开始时间
    private var recordOfStart = 0L

    //阅读总页数
    private var pagesInRecord = CopyOnWriteArraySet<Long>()
    private var totalPagesRead = 0L

    private val notifyLock = Mutex()

    private var isPaused = false
    private var doCancel = false
    /**
     * key: startPage
     * value: endPage
     * 这里的键值对对应了PageDeduplication中的字段 需要读取
     * 记录进入的时候需要去重处理
     */
    private var recordOfPages = CopyOnWriteArrayList<Range>()

    //阅读总时间 可以暂停所以需要记录
    private var recordOfResult = 0L

    private fun delaying(): Job{
        return CoroutineScope(Dispatchers.Default).launch {
            try {
                notifyLock.withLock {
                    repeat(timeToNotify) {
                        delay(1000*60)
                        while (isPaused){
                            delay(1000)
                        }
                        if(doCancel){
                            cancel()
                        }
                    }
                }
            }catch (e: Exception){
                Log.d("BRD","提醒延时 被取消了 ${e.message}")
                cancel()
            }
        }
    }
    fun pauseNotify(){
        isPaused =true
    }
    fun resumeNotify(){
        isPaused = false
    }
    fun closeNotify(){
        doCancel = true
    }
    suspend fun toNotify(): NotifyContent?{
        val j = delaying()
        delay(300)
        notifyLock.withLock {
            if(j.isCompleted){
                //如果倒计时结束， 并且正常完成 进行提醒
                val time = System.currentTimeMillis() - recordOfStart + recordOfResult
                mergeRange()
                val perPageTime = if(totalPagesRead ==0L)0L
                else (time.toDouble()/totalPagesRead).toLong()

                Log.d("BRD","现在的用时： $time ${time/1000}秒 页数： $perPageTime")
                return NotifyContent(
                    time = time,
                    perPageTime = perPageTime
                )
            }
        }
        return null
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

    }

    fun getRecord(): BookRecord = BookRecord(
        bookId = bookId,
        timeOfRecord = System.currentTimeMillis(),
        duration = recordOfResult,
    )
    fun getPageDedu():List<PageDeduplication> {
        mergeRange()
        return if(recordOfPages.isEmpty()) emptyList()
        else{
            var time = System.currentTimeMillis()
            recordOfPages.map {
                PageDeduplication(
                    bookId = bookId,
                    pageStart = it.start,
                    pageEnd = it.end,
                    timeOfRecord = time++
                )
            }
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

    }
    //重新计算重合的页数
    private fun mergeRange(){
        Log.d("BRD","开始合并页数")
        totalPagesRead+=pagesInRecord.size
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
            pagesInRecord.clear()
            recordOfPages = CopyOnWriteArrayList(res)
        }
    }

}
data class Range(
    val start: Long,
    var end: Long
)
data class NotifyContent(
    val time: Long,
    val perPageTime: Long,
)