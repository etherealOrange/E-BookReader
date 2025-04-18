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
) : ViewModel(), GetChapterIndexes {

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
                    config = PagingConfig(
                        pageSize = 2,
                        maxSize = 10,
                        prefetchDistance = 1,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { TXTPagingSource(this@ViewModelOnTXT, txtReader) }
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

//                txtReader.loadBook()
//                    .filter { it.chapterOrder>400 }
//                    .forEach {
//                    Log.d("VMT init","章节信息\n $it")
//                }
//                txtReader.readManyLines(20409,22822).also {
//                    Log.d("VMT init","章节内容\n $it")
//                }

//                val pattern = """\n第([一二三四五六七八九十\d]+)章\s*(.*)\r?\n""".toRegex()
//                pattern.findAll("hhhhh\n第1章 一笑出门去，千里落花风\r\n").forEach { match ->
//                    Log.d("VMT init", "match ${match.value} 是否有回车")
//                }
                _isInitFinished.value=true
            }
        }
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
        return mutableListOf<ChapterIndex>().apply {
            Log.d("VMT getChapterIndexes","开始读取章节索引 $start $size\n 章节大小${_chapters.size}")
            _chapters.forEachIndexed {
                index, chapter ->
                if(index>= start+size)return@forEachIndexed
                Log.d("VMT getChapterIndexes","章节索引 $index \n $chapter")
                if (index in start until start + size) {
                    add(
                        ChapterIndex(
                            title = chapter.chapterTitle,
                            chapterOrder = chapter.chapterOrder,
                            startByte = chapter.startBytes,
                            endByte = chapter.endBytes,
                            partOrder = chapter.partOrder
                        )
                    )
                }
            }

        }

    }

    //阻止多个按钮一起按下
    private val canSingleTouch = MutableStateFlow<Boolean>(true)
    //计算按住总时长
    private val _touchMillis = MutableStateFlow<Long>(0)
    //开始按下的时间
    private val startTouchMillis = MutableStateFlow<Long>(0)
    val touchMillis = _touchMillis.asStateFlow()
    private val repeatDelay =200L
    private val startRepeatDelay = 500L

    //开始按下
    fun startTouch(){
        startTouchMillis.value= System.currentTimeMillis()
    }
    //结束按下 同时更新按下的总时间
    fun endTouch(){
        _touchMillis.value = System.currentTimeMillis() - startTouchMillis.value
    }

    fun doTouching(){
        canSingleTouch.value=false
    }
    fun notTouching(){
        canSingleTouch.value=true
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