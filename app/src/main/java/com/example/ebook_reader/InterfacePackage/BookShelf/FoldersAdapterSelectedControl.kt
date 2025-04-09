package com.example.ebook_reader.InterfacePackage.BookShelf

interface FoldersAdapterSelectedControl {
    fun prepareMoveToFolder(folderId: Long?)
    fun clearMoveToFolderId()
}