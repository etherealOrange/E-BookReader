package com.example.ebook_reader.ui.ReadingBook

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import coil3.Bitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.InterfacePackage.ReadingBook.GetPDFPage
import com.example.ebook_reader.Repository.ReadingBook.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ViewModelOnPDF @Inject constructor(
    @ApplicationContext val context: Context,
    private val Repo: ReadingRepository,
    savedStateHandle: SavedStateHandle
) : BaseReadingViewModel(savedStateHandle)
, GetPDFPage
{

    val pdfUri = File(context.filesDir,book.bookUrl)
    private val fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri.toUri(), "r").also { Log.d("VMP","创建了fileDescriptor $it") }
    val pdfRenderer = fileDescriptor?.let { PdfRenderer(it) }.also { Log.d("VMP","创建了 pdfRenderer $it") }
    val pdfSize = pdfRenderer?.pageCount.also { Log.d("VMP","Pdf 有 $it 页") }
    private lateinit var _recorder : BookRecorder
    val recorder get()= _recorder
    private val _isFinished = MutableStateFlow<Boolean>(false)
    val isFinished = _isFinished.asStateFlow()
    private val otherSetting = ConfigManager.getInstance(context).getOtherConfig()
    init {
        viewModelScope.launch {
            _isFinished.value=false
            _recorder = BookRecorder(book.bookId,otherSetting.sleepTime)
            _isFinished.value=true
        }
    }

    private val _currentPos = MutableStateFlow<Long>(book.currentPage)
    val currentPos = _currentPos.asStateFlow()
    fun updateCurrentPos(pos: Long) {
        _currentPos.value = pos
    }

    val page = Pager(
        initialKey = book.currentPage,
        config = PagingConfig(
            pageSize = 3,
            maxSize = 10,
            prefetchDistance = 2
        ),
        pagingSourceFactory = { PDFPagingSource(pdfRenderer) }
    ).flow.cachedIn(viewModelScope)

    fun updateRecord(){
        Repo.insertBookRecord(recorder.getRecord())
        Repo.insertPageDeduplication(recorder.getPageDedu())
    }

    override fun getPage(pageIndex: Long): Bitmap? {
        if ( pdfRenderer==null || pageIndex < 0 || pageIndex >= pdfRenderer.pageCount ) {
            return null
        }
        val page = pdfRenderer.openPage(pageIndex.toInt())
        Log.d("VMP","原始大小  ${page.height} ${page.width}")
        // 计算缩放比例
        val scale = 1080f/page.width


        val bitmap = createBitmap(1080, (page.height*scale).toInt())
        page.render(bitmap,null,null,RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }

    override fun onCleared() {
        super.onCleared()
        Repo.updateCurrentPage(book.bookId,_currentPos.value)
        pdfRenderer?.close()
        fileDescriptor?.close()
    }

}