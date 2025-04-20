package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "BookMarksInfo",
    primaryKeys = ["bookId","chapterOrder"],
    foreignKeys =[
        ForeignKey(
            entity = ChapterView::class,
            parentColumns = ["bookId","chapterOrder"],
            childColumns = ["bookId","chapterOrder"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId","chapterOrder"], unique = true)
    ]
    )
data class BookMarkView(
    val bookId: Long,
    val chapterOrder : Long,
    val content : String
)
