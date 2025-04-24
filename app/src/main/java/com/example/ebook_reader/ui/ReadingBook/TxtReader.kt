package com.example.ebook_reader.ui.ReadingBook

import android.util.Log
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import java.io.BufferedReader
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception

class TxtReader(
    private val file: File
)
{
    private var rd: RandomAccessFile? = null
    private val cur get() = rd!!


    init {
        if(file.isFile){
            rd = RandomAccessFile(file,"r")
        }
    }


    /**
     * - 负责读取指定书本的部分章节内容
     * - 应该保存一个实例来进行连续的读取操作
     */
    fun readManyLines(start: Long, end: Long): String {
        if(rd == null) return ""
        return try{
            val startPlusOne = start+1
            val constLen = 1024
            var totalByte = end - startPlusOne
            val buffer = ByteArray(constLen)
            val builder = StringBuilder()
            if(totalByte <= constLen.toLong()){
                cur.seek(startPlusOne)
                cur.read(buffer)
                buffer
                    .copyOfRange(0, totalByte.toInt())
                    .toString(Charsets.UTF_8)
                    .split('\n')
                    .forEach { builder.append("    "+it.trim()+"\n") }
            }else{
                var currentByte = 0L
                var remaining = ByteArray(0)
                while (currentByte < totalByte){
                    cur.seek(startPlusOne+currentByte+remaining.size)
                    val realLen = cur.read(buffer)
                    //读取到文件末尾了
                    if (realLen==-1)break
                    //合起来总的字节 从这里进行读取
                    val sum =  remaining + buffer.copyOfRange(0,realLen)
                    //进行判断已经读过的字节 currentByte 和 最终需要的字节 totalByte的差距
                    val diffByte = totalByte - currentByte
                    //如果比现有读取到的字节数小
                    //就代表最后一次读取 只取需要的长度
                    if(diffByte < sum.size){
                        val context = sum.copyOfRange(0, diffByte.toInt()).toString(Charsets.UTF_8)
//                        Log.d("TR readManyLines", "读取到最后一行 $context")
                        context
                            .split('\n')
                            .forEach { builder.append("    "+it.trim()+"\n") }
                        break
                    }
                    //如果长度仍然大于现在取到的字节
                    //需要读取全部 到 \n 进行下啊一个循环
                    //全部内容 字符串
                    val content = sum.toString(Charsets.UTF_8)
                    //有效内容 字符串
                    val realContent = content.substring(0, content.lastIndexOf('\n'))
                    //有效内容的字节长度
                    val realByteEnd = realContent.toByteArray(Charsets.UTF_8).size
                    //应该使用有效内容 字符串进行插入
                    realContent
                        .split('\n')
                        .forEach { builder.append("    "+it.trim()+"\n")  }

                    remaining = sum.copyOfRange(realByteEnd, sum.size)
                    currentByte += realByteEnd
                }
            }
            builder.toString()
        }catch (e: Exception){
            Log.d("TR readManyLines", "读取失败 ${e.message}")
             ""
        }
    }
    //负责指定书本的章节导入工作
    fun loadBook(book: BookView):List<ChapterView>{
        if(rd==null) return emptyList()
        val constLen = 10240
        var remainLen = 0
        var content =String()
        val pattern = """\n第([一二三四五六七八九十\d]+)章\s*(.*)\r?\n""".toRegex()
        var chapter = ChapterView(
            bookId = book.bookId,
            chapterTitle = "简介",
            chapterOrder = -1, //添加前会把Order+1
            startBytes = -1, //读取时会加一所以为了简介的完整需要-1
            endBytes = 0,
            partOrder = 0,
        )
        var hasChapters = true //代表 上一次查询是否有章节
        var findChapterNum = 0 //代表 本次循环真正找到的章节数量 用于更新hasChapters
        var currentIndex = 0L
        return mutableListOf<ChapterView>().apply {
            try{
                var remaining = ByteArray(remainLen)
                while(true){
                    //读取下一段字节
                    val buffer = ByteArray(constLen-remainLen)
                    //实际读取到的字节数
                    val realLen = cur.read(buffer)
//                    Log.d("TR loadBook","读取了$realLen 字节信息 ${String(buffer, Charsets.UTF_8).substring(0,200)}")
                    if(realLen==-1) {
                        //不存在更多的字节了
                        //需要退出
                        //如果上一次查询有章节,
                        //如果上一次查询没有章节, 那么在上一个循环结束的时候就添加了章节, 不需要再添加了
                        if(hasChapters ){
                            //需要添加章节 结束位置为文件的长度
                            chapter = chapter.copy(chapterOrder = chapter.chapterOrder+1 ,endBytes = file.length())
                            add(chapter)
                        }
                        break //读取到末尾就退出
                    }
                    //把上一次的字节 + 这一次读取到的字节
                    val sumBuffer = remaining + buffer.copyOf(realLen)
                    //这一次获取到的总字节 转为字符串
                    content = String(sumBuffer, Charsets.UTF_8)
                    //识别总的字符串中是否有章节标题 进行循环操作
                    pattern.findAll(content).forEach { match ->
//                        Log.d("TR loadBook","match ${match.value} 是否有回车")
                        //处理 上一个章节的问题
                        // 找到章节标题行的起始位置 是上一个章节的结束位置 < 这个是字符串的长度
                        val lineStart = match.range.first
//                            .also { Log.d("TR loacBook",it.toString()) }
                        //需要把字符串长度转换为字节长度
                        val realByteStart = content.substring(0, lineStart).toByteArray(Charsets.UTF_8).size
                        //相对位置 实际位置应该为 索引开始地址 + 字节相对位置
                        val realStart = currentIndex + realByteStart
                        //这是上一个章节的结束位置 使用 <
                        chapter = chapter.copy(endBytes = realStart)
                        //如果上一次查询有查询到章节题目 直接添加新的章节
                        if(hasChapters){
                            chapter = chapter.copy(chapterOrder = chapter.chapterOrder+1)
                            add(chapter)
                        }else{
                            //如果上一次查询没有找到章节, 那么 上一次查询 应该设置开始位置为 这一次的块索引开始位置
                            //结束位置为 上面的 realStart 没问题
                            //但是章节题目不变 part 需要 + 1
                            chapter = chapter.copy(
                                partOrder = 1 + chapter.partOrder,
                            )
                            chapter = chapter.copy(chapterOrder = chapter.chapterOrder+1)
                            //然后再提交
                            add(chapter)
                        }

                        //解决下一个章节的创建问题
                        //存在下一个章节所以需要重置 partOrder
                        chapter = chapter.copy(partOrder = 0L)
                        //查找结果是章节题目 下一个章节的题目 读取到\n
                        val title = match.value.trim()
                        //下一个章节 所以 章节名称需要改变, 章节顺序需要 + 1
                        chapter = chapter.copy(chapterTitle = title)
                        //获取到 下一个章节开始的位置 是章节名称的 \n 位置 + 1 字符串长度
                        val lineEnd = match.range.last
                        //转换为字节长度
                        val realByteEnd = content.substring(0, lineEnd).toByteArray(Charsets.UTF_8).size
                        //相对位置 实际位置应该为 索引开始地址 + 相对位置
                        val realEnd = currentIndex + realByteEnd
                        //下一个章节的开始位置
                        chapter = chapter.copy(startBytes = realEnd)

                        //找到了章节所以应该设置  查找找到了 这样下一次进行时就是以找到了章节为基础
                        hasChapters = true
                        //本次循环真正找到的章节数量 + 1
                        findChapterNum +=1

//                        Log.d("TR loadBook","全部信息:\n $realStart $realEnd  currentIndex $currentIndex")
                    }


                    //找到最后完整的一行 的 \n 字符串长度
                    val lastEnd = content.lastIndexOf('\n')
                    //获取到最后一行的字节长度
                    val realByteEnd = content.substring(0, lastEnd).toByteArray(Charsets.UTF_8).size
                    //相对位置 实际位置应该为 索引开始地址 + 相对位置
                    val realEnd = currentIndex + realByteEnd
                    //这里跳过新建新的章节 而是让循环继续继续
                    //无论下一次循环存不存在章节 就使用本次循环中章节开始位置
                    //还需设置下一循环块开始的位置
                    currentIndex = realEnd
//                    Log.d("TR loadBook","最后有效行结束 $realByteEnd $realEnd")
                    //还需计算remain的大小
                    remaining = sumBuffer.copyOfRange(realByteEnd, sumBuffer.size)
                    remainLen = remaining.size
//                    Log.d("TR loadBook","剩余字节长度 $remainLen ${String(remaining, Charsets.UTF_8)} currentIndex $currentIndex")

                    //处理没有找到章节的问题
                    //但是一开始设置了章节存在 怎么知道没找到章节
                    //代表这一次循环找到了章节
                    if(findChapterNum>0){

                        //如果本次循环找到了章节, 那么就设置为true
                        hasChapters = true

                    }else{
                        //说明这一次循环没有找到章节
                        //需要添加一个额外的章节
                        //这里章节的的结束位置就是最后有效的一行的位置 \n + 1
                        chapter = chapter.copy(endBytes = realEnd)
                        //这一次没找到章节需要查看上一次是否找到了章节
                        if(!hasChapters){
                            //如果上一次没找到章节需要 让part +1
                            chapter = chapter.copy(partOrder = chapter.partOrder+1)
                        }
                        //不管上一次找没找到章节, 添加章节
                        chapter = chapter.copy(chapterOrder = chapter.chapterOrder+1)
                        add(chapter)
                        //设置下一个章节的开始位置应该是上一章节的结束位置
                        chapter = chapter.copy(startBytes = chapter.endBytes)

                        //下一次循环就设置为没有章节
                        hasChapters = false
                    }
                    //还原Number 进入下一次循环
                    findChapterNum = 0
                }

            }catch (e: Exception){
                Log.d("TR loadBook", "读取失败 $e ${e.message}")
            }
        }
    }

    companion object{
        fun getNewInstance(file: File): TxtReader {
            return TxtReader(file)
        }
    }

    fun close() {
//        reader?.close()
        rd?.close()
    }



}