package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.PrimaryKey

@Entity(tableName = "BooksInfo",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = FolderView::class,
            parentColumns = arrayOf("folderId"),
            childColumns = arrayOf("folderId"),
            onDelete = SET_NULL
        )
    ]
    )
data class BookView(
    @PrimaryKey(autoGenerate = true) val bookId: Long,
    val title: String,
    val bookType: BookType,
    val currentPage:Long,
    val totalPages:Long,
    val coverUrl: String,
    val bookUrl: String,
    val folderId: Long?
):BookAndFolderItem()
