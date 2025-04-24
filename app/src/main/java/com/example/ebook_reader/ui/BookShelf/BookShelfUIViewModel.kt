package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebook_reader.Enum.BookShelfState
import com.example.ebook_reader.Repository.BookShelf.BookShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookShelfUIViewModel @Inject constructor(private val Repo: BookShelfRepository): ViewModel() {
    private var _CoverDir = MutableStateFlow<String>("")
    val CoverDir: StateFlow<String> get() = _CoverDir
    val isInFolder: StateFlow<Boolean> = Repo.isInFolder
    val isEditModel: StateFlow<Boolean> = Repo.isEditModel

    fun updateCoverDir(dir: String){
        _CoverDir.value = dir
    }
    fun clearCoverDir(){
        _CoverDir.value=""
    }
    fun updateBookCover(bookId: Long, cover: String){
        viewModelScope.launch {
            Repo.updateBookCover(bookId,cover)
        }
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

    val state_now = isEditModel.combine(isInFolder) {edit,folder->
        if (edit && folder){
            BookShelfState.InEditInFolder
        }else if (edit && !folder){
            BookShelfState.InEditNotInFolder
        }else if (!edit && folder){
            BookShelfState.NotInEditInFolder
        }else{
            BookShelfState.NotInEditNotInFolder
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(200),
        initialValue = BookShelfState.NotInEditNotInFolder
    )


}