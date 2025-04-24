package com.example.ebook_reader.Repository.BookShelf

import android.util.Log
import com.example.ebook_reader.DAO.BooksAndFoldersInfoDao
import com.example.ebook_reader.DAO.ChapterInfoDao
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.UIFolderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class BookShelfRepository @Inject constructor (
    private val chapterDao: ChapterInfoDao
    ,private val dao: BooksAndFoldersInfoDao) {

    private val _allBooks = MutableStateFlow<List<BookView>>(emptyList())
    val allBooks: StateFlow<List<BookView>> get() = _allBooks
    private val _allFolders = MutableStateFlow<List<FolderView>>(emptyList())
    val allFolders : StateFlow<List<FolderView>> get() = _allFolders
    private val _notifyBooksNumChange = MutableStateFlow<Boolean>(false)
    /**
     * 书籍数量变化通知
     */
    fun notifyBooksNumChange(){
        _notifyBooksNumChange.value = !_notifyBooksNumChange.value
    }
    val allUIFolders: StateFlow<List<UIFolderView>> get()= combine(_allFolders,_notifyBooksNumChange) {
        folder,notify->
        folder.map {
            UIFolderView(it, dao.getBooksNumInFolder(it.folderId))
        }
    } .stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )
    suspend fun updateBookCover(bookId: Long,url: String){
        dao.updateBookCover(bookId,url)
    }

    /**
     *
     */
    private val _chapters = MutableStateFlow<List<ChapterView>>(emptyList())
    val chapters: StateFlow<List<ChapterView>> get() = _chapters


    /**
     * 书籍和文件夹的联合
     */
    val allBooksAndFolders: StateFlow<List<BookAndFolderItem>> = combine(allBooks,allUIFolders) {
        books,folders->
        folders+books
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = WhileSubscribed(500),
        initialValue = emptyList()
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                dao.getAllBooks()
                    .debounce(200)//防抖200
                    .distinctUntilChanged()//去除查询带来的数据库变化
                    .collectLatest { _allBooks.value=it }
            }
            launch {
                dao.getAllFolders()
                    .debounce(200)//防抖200
                    .distinctUntilChanged()//去除查询带来的数据库变化
                    .collectLatest { _allFolders.value=it }
            }
        }
    }
//    suspend fun updateChapters(bookId: Long){
//        _chapters.value = emptyList()
//        _chapters.update {
//            Log.d("BSR updateChapters","更新 前 目前的章节列表")
//            chapterDao.selectAllChapterFromBookId(bookId)
//        }
//        Log.d("BSR updateChapters","更新 完成 目前的章节列表")
//    }

    suspend fun insertChapter(chapters: List<ChapterView>) {
        chapterDao.insertChapters(chapters = chapters)
    }

    private val _inWhichFolder = MutableStateFlow<Long?>(null)
    val inWhichFolder: StateFlow<Long?> get() = _inWhichFolder
    /**
     * 原地刷新InWhichFolder
     */
    fun flashInWhichFolder(){
        val tmp = _inWhichFolder.value
        _inWhichFolder.value = null
        _inWhichFolder.value=tmp
    }
    //是否隐藏ActionBar的辅助存储 和方法
    private val _isHideActionBar = MutableStateFlow(false)
    val isHideActionBar: StateFlow<Boolean> get() = _isHideActionBar
    //编辑状态 和 在文件夹内 Boolean 状态
    private val _isEditModule = MutableStateFlow(false)
    val isEditModel : StateFlow<Boolean> get() = _isEditModule


    private val _isInFolder = MutableStateFlow(false)
    val isInFolder : StateFlow<Boolean> get() = _isInFolder
    fun switchEditModule() {
        _isEditModule.value = !_isEditModule.value
    }
    /**
     * 隐藏ActionBar
     *
     * 选择文件夹id 进入文件夹内
     */
    fun goIntoFolder(long: Long){

        Log.d("BSR goIntoFolder","修改三个属性成功 id $long isInFolder ${_isInFolder.value} isHideActionBar ${_isHideActionBar.value}")
        _inWhichFolder.value = long
        _isInFolder.value = true
        _isHideActionBar.value = true
        Log.d("BSR goIntoFolder","修改三个属性成功 id $long isInFolder ${_isInFolder.value} isHideActionBar ${_isHideActionBar.value}")
    }
    /**
     * 通过更改VM中的值 显示ActionBar 退出到主页
     *
     * 设置不在文件夹内
     */
    fun getOutOfFolder(){
        _inWhichFolder.value = null
        _isInFolder.value = false
        _isHideActionBar.value = false
    }

    /**
     * 修改书本总章节数
     */
    suspend fun updateBookTotalPages(bookId: Long,pages: Long){
        dao.updateBookPages(bookId,pages)
    }



    suspend fun moveBookToFolder(bookId: Long, folderId: Long?) {
        dao.moveBookToFolder(bookId, folderId)
    }
    /**
     * 获取全部书籍数量
     */
    suspend fun getBooksNum(): Long {
        return dao.getBooksNum()
    }
    /**
     * 插入整个书本实例
     * @param book 书本实例
     */
    suspend fun insertBook(book: BookView): Long {
        Log.d("BSR","进行插入书本中2")
        return dao.insertBook(book)
    }
    /**
     * 插入整个文件夹实例
     * @param folder 文件夹实例
     */
    suspend fun insertFolder(folder: FolderView) {
        dao.insertFolder(folder)
    }
    /**
     * 得到书本封面的路径
     * @param bookId 书本id
     * @return 书本封面路径
     */
    fun getBookCoverUrl(bookId: Long): String? {
        return allBooks.value.filter { bookId==it.bookId }.map { it.coverUrl }.firstOrNull()
    }

    /**
     * 得到书本的路径
     * @param bookId 书本id
     * @return 书本路径
     */
    fun getBookUrl(bookId: Long): String? {
        return allBooks.value.filter { bookId==it.bookId }.map { it.bookUrl }.firstOrNull()
    }
    /**
     * 得到文件夹封面的路径
     * @param folderId 文件夹id
     * @return 文件夹封面路径
     */
    fun getFolderCoverUrl(folderId: Long): String? {
        return allFolders.value.filter { folderId==it.folderId }.map { it.coverUrl }.firstOrNull()
    }
    /**
     * 删除书本实例
     * @param long 书本id
     */
    suspend fun deleteBookById(long: Long) {
        dao.deleteBookById(long)
    }
    /**
     * 删除文件夹实例
     * @param long 文件夹id
     */
    suspend fun deleteFolderById(long: Long) {
        dao.deleteFolderById(long)
    }
    /**
     * 重命名文件夹
     * @param folderId 文件夹id
     * @param newName 新的文件夹名称
     */
    suspend fun renameFolder(folderId: Long, newName: String) {
        dao.renameFolder(folderId, newName)
    }


}