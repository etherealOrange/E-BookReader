package com.example.ebook_reader.ui.BookShelf

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.DAO.AppDatabase
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookType
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.FolderView
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*sealed class BookAndFolderItem {}
//TODO:使用重新设计后的数据类 记得更改Dao和数据库
//书本表
@Entity(tableName = "BooksInfo",
    foreignKeys = [
        ForeignKey(
            entity = FolderView::class,
            parentColumns = ["uid"],
            childColumns = ["inWhichFolder"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class BookView(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    val name: String,
    val currentChapter: Int,
    val currentPage: Int,
    val totalChapter: Int,
    val cover: String,
    val isInFolder: Boolean = false,
    val inWhichFolder: Long? = null,
):BookAndFolderItem()
//文件夹表
@Entity(tableName = "FoldersInfo")
data class FolderView(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    val name: String,
    val booksNum: Long,
    val cover: String,
):BookAndFolderItem()*/


//负责数据的 获取 处理 打包 更新 添加 删除
class BookShelfViewModel(application: Application): AndroidViewModel(application) {
    //数据库连接
    private val db = AppDatabase.getDatabase(application)
    //Dao连接
    private val booksAndFoldersInfoDao = db.booksAndFoldersInfoDao()
    //私有书本
    private var _Books = MutableStateFlow<List<BookView>>(emptyList())
    //私有文件夹
    private var _Folders = MutableStateFlow<List<FolderView>>(emptyList())
    val Folders: StateFlow<List<FolderView>> get() = _Folders

    /**
     * 将书本和文件夹合并成一个列表
     *
     * 根据是否进入文件夹 和进入文件夹的id来过滤 书本和文件夹
     *
     * 不在文件夹内时 显示所有不在文件夹内的书本 和 所有的文件夹
     *
     * 在文件夹内时 显示该文件夹内的书本
     */
    val items: StateFlow<List<BookAndFolderItem>> = combine(_Books,_Folders) {
        books, folders ->
        when(inWhichFolder.value){
            null->{
                folders + books.filter { it.folderId==null }
            }
            else->{
                books.filter { it.folderId == inWhichFolder.value }
            }
        }
    }
     .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    //是否隐藏ActionBar的辅助存储 和方法
    private var _isHideActionBar = MutableStateFlow(false)
    val isHideActionBar: StateFlow<Boolean> get() = _isHideActionBar

    //编辑状态 和 在文件夹内 Boolean 状态
    private var _isEditModule = MutableStateFlow(false)
    val isEditModel : StateFlow<Boolean> get() = _isEditModule
    private var  _isInFolder = MutableStateFlow(false)
    val isInFolder : StateFlow<Boolean> get() = _isInFolder
    private var _inWhichFolder = MutableStateFlow<Long?>(null)
    val inWhichFolder: StateFlow<Long?> get() = _inWhichFolder
    //修改 现在 进入的文件夹id
    fun updateInWhichFolder(long: Long?){
        //查找要更新的文件夹id是否存在
        if(Folders.value.any{it.folderId==long}){
            _inWhichFolder.value = long
            return
        }
        _inWhichFolder.value = null
        Log.d("updateInWhichFolder","你的文件夹id不存在")

    }
    fun switchEditModel(){
        _isEditModule.value = !_isEditModule.value
    }
    @Deprecated("不再使用")
    fun setEditModel(boolean: Boolean){
        _isEditModule.value = boolean
    }
    fun resetEditAndInFolderModels(){
        _isInFolder.value=false
        _isEditModule.value=false
    }
    /**
     * 选中书本时可以移动到文件夹, 选中文件夹时不能移动
     */
    private var _canMoveBooks= MutableStateFlow(false)
    val canMoveBooks: StateFlow<Boolean> get() = _canMoveBooks

    /**
     * 检查是否选中了
     */
    private fun updateCanMoveBooks(){
        viewModelScope.launch {
            combine(selectedBooksId,selectedFolderId){
                books,folder->
                books.isNotEmpty() && folder.isEmpty()
            }.collectLatest {
                _canMoveBooks.value = it
            }
        }
    }

    /**
     * 查看是否选中了一个文件夹
     */
    private var _isSingleSelectedFolder = MutableStateFlow(false)
    val isSingleSelectedFolder: StateFlow<Boolean> get() = _isSingleSelectedFolder
    private fun updateIsSingleSelectedFolder(){
        viewModelScope.launch {
            selectedFolderId.collectLatest {
                _isSingleSelectedFolder.value = it.size == 1
            }
        }
    }

    //选中的书本id 和 文件夹id 和 添加 移除 方法
    private var _selectedBooksId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedBooksId: StateFlow<MutableSet<Long>> get() = _selectedBooksId
    private var _selectedFolderId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedFolderId: StateFlow<MutableSet<Long>> get() = _selectedFolderId
    fun addSelectedBooksId(id: Long){
        _selectedBooksId.value.add(id)
    }
    fun removeSelectedBooksId(id: Long){
        _selectedBooksId.value.remove(id)
    }
    fun addSelectedFolderId(id: Long){
        _selectedFolderId.value.add(id)
    }
    fun removeSelectedFolderId(id: Long){
        _selectedFolderId.value.remove(id)
    }
    fun clearSelectedBooksId(){
        _selectedBooksId.value.clear()
    }
    fun clearSelectedFolderId(){
        _selectedFolderId.value.clear()
    }


    /**
     * 通过更改VM中的值 隐藏ActionBar
     *
     * 设置在文件夹内
     */
    fun goIntoFolder(){
        _isInFolder.value = true
        _isHideActionBar.value = true
    }
    /**
     * 通过更改VM中的值 显示ActionBar
     *
     * 设置不在文件夹内
     */
    fun getOutOfFolder(){
        _isInFolder.value = false
        _isHideActionBar.value = false
    }

    init {
        updateIsSingleSelectedFolder()
        updateCanMoveBooks()
        loadBooks()
        loadFolders()
        viewModelScope.launch {
            doSimulation()
        }

    }
    suspend fun doSimulation(){
        if (booksAndFoldersInfoDao.getBooksNum()<=0){
            simulateInsertBooks()
            simulateInsertFolders()
            simulateInsertBooksInFolder(1)
        }
    }

    @OptIn(FlowPreview::class)
    private fun loadFolders() {
        viewModelScope.launch {
            booksAndFoldersInfoDao.getAllFolders()
                .debounce (200) //防抖200
                .distinctUntilChanged() //去除查询带来的数据库变化
                .collectLatest {
                    _Folders.value = it
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun loadBooks() {
        viewModelScope.launch {
            booksAndFoldersInfoDao.getAllBooks()
                .debounce (200) //防抖200
                .distinctUntilChanged() //去除查询带来的数据库变化
                .collectLatest {
                    _Books.value = it
                }
        }

    }
    //插入书本
    fun insertBook(book: BookView) {
        viewModelScope.launch {
            booksAndFoldersInfoDao.insertBook(book)
        }
    }
    //插入文件夹
    fun insertFolder(folder: FolderView) {
        viewModelScope.launch {
            booksAndFoldersInfoDao.insertFolder(folder)
        }
    }
    //获取文件夹内书本的数量
    fun getBooksNumInFolder(folderId: Long): Long {
        val num: Long = _Books.value.count{it.folderId==folderId}.toLong()
        return num
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

    //删除选中的书本
    fun deleteSelectedBooks(){
        viewModelScope.launch {
            selectedBooksId.value.forEach {
                booksAndFoldersInfoDao.deleteBookById(it)
            }
        }
    }
    //删除选中的文件夹
    fun deleteSelectedFolders(){
        viewModelScope.launch {
            selectedFolderId.value.forEach {
                booksAndFoldersInfoDao.deleteFolderById(it)
            }
        }
    }
    //删除选中的全部
    fun deleteSelectedAll(){
        viewModelScope.launch {
            deleteSelectedBooks()
            deleteSelectedFolders()
        }
    }
    //重命名文件夹
    fun renameFolder(folderId: Long, title: String){
        viewModelScope.launch {
            booksAndFoldersInfoDao.renameFolder(folderId, title)
        }
    }
    //使用选中文件夹 重命名文件夹
    fun renameFolder(title: String){
        viewModelScope.launch {
            if(selectedFolderId.value.isEmpty()) {
                Log.d("RenameFolder", "No folder selected")
                return@launch
            }
            else if (selectedFolderId.value.count()>1){
                Log.d("RenameFolder", "More than one folder selected")
                return@launch
            }
            booksAndFoldersInfoDao.renameFolder(selectedFolderId.value.first(), title)
        }
    }
}