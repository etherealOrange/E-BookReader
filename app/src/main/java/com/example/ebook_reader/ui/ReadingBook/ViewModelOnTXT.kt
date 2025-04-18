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
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetChapterIndexes
import com.example.ebook_reader.InterfacePackage.ReadingBook.RefreshChapterFlow
import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex
import com.example.ebook_reader.Repository.ReadingBook.ChapterPage
import com.example.ebook_reader.Repository.ReadingBook.ReadingRepository
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.ReadingSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModelOnTXT @Inject constructor(
    @ApplicationContext context: Context,
    private val Repo: ReadingRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel()
    , GetChapterIndexes
    , RefreshChapterFlow{

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
                    pagingSourceFactory = { TXTPagingSource(this@ViewModelOnTXT, txtReader,jump,canJump) }
                ).flow.cachedIn(viewModelScope)


                chapterListFlow = Pager(
                    config = PagingConfig(
                        pageSize = 10,
                        maxSize = 50,
                        prefetchDistance = 10,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { TXTChapterListSource(this@ViewModelOnTXT)}
                ).flow.cachedIn(viewModelScope)
                _isInitFinished.value=true
            }
        }
    }

    private val _changePosition = MutableStateFlow<Boolean>(false)
    /**
     * 返回提供体现要改变跳转位置
     */
    val changePosition get() = _changePosition.asStateFlow()


     var canJump: Boolean =false
     var jump =0L
    /**
     * 跳转到指定章节
     */
    override fun refreshChapterFlow(position: Long) {
        Log.d("VMT refreshChapterFlow", "开始 刷新章节 $position  ")
        jump = position
        canJump  = true
        _changePosition.value = !_changePosition.value
        Log.d("VMT refreshChapterFlow","刷新完毕")
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
        val end = (start + size).coerceAtMost(_chapters.size.toLong())
        //限制其长度不小于0
        val startIndex = start.coerceAtLeast(0).toInt()
        val endIndex = end.toInt()
        Log.d("VMT getChapterIndexes","开始读取章节索引 开始start $start 大小size $size\n 章节总大小${_chapters.size}")
        return _chapters.subList(startIndex,endIndex).mapIndexed {
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