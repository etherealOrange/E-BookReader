package com.example.ebook_reader.entities

import androidx.compose.ui.focus.FocusOrder
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ChaptersInfo",
    foreignKeys =[
        ForeignKey(
            entity = BookView::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId","chapterOrder"], unique = true),
    ]
)
/**
 * 章节信息
 * - 开始位置为章节名称那一行 获取内容需要 >
 * - 结束为下一章节开始前一行 获取内容需要 <=
 */
data class ChapterView(
    @PrimaryKey(autoGenerate = true) val chapterId: Long,
    val bookId: Long,
    val chapterTitle: String,
    val chapterOrder: Long,
    val startBytes: Long,
    val endBytes: Long,
    val partOrder: Long
)
