package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "BookMarksInfo",
    foreignKeys =[
        ForeignKey(
            entity = ChapterView::class,
            parentColumns = ["chapterId"],
            childColumns = ["chapterId"],
            onDelete = CASCADE
        )
    ]
    )
data class BookMarkView(
    @PrimaryKey(autoGenerate = true) val chapterId: Long,
    val startBytes: Long
)
