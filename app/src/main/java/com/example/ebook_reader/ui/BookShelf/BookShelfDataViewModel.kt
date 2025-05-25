package com.example.ebook_reader.ui.BookShelf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.DAO.ChapterInfoDao
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterChangePosition
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterSelectedControl
import com.example.ebook_reader.InterfacePackage.BookShelf.FoldersAdapterSelectedControl
import com.example.ebook_reader.Repository.BookShelf.BookAdapterUIState
import com.example.ebook_reader.Repository.BookShelf.BookShelfRepository
import com.example.ebook_reader.Repository.BookShelf.FolderAdapterUIState
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.Repository.BookShelf.ChapterModified
import com.example.ebook_reader.entities.BookMarkView
import com.example.ebook_reader.entities.BookRecord
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.PageDeduplication
import com.example.ebook_reader.entities.UIFolderView
import com.example.ebook_reader.ui.ReadingBook.TxtReader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.Long
import kotlin.collections.mutableSetOf


//负责数据的 获取 处理 打包 更新 添加 删除
@HiltViewModel
class BookShelfDataViewModel @Inject constructor (
    @ApplicationContext val context: Context,
    private val Repo: BookShelfRepository,
    private val testDao: ChapterInfoDao,
): ViewModel()
    ,BooksAdapterSelectedControl
    ,BooksAdapterChangePosition
    ,FoldersAdapterSelectedControl
{

    val isHideActionBar: StateFlow<Boolean> get()= Repo.isHideActionBar
    val isInFolder: StateFlow<Boolean> get() = Repo.isInFolder
    val inWhichFolder: StateFlow<Long?> get() = Repo.inWhichFolder
    val Folders: StateFlow<List<FolderView>> get() = Repo.allFolders
    val Books: StateFlow<List<BookView>> get() = Repo.allBooks

    //VM 中处理编辑模式UI信息
    private var _selectedBooksId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedBooksId: StateFlow<Set<Long>> = _selectedBooksId
    private var _selectedFolderId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedFolderId: StateFlow<Set<Long>> = _selectedFolderId
    override fun switchSelectedBooksId(id: Long){
        _selectedBooksId.update {
            it.toMutableSet().apply {
                if (contains(id)){
                    remove(id)
                }else{
                    add(id)
                }
            }
        }
    }
    override fun switchSelectedFolderId(id: Long){
        _selectedFolderId.update {
            it.toMutableSet().apply {
                if (contains(id)){
                    remove(id)
                }else{
                    add(id)
                }
            }
        }
    }
    fun selectAllBooksAndFolders(){
        viewModelScope.launch {
            if (isInFolder.value) {
                _selectedBooksId.value = Books.value.map { it.bookId }.toMutableSet()
            } else {
                _selectedBooksId.value = Books.value.map { it.bookId }.toMutableSet()
                _selectedFolderId.value = Folders.value.map { it.folderId }.toMutableSet()
            }
        }
    }
    fun clearAllSelected(){
        _selectedBooksId.value= mutableSetOf<Long>()
        _selectedFolderId.value=mutableSetOf<Long>()
    }

    /**
     * null ->主页面
     * -1 -> 没有选择
     * 其他 -> 选择的文件夹id
     */
    private val _moveToFolderId: MutableStateFlow<Long?> = MutableStateFlow(null)
    val moveToFolderId: StateFlow<Long?> get() = _moveToFolderId
    override fun clearMoveToFolderId(){
        _moveToFolderId.value = -1L
    }
    override fun prepareMoveToFolder(folderId: Long?){
        _moveToFolderId.value =folderId
    }
    val foldersAdapterUIItem: StateFlow<List<FolderAdapterUIState>> = combine(Repo.allUIFolders,moveToFolderId,inWhichFolder) {
            folders,move ,whichFolder->
        Log.d("VM foldersAdapterUIItem","更新了foldersAdapterUIItem\n moveToFolderId $move inWhichFolder $whichFolder")
        folders
            .filter { it.folder.folderId!=whichFolder }
            .map {
            FolderAdapterUIState(
                it,
                isSelected = it.folder.folderId == move
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )

    fun gotoMoveBooks(){
        viewModelScope.launch {
            if(selectedBooksId.value.isEmpty()) {
                Log.d("VM moveBooksToFolder", "No book selected")
                return@launch
            }
            if(isInFolder.value==false && moveToFolderId.value==null){
                Log.d("VM moveBooksToFolder", "你想要把书本从主页移动到主页 \nisInFolder -> false \nmoveToFolderId -> null")
                return@launch
            }

            if(moveToFolderId.value!=null&&Folders.value.none{it.folderId==moveToFolderId.value}) {
                Log.d("VM moveBooksToFolder", "your selected folder not exist")
                return@launch
            }
            selectedBooksId.value.forEach {
                Repo.moveBookToFolder(it,moveToFolderId.value)
                Repo.notifyBooksNumChange()
                Log.d("VM moveBooksToFolder","移动了书本id $it 的folderId 到 ${moveToFolderId.value}")
            }
            clearAllSelected()
        }
    }


    val booksAdapterUIItems: StateFlow<List<BookAdapterUIState>> = combine(Repo.allBooksAndFolders,inWhichFolder,selectedBooksId,selectedFolderId) {
        all,inWhichFolder,selectedBooks,selectedFolders ->
        Log.d("VM booksAdapterUIItems","更新了booksAdapterUIItems\n " +
                "Folders ${Repo.allFolders.value}\n" +
                "allBooksAndFolders $all \n inWhichFolder $inWhichFolder selectedBooks $selectedBooks selectedFolders $selectedFolders")
        all.filter {
            when(inWhichFolder){
                null->it is UIFolderView||(it is BookView && it.folderId==null)
                else->it is BookView && it.folderId == inWhichFolder
            }
        }.map {
            BookAdapterUIState(
                it,
                isSelected = when(it){
                    is UIFolderView->selectedFolders.contains(it.folder.folderId)
                    is BookView->selectedBooks.contains(it.bookId)
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )


    /**
     * 查看是否选中了一个文件夹
     */
    val isSingleSelectedFolder: StateFlow<Boolean> = selectedFolderId
        .map {
            Log.d("UIVM isSingleSelectedFolder","isSingleSelectedFolder  $it")
            it.size==1}
        .stateIn(
            viewModelScope,
            started = WhileSubscribed(500),
            initialValue = false
        )

    /**
     * 注意 `combine`中的`selectedBooksId`和`selectedFolderId`需要在前面初始化, 不然会报空指针异常
     *
     * 编译器比较笨没发现这一点
     */
    val isSelectThings = combine(selectedBooksId,selectedFolderId) {
            books,folders->
        Log.d("UIVM isSelectThings","books ${books} folders ${folders}")
        books.isNotEmpty() || folders.isNotEmpty()
    }.stateIn(
        viewModelScope,
        started = WhileSubscribed(500),
        initialValue = false
    )



    /**
     * 选中书本时可以移动到文件夹, 选中文件夹时不能移动
     */
    val canMoveBooks: StateFlow<Boolean> = selectedBooksId.combine(selectedFolderId){books,folder->
        Log.d("UIVM canMoveBooks","books $books folder $folder")
        books.isNotEmpty() && folder.isEmpty()
    }.stateIn(
        viewModelScope,
        started = WhileSubscribed(500),
        initialValue = false
    )

    /**
     * 通过更改VM中的值 隐藏ActionBar
     *
     * 选择文件夹id 进入文件夹内
     *
     * 如果id不存在则退出到主页
     */
    override fun goIntoFolder(folderId: Long){
        //查找要更新的文件夹id是否存在
        if(Folders.value.any{it.folderId==folderId}){
            Repo.goIntoFolder(folderId)
            Log.d("VM goIntoFolder","goIntoFolder id is $folderId")
            return
        }
        getOutOfFolder()
        Log.d("VM goIntoFolder","你的文件夹id ${Folders.value}")
        Log.d("VM goIntoFolder","你的文件夹id $folderId 不存在")
    }


    /**
     * 通过更改VM中的值 显示ActionBar 退出到主页
     *
     * 设置不在文件夹内
     */
    fun getOutOfFolder(){
        Repo.getOutOfFolder()
    }


    /**
     * 处理导入的书籍
     */
    fun loadBook(book: BookView): Result<ChapterModified>{
        Log.d("VM loadBook","loadBook 加入的书本信息: $book")
        return when(book.bookType){
            BookType.TXT -> {
                processTextBook(book)
            }
//            BookType.EPUB -> {
//                processEpubBook(book)
//            }
            BookType.PDF -> {
                processPdfBook(book)
            }
        }
    }



    /**
     *检查文件是否存在且可读
     */
    private fun checkFileExistCanRead(file: File): Result<File>{
        if(!file.exists()){
            Log.d("VM checkFileExistCanRead","文件不存在")
            return Result.failure(Exception("文件不存在"))
        }
        if(!file.canRead()){
            Log.d("VM checkFileExistCanRead","文件不可读")
            return Result.failure(Exception("文件不可读"))
        }
        return Result.success(file)
    }
    /**
     * 处理所有文件的基础方法
     */
    private fun processBook(book: BookView, process:(File)-> Result<ChapterModified>):Result<ChapterModified>{
        val file = File(context.filesDir,book.bookUrl)
        val result = checkFileExistCanRead(file)
        result.onSuccess {
            Log.d("VM processBook","文件存在且可读")
            return process(it)
        }.onFailure {
            return Result.failure(Exception(it))
        }
        return Result.failure(Exception("未知错误"))
    }

    /**
     * 处理 txt 书本
     */
    private fun processTextBook(book: BookView): Result<ChapterModified>{
        return processBook(book) {
            Log.d("VM processTextBook","处理 txt 书本路径 $it")
            val reader = TxtReader.getNewInstance(it)
            val chapters = reader.loadBook(book)
            Result.success(ChapterModified(chapters,chapters.size) ).also { reader.close() }
        }
    }

    suspend fun insertChapters(chapters: List<ChapterView>){
        withContext(Dispatchers.IO) {
            Repo.insertChapter(chapters)
        }
    }


    /**
     * 处理 epub 书本
     */
    private fun processEpubBook(book: BookView): Result<ChapterModified>{
        return processBook(book) {
            Log.d("VM processEpubBook","处理epub 书本路径 $it")
            Result.success(ChapterModified(emptyList(),0))
        }
    }
    /**
     * 处理 pdf 书本
     */
    private fun processPdfBook(book: BookView): Result<ChapterModified>{
        return processBook(book) {
            Log.d("VM processPdfBook","处理pdf 书本路径 $it")
            val pdfDescriptor = context.contentResolver.openFileDescriptor(it.toUri(),"r")
            if (pdfDescriptor==null){ return@processBook Result.failure(Exception("无法读取pdf文件")) }
            val pdfReader = PdfRenderer(pdfDescriptor)
            val pageCount = pdfReader.pageCount
            pdfDescriptor.close()
            Result.success(ChapterModified(emptyList(),pageCount))
        }
    }



    init {
        viewModelScope.launch {
            launch {
                Folders.collectLatest {  }
                Books.collectLatest {  }
            }
//            launch {
//                doSimulation()
//            }
            launch {
                Repo.chapters.buffer(1024, BufferOverflow.SUSPEND)
                    .collectLatest {
                        Log.d("VM loadBook","章节总条数：${it.size}")
                    }
            }

        }

    }

    suspend fun doSimulation(){
        if (Repo.getBooksNum()<=0){
            viewModelScope.launch {
                simulateInsertBooks()
                simulateInsertFolders()
                simulateInsertBooksInFolder(1)
                simulateInsertBookRecords()
                simulateInsertPageDedu()
                simulateInsertChapterView()
                simluateInsertBookMarks()
            }

        }
    }



    //插入书本
    suspend fun insertBook(book: BookView): Long = Repo.insertBook(book)

    //插入文件夹
    suspend fun insertFolder(folder: FolderView) {
        Repo.insertFolder(folder)
    }

    //模拟在主页 插入书本
    private suspend fun simulateInsertBooks(){
        for (i in 0..15){
            insertBook(BookView(0,"book$i",BookType.TXT,1,100,"","",null))
        }

    }
    //模拟在文件夹内 插入书本
    private suspend fun simulateInsertBooksInFolder(folderId: Long){
        for (i in 0..15){
            insertBook(BookView(0,"bookInFolder$i",BookType.TXT,1,100,"","",folderId))
        }
    }
    //模拟插入文件夹
    private suspend fun simulateInsertFolders(){
        for (i in 0..2){
            insertFolder(FolderView(0,"folder$i",""))
        }
    }
    val twoMonthAge = LocalDate.now()
        .minusMonths(2)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val sevenMonthAge = LocalDate.now()
        .minusMonths(7)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val month13Age = LocalDate.now()
        .minusYears(1)
        .minusMonths(1)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    private suspend fun simulateInsertBookRecords(){
        for (i in 1..5){
            testDao.insertBookRecord(BookRecord(
                bookId = i.toLong(),
                timeOfRecord = twoMonthAge,
                duration = 6000000L
            ))
        }
        for (i in 1..5){
            testDao.insertBookRecord(BookRecord(
                bookId = i.toLong(),
                timeOfRecord = sevenMonthAge,
                duration = 6000000L
            ))
        }
        for (i in 1..5){
            testDao.insertBookRecord(BookRecord(
                bookId = i.toLong(),
                timeOfRecord = month13Age,
                duration = 6000000L
            ))
        }
    }

    private suspend fun simulateInsertPageDedu(){
        var p = mutableListOf<PageDeduplication>()
        for (i in 1..5){
            p.add(PageDeduplication(
                bookId = i.toLong(),
                timeOfRecord = twoMonthAge,
                pageStart = 0L,
                pageEnd = 20L
            ))
            p.add(PageDeduplication(
                bookId = i.toLong(),
                timeOfRecord = sevenMonthAge,
                pageStart = 21L,
                pageEnd = 40L
            ))
            p.add(PageDeduplication(
                bookId = i.toLong(),
                timeOfRecord = month13Age,
                pageStart = 41L,
                pageEnd = 80L
            ))
        }
        testDao.insertPageDeduplication(p)
        p.clear()
    }
    private suspend fun simulateInsertChapterView(){
        for (i in 4..8){
            for(j in 4..20){
                testDao.insertChapters(listOf(ChapterView(
                    bookId = i.toLong(),
                    chapterOrder = j.toLong(),
                    chapterTitle = "chapter $i",
                    startBytes = 0L,
                    endBytes = 0L,
                    partOrder = 0L
                ))
                )
            }


        }
    }

    private suspend fun simluateInsertBookMarks(){
        for (i in 4..8){
            testDao.insertBookMark(BookMarkView(
                bookId = i.toLong(),
                timeOfRecord = twoMonthAge,
                chapterOrder = i.toLong(),
                content = "你好"
            ))
        }
        for (i in 4..8){
            testDao.insertBookMark(BookMarkView(
                bookId = i.toLong(),
                timeOfRecord = sevenMonthAge,
                chapterOrder = i.toLong()+5,
                content = "你好"
            ))
        }
        for (i in 4..8){
            testDao.insertBookMark(BookMarkView(
                bookId = i.toLong(),
                timeOfRecord = month13Age,
                chapterOrder = i.toLong()+10,
                content = "你好"
            ))
        }

    }


    /**
     * 删除选中的书本 清空SelectedBooksId
     * - 删除书本的封面
     * - 通知书本数量变化
     */
    fun deleteSelectedBooks(){
        viewModelScope.launch {
            if(selectedBooksId.value.isEmpty())return@launch
            selectedBooksId.value.forEach {
                Log.d("VM deleteSelectedBooks","删除了书本id $it")
                Repo.getBookUrl(it)?.let { rmUri(it) }
                Repo.getBookCoverUrl(it)?.let { rmUri(it) }
                Repo.deleteBookById(it)
            }
            Repo.notifyBooksNumChange()
            clearAllSelected()
        }
    }
    /**
     * 删除选中的文件夹 清空SelectedFolderId
     * - 删除文件夹的封面
     * - 删除文件夹内的书本
     */
    fun deleteSelectedFolders(){
        viewModelScope.launch {
            if (selectedFolderId.value.isEmpty())return@launch
            selectedFolderId.value.forEach {
                Log.d("VM deleteSelectedFolders","删除了文件夹id $it")
                Repo.getFolderCoverUrl(it)?.let {
                    rmUri(it)
                }
                Repo.deleteFolderById(it)
            }
            clearAllSelected()
        }
    }
    /**
     * 根据Uri删除文件
     */
    private fun rmUri(uri: String){
        val file = File(uri)
        if(file.exists()){
            file.delete()
        }
    }
    //删除选中的全部
    fun deleteSelectedAll(){
        viewModelScope.launch {
            deleteSelectedBooks()
            deleteSelectedFolders()
        }
    }
    //插入新的文件夹
    fun insertNewFolder(title: String, cover: String){
        viewModelScope.launch {
            Repo.insertFolder(FolderView(0,title,cover))
        }
    }

    /**
     * 在主页 重命名单个文件夹
     */
    fun renameFolder(title: String){
        viewModelScope.launch {
            if(selectedFolderId.value.isEmpty()) {
                Log.d("VM RenameFolder", "No folder selected")
                return@launch
            }
            else if (selectedFolderId.value.count()>1){
                Log.d("VM RenameFolder", "More than one folder selected")
                return@launch
            }
            Log.d("VM RenameFolder","inWhichFolder is ${Repo.inWhichFolder.value}")
            val folder = selectedFolderId.value.firstOrNull()
            val i = Repo.renameFolder(folder, title)
            if(i!=1){
                Toast.makeText(context,"修改文件夹名称失败",Toast.LENGTH_SHORT).show()
            }

        }
    }
    /**
     * 在文件夹内 重命名文件夹
     */
    fun renameFolderInFolder(title: String){
        viewModelScope.launch {
            if(Repo.inWhichFolder.value==null){
                Log.d("VM RenameFolderInFolder", "Not in Folder")
                return@launch
            }
            Log.d("VM renameFolderInFolder","inWhichFolder is ${Repo.inWhichFolder.value}")
            val i = Repo.renameFolder(Repo.inWhichFolder.value!!, title)
            if(i!=1) {
                Toast.makeText(context, "修改文件夹名称失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}