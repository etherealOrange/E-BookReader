package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Repository.BookShelf.BookShelfRepository
import com.example.ebook_reader.entities.BookAndFolderItem
import com.example.ebook_reader.entities.BookType
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.FolderView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


//负责数据的 获取 处理 打包 更新 添加 删除
@HiltViewModel
class BookShelfDataViewModel @Inject constructor (private val Repo: BookShelfRepository): ViewModel() {
    val isHideActionBar: StateFlow<Boolean> get() = Repo.isHideActionBar
    val isInFolder: StateFlow<Boolean> get() = Repo.isInFolder
    val Folders: StateFlow<List<FolderView>> get() = Repo.allFolders
    val Books: StateFlow<List<BookView>> get() = Repo.allBooks
    /**
     * 将书本和文件夹合并成一个列表
     *
     * 根据是否进入文件夹 和进入文件夹的id来过滤 书本和文件夹
     *
     * 不在文件夹内时 显示所有不在文件夹内的书本 和 所有的文件夹
     *
     * 在文件夹内时 显示该文件夹内的书本
     */
    val items: StateFlow<List<BookAndFolderItem>> = combine(Repo.allBooks,
        Repo.allFolders,
        Repo.inWhichFolder) {
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
            Repo.goIntoFolder(long)
            Log.d("VM goIntoFolder","goIntoFolder id is $long")
            return
        }
        getOutOfFolder()
        Log.d("VM goIntoFolder","你的文件夹id ${Folders.value}")
        Log.d("VM goIntoFolder","你的文件夹id $long 不存在")
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

    init {
        viewModelScope.launch {
            launch {
                Folders.collectLatest {  }
                Books.collectLatest {  }
            }
            launch {
                doSimulation()
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
    fun insertBook(book: BookView) {
        viewModelScope.launch {
            Repo.insertBook(book)
        }
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
     */
    fun deleteSelectedBooks(){
        viewModelScope.launch {
            if(Repo.selectedBooksId.value.isEmpty())return@launch
            Repo.selectedBooksId.value.forEach {
                Log.d("VM deleteSelectedBooks","删除了书本id $it")
                rmCover(Repo.getBookCoverUrl(it))
                Repo.deleteBookById(it)
            }
            Repo.clearSelectedBooksId()
        }
    }
    /**
     * 删除选中的文件夹 清空SelectedFolderId
     * - 删除文件夹的封面
     * - 删除文件夹内的书本
     */
    fun deleteSelectedFolders(){
        viewModelScope.launch {
            if (Repo.selectedFolderId.value.isEmpty())return@launch
            Repo.selectedFolderId.value.forEach {
                Log.d("VM deleteSelectedFolders","删除了文件夹id $it")
                rmCover(Repo.getFolderCoverUrl(it))
                Repo.deleteFolderById(it)
            }
            Repo.clearSelectedFolderId()
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
    /**
     * 在主页 重命名单个文件夹
     */
    fun renameFolder(title: String){
        viewModelScope.launch {
            if(Repo.selectedFolderId.value.isEmpty()) {
                Log.d("VM RenameFolder", "No folder selected")
                return@launch
            }
            else if (Repo.selectedFolderId.value.count()>1){
                Log.d("VM RenameFolder", "More than one folder selected")
                return@launch
            }
            Log.d("VM RenameFolder","inWhichFolder is ${Repo.inWhichFolder.value}")
            Repo.renameFolder(Repo.selectedFolderId.value.first(), title)
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