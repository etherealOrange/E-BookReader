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
import com.example.ebook_reader.entities.InsideFolderName
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

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

    private var _inWhichFolder = MutableStateFlow<Long?>(null)
    val FolderCoverDir = "${application.getExternalFilesDir(null)}/${InsideFolderName.FOLDERSCOVERFOLDER.displayName}"
    val BookCoverDir = "${application.getExternalFilesDir(null)}/${InsideFolderName.BOOKSCOVERFOLDER.displayName}"
    private var _CoverDir = MutableStateFlow<String>("")
    val CoverDir: StateFlow<String> get() = _CoverDir
    fun updateCoverDir(dir: String){
        _CoverDir.value = dir
    }
    fun clearCoverDir(){
        _CoverDir.value=""
    }
    /**
     * 当前在文件夹内的id 进行过检查 一定在存在的文件夹内
     */
    val inWhichFolder: StateFlow<Long?> get() = _inWhichFolder
    val inFolderName: StateFlow<String> = combine(inWhichFolder,Folders) {
        inWhichFolder , folders ->
        val folder = folders.find { it.folderId == inWhichFolder }
        Log.d("VM inFolderName","inFolderName is $folder")
        folder?.title ?: "主页"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = "主页"
    )
    /**
     * 将书本和文件夹合并成一个列表
     *
     * 根据是否进入文件夹 和进入文件夹的id来过滤 书本和文件夹
     *
     * 不在文件夹内时 显示所有不在文件夹内的书本 和 所有的文件夹
     *
     * 在文件夹内时 显示该文件夹内的书本
     */
    val items: StateFlow<List<BookAndFolderItem>> = combine(_Books,_Folders,_inWhichFolder) {
        books, folders,inWhichFolder ->
        when(inWhichFolder){
            null->{
                folders + books.filter { it.folderId==null }
            }
            else->{
                books.filter { it.folderId == inWhichFolder }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = emptyList()
    )

    //选中的书本id 和 文件夹id 和 添加 移除 方法
    private var _selectedBooksId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedBooksId: StateFlow<MutableSet<Long>> get() = _selectedBooksId
    private var _selectedFolderId = MutableStateFlow<MutableSet<Long>>(mutableSetOf())
    val selectedFolderId: StateFlow<MutableSet<Long>> get() = _selectedFolderId
    fun addSelectedBooksId(id: Long){
        viewModelScope.launch {
            _selectedBooksId.update {
                it.toMutableSet().apply { add(id) }
            }
        }
    }
    fun removeSelectedBooksId(id: Long){
        viewModelScope.launch {
            _selectedBooksId.update {
                it.toMutableSet().apply { remove(id) }
            }
        }
    }
    fun addSelectedFolderId(id: Long){
        viewModelScope.launch {
            _selectedFolderId.update {
                it.toMutableSet().apply { add(id) }
            }
        }
    }
    fun removeSelectedFolderId(id: Long){
        viewModelScope.launch {
            _selectedFolderId.update {
                it.toMutableSet().apply { remove(id) }
            }
        }
    }
    fun clearSelectedBooksId(){
        _selectedBooksId.value= mutableSetOf<Long>()
    }
    fun clearSelectedFolderId(){
        _selectedFolderId.value=mutableSetOf<Long>()
    }

    //是否隐藏ActionBar的辅助存储 和方法
    private var _isHideActionBar = MutableStateFlow(false)
    val isHideActionBar: StateFlow<Boolean> get() = _isHideActionBar

    //编辑状态 和 在文件夹内 Boolean 状态
    private var _isEditModule = MutableStateFlow(false)
    val isEditModel : StateFlow<Boolean> get() = _isEditModule
    private var _isInFolder = MutableStateFlow(false)
    val isInFolder : StateFlow<Boolean> get() = _isInFolder
    //切换编辑模式
    fun switchEditModel(){
        Log.d("VM _isEditModule","_isEditModule is ${_isEditModule.value}")
        _isEditModule.value = !_isEditModule.value
    }

    /**
     * 选中书本时可以移动到文件夹, 选中文件夹时不能移动
     */
    val canMoveBooks: StateFlow<Boolean> = combine(_selectedBooksId,_selectedFolderId){books,folder->
        Log.d("VM canMoveBooks","books $books folder $folder")
        books.isNotEmpty() && folder.isEmpty()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = false
    )

    /**
     * 查看是否选中了一个文件夹
     */
    val isSingleSelectedFolder: StateFlow<Boolean> = _selectedFolderId
        .map {
            Log.d("VM isSingleSelectedFolder","isSingleSelectedFolder  $it")
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
    val isSelectThings = combine(_selectedBooksId,_selectedFolderId) {
        books,folders->
        Log.d("VM isSelectThings","books ${books} folders ${folders}")
        books.isNotEmpty() || folders.isNotEmpty()
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
    fun goIntoFolder(long: Long){
        //查找要更新的文件夹id是否存在
        if(Folders.value.any{it.folderId==long}){
            _inWhichFolder.value = long
            _isInFolder.value = true
            _isHideActionBar.value = true
            Log.d("VM updateInWhichFolder","updateInWhichFolder is $long")
            return
        }
        getOutOfFolder()
        Log.d("VM updateInWhichFolder","你的文件夹id不存在")
    }

    /**
     * 原地刷新InWhichFolder
     */
    fun flashInWhichFolder(){
        val tmp = _inWhichFolder.value
        _inWhichFolder.value = null
        _inWhichFolder.value=tmp
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

    init {
        loadBooks()
        loadFolders()
        viewModelScope.launch {
            launch {
                doSimulation()
            }
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
                    Log.d("VM loadFolders","loadFolders is $it")
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
                    Log.d("VM loadBooks","loadBooks is $it")
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

    /**
     * 根据inWhichFolder获取文件夹名称
     *
     * 得知当前在文件夹的名称
     */
    fun getCurrentFolderName(): String {
        val folder = Folders.value.find { it.folderId == inWhichFolder.value }
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
     */
    fun deleteSelectedBooks(){
        viewModelScope.launch {
            if(selectedBooksId.value.isEmpty())return@launch
            selectedBooksId.value.forEach {
                Log.d("VM deleteSelectedBooks","删除了书本id $it")
                rmCover(booksAndFoldersInfoDao.getBookCoverUrl(it))
                booksAndFoldersInfoDao.deleteBookById(it)
            }
            clearSelectedBooksId()
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
                rmCover(booksAndFoldersInfoDao.getFolderCoverUrl(it))
                booksAndFoldersInfoDao.deleteFolderById(it)
            }
            clearSelectedFolderId()
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
            booksAndFoldersInfoDao.insertFolder(FolderView(0,title,cover))
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
            Log.d("VM RenameFolder","inWhichFolder is ${inWhichFolder.value}")
            booksAndFoldersInfoDao.renameFolder(selectedFolderId.value.first(), title)
            flashInWhichFolder()
        }
    }
    /**
     * 在文件夹内 重命名文件夹
     */
    fun renameFolderInFolder(title: String){
        viewModelScope.launch {
            if(inWhichFolder.value==null){
                Log.d("VM RenameFolderInFolder", "Not in Folder")
                return@launch
            }
            Log.d("VM renameFolderInFolder","inWhichFolder is ${inWhichFolder.value}")
            booksAndFoldersInfoDao.renameFolder(inWhichFolder.value!!, title)
            flashInWhichFolder()
        }
    }
}