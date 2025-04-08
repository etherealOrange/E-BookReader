package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Repository.BookShelf.BookShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BookShelfUIViewModule @Inject constructor(private val Repo: BookShelfRepository): ViewModel() {
    private var _CoverDir = MutableStateFlow<String>("")
    val CoverDir: StateFlow<String> get() = _CoverDir
    val isInFolder: StateFlow<Boolean> = Repo.isInFolder
    val isEditModel: StateFlow<Boolean> = Repo.isEditModel
    val selectedBooksId: StateFlow<Set<Long>> = Repo.selectedBooksId
    val selectedFolderId: StateFlow<Set<Long>> = Repo.selectedFolderId
    val inWhichFolder: StateFlow<Long?> = Repo.inWhichFolder

    fun updateCoverDir(dir: String){
        _CoverDir.value = dir
    }
    fun clearCoverDir(){
        _CoverDir.value=""
    }
    fun addSelectedBooksId(id: Long){
        Repo.addSelectedBooksId(id)
    }
    fun addSelectedFolderId(id: Long){
        Repo.addSelectedFolderId(id)
    }
    fun removeSelectedBooksId(id: Long){
        Repo.removeSelectedBooksId(id)
    }
    fun removeSelectedFolderId(id: Long){
        Repo.removeSelectedFolderId(id)
    }
    fun clearSelectedBooksId(){
        Repo.clearSelectedBooksId()
    }
    fun clearSelectedFolderId(){
        Repo.clearSelectedFolderId()
    }


    /**
     * 当前在文件夹内的id 进行过检查 一定在存在的文件夹内
     */
    val inFolderName: StateFlow<String> = combine(Repo.inWhichFolder,Repo.allFolders) {
            inWhichFolder , folders ->
        val folder = folders.find { it.folderId == inWhichFolder }
        Log.d("UIVM inFolderName","inFolderName is $folder")
        folder?.title ?: "主页"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = "主页"
    )

    /**
     * 查看是否选中了一个文件夹
     */
    val isSingleSelectedFolder: StateFlow<Boolean> = Repo.selectedFolderId
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
    val isSelectThings = combine(Repo.selectedBooksId,Repo.selectedFolderId) {
            books,folders->
        Log.d("UIVM isSelectThings","books ${books} folders ${folders}")
        books.isNotEmpty() || folders.isNotEmpty()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = false
    )

    //切换编辑模式
    fun switchEditModel(){
        Log.d("UIVM _isEditModule","_isEditModule is ${Repo.isEditModel.value}")
        Repo.switchEditModule()
    }

    /**
     * 选中书本时可以移动到文件夹, 选中文件夹时不能移动
     */
    val canMoveBooks: StateFlow<Boolean> = combine(Repo.selectedBooksId,Repo.selectedFolderId){books,folder->
        Log.d("UIVM canMoveBooks","books $books folder $folder")
        books.isNotEmpty() && folder.isEmpty()
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = false
    )



}