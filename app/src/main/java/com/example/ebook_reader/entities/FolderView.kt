package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "FoldersInfo")
data class FolderView(
    @PrimaryKey(autoGenerate = true) val folderId: Long,
    val title: String,
    val coverUrl: String
):BookAndFolderItem()

//data class FolderView(
//    @PrimaryKey(autoGenerate = true) val uid: Long,
//    val name: String,
//    val booksNum: Long,
//    val cover: String,
//):BookAndFolderItem()
