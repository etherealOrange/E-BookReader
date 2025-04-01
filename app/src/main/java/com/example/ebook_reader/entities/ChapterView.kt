package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "ChaptersInfo",
    foreignKeys =[
        ForeignKey(
            entity = BookView::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = CASCADE
        )
    ]
)
data class ChapterView(
    @PrimaryKey(autoGenerate = true) val chapterId: Long,
    val bookId: Long,
    val chapterNumber: Long,
    val chapterBytes: Long,

)
