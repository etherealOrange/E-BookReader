package com.example.ebook_reader.ui.ReadingBook

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.InterfacePackage.ReadingBook.DeleteBookMark
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetBookMarks
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.InterfacePackage.ReadingBook.RefreshChapterFlow
import com.example.ebook_reader.Repository.ReadingBook.BookMarkUI
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import com.example.ebook_reader.Repository.ReadingBook.ReadingRepository
import com.example.ebook_reader.Repository.ReadingBook.transferBookMark
import com.example.ebook_reader.entities.BookMarkView
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.ReadingSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModelOnTXT @Inject constructor(
    @ApplicationContext context: Context,
    private val Repo: ReadingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel()
    , GetChapterIndexes
    , RefreshChapterFlow
    , GetBookMarks
    , DeleteBookMark
{

    private val _config = MutableStateFlow<ReadingSetting>(ReadingSetting())
    val config get() = _config.asStateFlow()

    fun updateConfig(config: ReadingSetting) {
        Log.d("VMT updateConfig", "更新配置 $config")
        _config.value = config
    }

    private val _book = savedStateHandle.get<BookView>("book")
    val book get() = _book?: BookView(
        bookId = -1L,
        title = "没有书本",
        bookUrl = "",
        bookType = BookType.TXT,
        coverUrl = "",
        currentPage = 0L,
        totalPages = 0L,
        folderId = null,
    )
    val currentBookMark =Repo.currentBookMark
    private var _currentChapterPos = MutableStateFlow<Long>(0L)
    val currentChapterPos = _currentChapterPos.asStateFlow()
    fun updatePos(pos: Long){
        _currentChapterPos.value = pos
        viewModelScope.launch(Dispatchers.IO) {
            Repo.setCurrentBookMark(book.bookId, pos)
        }
    }
    fun changeBookMark(content: String){
        val pos = _currentChapterPos.value
        viewModelScope.launch {
            if(Repo.isBookMarkExist(book.bookId, pos)){
                Repo.updateBookMark(book.bookId, pos, content)
            }
            else{
                Repo.insertBookMark(BookMarkView(
                    bookId = book.bookId,
                    chapterOrder = pos,
                    content = content
                ))
            }
            jumpOfBookMark.notifyChange()
        }
    }

    /**
     * 删除书签
     */
    override fun delete(order: Long) {
        viewModelScope.launch {
            Repo.deleteBookMark(book.bookId, order)
            jumpOfBookMark.notifyChange()
        }
    }
    init {
        viewModelScope.launch {
            currentChapterPos.collectLatest {  }
        }
    }


    private val _isInitFinished = MutableStateFlow<Boolean>(false)
    val isInitFinished = _isInitFinished.asStateFlow()
    init {
        viewModelScope.launch {
            launch {
                _isInitFinished.value=false
                _chapters = Repo.getChaptersFromBookId(book.bookId).sortedBy { it.chapterOrder }

                txtReader = TxtReader(File(context.filesDir, book.bookUrl))
                chapterFlow = Pager(
                    initialKey = 0,
                    config = PagingConfig(
                        pageSize = 10,
                        maxSize = 30,
                        prefetchDistance = 1,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { TXTPagingSource(this@ViewModelOnTXT, txtReader,jumpOfChapter) }
                ).also { updatePos(jumpOfChapter.toPosition);jumpOfChapter.resetJump() }
                    .flow.cachedIn(viewModelScope)


                chapterListFlow = Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        maxSize = 60,
                        prefetchDistance = 10,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { TXTChapterListSource(this@ViewModelOnTXT,jumpOfList)}
                ).also { jumpOfList.resetJump() }
                    .flow.cachedIn(viewModelScope)


                bookMarkFlow = Pager(
                    config = PagingConfig(
                        pageSize = 20,
                        maxSize = 60,
                        prefetchDistance = 10,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { TXTBookMarkSource(this@ViewModelOnTXT,jumpOfBookMark) }
                ).also { jumpOfBookMark.resetJump() }
                    .flow.cachedIn(viewModelScope)

                _isInitFinished.value=true
            }
        }
    }


    lateinit var bookMarkFlow: Flow<PagingData<BookMarkUI>>

    override suspend fun getBookMarks(
        order: Long
    ): transferBookMark {
        val pair = Repo.getBookMarkSrc(book.bookId, order
            .coerceAtLeast(0)
            .coerceAtMost(_chapters.size.toLong())
        )

        return transferBookMark(
            bookMark = pair.bookMark,
            preId = pair.preId,
            nextId = pair.nextId,
            order = order,
            title = _chapters[order.toInt()].chapterTitle
        )
    }

    val jumpOfBookMark = JumpSolve()

    /**
     * 跳转和刷新书签
     * - 在主视图 下方的查看书签, 点击的时候需要跳转到指定 位置的书签
     * - 在书签列表中, 点击定位时需要到指定位置的书签
     * - 删除的时候
     * - 所有更新BookMark的时候
     */
    fun refreshBookMarkFlow(position: Long){
        Log.d("VMT refreshBookMarkFlow", "开始 刷新书签 $position  ")
        jumpOfBookMark.setJump(position)
        jumpOfBookMark.notifyChange()
        Log.d("VMT refreshBookMarkFlow","刷新完毕")
    }
    /**
     * 跳转到指定章节
     */
    val jumpOfChapter = JumpSolve()

    /**
     * 跳转到指定章节
     */
    override fun refreshChapterFlow(position: Long) {
        Log.d("VMT refreshChapterFlow", "开始 刷新章节 $position  ")
        jumpOfChapter.setJump(position)
        jumpOfChapter.notifyChange()
        Log.d("VMT refreshChapterFlow","刷新完毕")
    }

    /**
     * 章节列表跳转
     */
    val jumpOfList = JumpSolve()
    /**
     * 刷新章节列表
     * @param isToStartOrEnd true 表示跳转到开始 false表示跳转到结束
     */
    fun refreshChapterListFlow(isToStartOrEnd: Boolean){
        Log.d("VMT refreshChapterListFlow", "开始跳转到 ${if(isToStartOrEnd) "开始" else "结束"}")
        if(isToStartOrEnd){
            jumpOfList.setJump(0L)
        }
        else{
            jumpOfList.setJump(_chapters.size.toLong()-1)
            Log.d("VMT refreshChapterListFlow","章节列表跳转到 ${jumpOfList.toPosition}")
        }
        jumpOfList.notifyChange()
        Log.d("VMT refreshChapterListFlow","刷新完毕")
    }



    private lateinit var txtReader : TxtReader
    private lateinit var _chapters : List<ChapterView>


    /**
     * paging3 的章节内容缓存加载的原始数据 加载章节内容的数据
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var chapterFlow: Flow<PagingData<ChapterPage>>

    /**
     * paging3 的章节索引缓存加载 加载章节列表的数据
     */
    lateinit var chapterListFlow: Flow<PagingData<ChapterIndex>>


    /**
     * 提供给Paging3的章节部分索引数据
     * - start 开始从0开始
     * - size 读取的数量
     */
    override fun getChapterIndexes(start: Long, size: Long): List<ChapterIndex> {
        if(_chapters.isEmpty()) return emptyList()
        //限制其长度不超过章节索引的总长度
        val end = (start + size).coerceAtMost(_chapters.size.toLong()).toInt()
        //限制其长度不小于0
        val startIndex = start.coerceAtLeast(0).toInt().coerceAtMost(end)
        Log.d("VMT getChapterIndexes","开始读取章节索引 开始start $start 大小size $size\n 章节总大小${_chapters.size}")
        return _chapters.subList(startIndex,end).mapIndexed {
            index,chapter->
            ChapterIndex(
                title = chapter.chapterTitle,
                chapterOrder = chapter.chapterOrder,
                startByte = chapter.startBytes,
                endByte = chapter.endBytes,
                partOrder = chapter.partOrder
            )
        }
    }






    //进行一次字体大小的减法
    fun minusTextSize(){
        if(_config.value.textSize>20){
            _config.update { it.copy(textSize = it.textSize - 1) }
            Log.d("VMT minusTextSIze","减小字体大小 ${_config.value.textSize}")
        }
    }
    //进行一次字体大小的加法
    fun plusTextSize(){
        if (_config.value.textSize<35){
            _config.update { it.copy(textSize = it.textSize +1) }
            Log.d("VMT plusTextSize","增大字体大小 ${_config.value.textSize}")
        }
    }
    //进行一次字间距的减法
    fun minusLineSpacing(){
        if (_config.value.lineSpacing>0){
            _config.update { it.copy(lineSpacing = it.lineSpacing - 1) }
            Log.d("VMT minusLineSpacing","减小行间距 ${_config.value.lineSpacing}")
        }
    }
    //进行一次字间距的加法
    fun plusLineSpacing(){
        if (_config.value.lineSpacing<30){
            _config.update { it.copy(lineSpacing = it.lineSpacing + 1) }
            Log.d("VMT plusLineSpacing","增大行间距 ${_config.value.lineSpacing}")
        }
    }
    //进行一次字间距的减法
    fun minusLetterSpacing(){
        if(_config.value.letterSpacing>0){
            _config.update { it.copy(letterSpacing = it.letterSpacing - 1) }
            Log.d("VMT minusLetterSpacing","减小字间距 ${_config.value.letterSpacing}")
        }
    }
    //进行一次字间距的加法
    fun plusLetterSpacing() {
        if (_config.value.letterSpacing < 30) {
            _config.update { it.copy(letterSpacing = it.letterSpacing + 1) }
            Log.d("VMT plusLetterSpacing", "增大字间距 ${_config.value.letterSpacing}")
        }
    }


    //进行持续的操作
    private var job: Job?=null
    fun keepDoThing(minusOrPlus: () -> Unit){
        val repeatDelay =200L
        val startRepeatDelay = 500L
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            Log.d("VMT keepMinusThing","开始持续操作")
            delay(startRepeatDelay)
            while (true){
                minusOrPlus()
                Log.d("VMT keepMinusThing","进行一次操作")
                delay(repeatDelay)
            }
        }
    }

    fun cancelJobAndDo(minusOrPlus: () -> Unit) {
        job?.cancel()
        Log.d("VMT cancelJobAndDo", "取消了任务")
        minusOrPlus()
        job=null
    }

    override fun onCleared() {
        super.onCleared()
        txtReader.close()
    }


}