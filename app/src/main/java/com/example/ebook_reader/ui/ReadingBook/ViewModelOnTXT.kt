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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModelOnTXT @Inject constructor(
    @ApplicationContext context: Context,
    private val Repo: ReadingRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(), GetChapterIndexes {

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
                _isInitFinished.value=true
            }
        }
    }
    private lateinit var txtReader : TxtReader
    private lateinit var _chapters : List<ChapterView>


    /**
     * paging3 的章节缓存加载的原始数据
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var chapterFlow: Flow<PagingData<ChapterPage>>

    /**
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
                            startLine = chapter.chapterStartLine,
                            endLint = chapter.chapterEndLine
                        )
                    )
                }
            }

        }

    }


}