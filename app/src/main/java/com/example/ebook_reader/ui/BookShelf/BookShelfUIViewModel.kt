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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookShelfUIViewModel @Inject constructor(private val Repo: BookShelfRepository): ViewModel() {
    private var _CoverDir = MutableStateFlow<String>("")
    val CoverDir: StateFlow<String> get() = _CoverDir
    val isInFolder: StateFlow<Boolean> get()= Repo.isInFolder
    val isEditMode = Repo.isEditMode

    fun updateCoverDir(dir: String){
        _CoverDir.value = dir
    }
    fun clearCoverDir(){
        _CoverDir.value=""
    }
    fun setEditMode(isEditMode: Boolean) = Repo.setEditMode(isEditMode)


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

    val state_now = isEditMode.combine(isInFolder) {edit,folder->
        if (edit && folder){
            BookShelfState.InEditInFolder
        }else if (edit){
            BookShelfState.InEditNotInFolder
        }else if (folder){
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