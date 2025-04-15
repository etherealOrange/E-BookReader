package com.example.ebook_reader.ui.BookShelf

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterChangePosition
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterSelectedControl
import com.example.ebook_reader.InterfacePackage.BookShelf.FoldersAdapterSelectedControl
import com.example.ebook_reader.Repository.BookShelf.BookAdapterUIState
import com.example.ebook_reader.Repository.BookShelf.BookShelfRepository
import com.example.ebook_reader.Repository.BookShelf.FolderAdapterUIState
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.UIFolderView
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.Long
import kotlin.collections.mutableSetOf


//负责数据的 获取 处理 打包 更新 添加 删除
@HiltViewModel
class BookShelfDataViewModel @Inject constructor (
    @ApplicationContext val context: Context,
    private val Repo: BookShelfRepository): ViewModel()
    ,BooksAdapterSelectedControl
    ,BooksAdapterChangePosition
    ,FoldersAdapterSelectedControl
{

    val isHideActionBar: StateFlow<Boolean> get() = Repo.isHideActionBar
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
            started = SharingStarted.WhileSubscribed(500),
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
        started = SharingStarted.WhileSubscribed(500),
        initialValue = false
    )

    //切换编辑模式
    fun switchEditModel(bookAdapter: BookAdapter?){
        bookAdapter?.let {
            when(Repo.isEditModel.value){
                true->it.closeEditModel()
                false->it.openEditModel()
            }
        }
        if(Repo.isEditModel.value){clearAllSelected()}
        Log.d("UIVM _isEditModule","_isEditModule is ${Repo.isEditModel.value}")
        Repo.switchEditModule()
    }

    /**
     * 选中书本时可以移动到文件夹, 选中文件夹时不能移动
     */
    val canMoveBooks: StateFlow<Boolean> = selectedBooksId.combine(selectedFolderId){books,folder->
        Log.d("UIVM canMoveBooks","books $books folder $folder")
        books.isNotEmpty() && folder.isEmpty()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
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
     * 原地刷新InWhichFolder
     */
    fun flashInWhichFolder(){
        Repo.flashInWhichFolder()
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
    fun loadBook(book: BookView){
        Log.d("VM loadBook","loadBook 加入的书本信息: $book")
        when(book.bookType){
            BookType.TXT -> {
                processTextBook(book)
                    .onSuccess {
                        Log.d("VM loadBook","成功加载 txt 书本")

                    }
                    .onFailure {
                        Log.d("BS loadBook","失败加载 txt 书本 $it")
                    }
            }
            BookType.EPUB -> {
                processEpubBook(book)
                    .onSuccess {
                        Log.d("BS loadBook","成功加载 epub 书本")
                    }
                    .onFailure {
                        Log.d("BS loadBook","失败加载 epub 书本 $it")
                    }
            }
            BookType.PDF -> {
                processPdfBook(book)
                    .onSuccess {
                        Log.d("BS loadBook","成功加载 pdf 书本")
                    }
                    .onFailure {
                        Log.d("BS loadBook","失败加载 pdf 书本 $it")
                    }
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
    private fun processBook(book: BookView, process:(File)-> Result<String>):Result<String>{
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
    //识别章节行的正则表达式
    val chapterRegex = """^第([一二三四五六七八九十\d]+)章\s*(.*)$""".toRegex()
    private fun isChapterLine(line: String): Boolean {
        return chapterRegex.matches(line)
    }

    /**
     * 处理 txt 书本
     */
    private fun processTextBook(book: BookView): Result<String>{
        return processBook(book) {
            Log.d("VM processTextBook","处理 txt 书本路径 $it")
            var currentLines = 0
            var line = String()
            var chapter: ChapterView? = ChapterView(
                chapterId = 0,
                bookId = book.bookId,
                chapterOrder = 0,
                chapterTitle = "简介",
                chapterStartLine = 0,
                chapterEndLine = 0
            )
            val chapterList = mutableListOf<ChapterView>()
            it.inputStream().bufferedReader().use {
                //获取每一个章节的名称 和开始结束位置
                while (it.readLine().also {line=it  }!=null) {
                    if(isChapterLine(line)){
                        //填写章节结束位置
                        chapter = chapter!!.copy(chapterEndLine = currentLines-1L)
                        chapterList.add(chapter)
                        //下一章节开始 填写开始的 名称 顺序 开始
                        chapter = chapter.copy(chapterTitle = line, chapterOrder = chapter.chapterOrder+1, chapterStartLine = currentLines.toLong())
                    }
                    currentLines++
                }
                chapter = chapter!!.copy(chapterEndLine = currentLines-1L)
                chapterList.add(chapter)
            }
            Log.d("VM","读取的总行数 $currentLines")
            isFinishedInsert.value=false
            insertChapters(chapterList)


            if(currentLines == 0){
                Result.failure(Exception("没读取到任何文字"))
            }
            else{
                Result.success("txt")
            }

        }
    }
    private val isFinishedInsert = MutableStateFlow<Boolean>(false)
    private fun insertChapters(chapters: List<ChapterView>){
        viewModelScope.launch {
            Repo.insertChapter(chapters)
            isFinishedInsert.value=true
            Repo.updateChapters(chapters[0].bookId)
            Log.d("VM insertChapters","insert 插入完毕")
        }
    }
    /**
     * 处理 epub 书本
     */
    private fun processEpubBook(book: BookView): Result<String>{
        return processBook(book) {
            Log.d("VM processEpubBook","处理epub 书本路径 $it")
            Result.success("epub")
        }
    }
    /**
     * 处理 pdf 书本
     */
    private fun processPdfBook(book: BookView): Result<String>{
        return processBook(book) {
            Log.d("VM processPdfBook","处理pdf 书本路径 $it")
            Result.success("pdf")
        }
    }








    init {
        viewModelScope.launch {
            launch {
                Folders.collectLatest {  }
                Books.collectLatest {  }
            }
            launch {
                doSimulation()
            }
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
            simulateInsertBooks()
            simulateInsertFolders()
            simulateInsertBooksInFolder(1)
        }
    }



    //插入书本
    suspend fun insertBook(book: BookView): Long {
        Log.d("VM","进行插入书本中")
        return Repo.insertBook(book)
    }
    //插入文件夹
    fun insertFolder(folder: FolderView) {
        viewModelScope.launch {
            Repo.insertFolder(folder)
        }
    }
    //获取文件夹内书本的数量
    fun getBooksNumInFolder(folderId: Long): Long {
        return Books.value.count{it.folderId==folderId}.toLong()
    }
    /**
     * 根据inWhichFolder获取文件夹名称
     *
     * 得知当前在文件夹的名称
     */
    fun getCurrentFolderName(): String {
        val folder = Folders.value.find { it.folderId == Repo.inWhichFolder.value }
        return folder?.title ?: "主页"
    }
    //模拟在主页 插入书本
    private fun simulateInsertBooks(){
        viewModelScope.launch {
            for (i in 0..15){
                insertBook(BookView(0,"book$i",BookType.TXT,1,10,"","",null))
            }
        }
    }
    //模拟在文件夹内 插入书本
    private fun simulateInsertBooksInFolder(folderId: Long){
        viewModelScope.launch {
            for (i in 0..15){
                insertBook(BookView(0,"bookInFolder$i",BookType.TXT,1,10,"","",folderId))
            }
        }
    }
    //模拟插入文件夹
    private fun simulateInsertFolders(){
        viewModelScope.launch {
            for (i in 0..2){
                insertFolder(FolderView(0,"folder$i",""))
            }
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
                rmCover(Repo.getBookCoverUrl(it))
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
                rmCover(Repo.getFolderCoverUrl(it))
                Repo.deleteFolderById(it)
            }
            clearAllSelected()
        }
    }
    /**
     * 删除封面
     */
    private fun rmCover(uri: String){
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
    fun insertNewBook(title: String, type: BookType, bookUri: String, cover: String){
        viewModelScope.launch {
            Repo.insertBook(BookView(0,title,type,1,10,"",bookUri,null))
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
            Repo.renameFolder(selectedFolderId.value.first(), title)
            flashInWhichFolder()
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
            Repo.renameFolder(Repo.inWhichFolder.value!!, title)
            flashInWhichFolder()
        }
    }
}