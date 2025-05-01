package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "PageDeduplication"
, primaryKeys = ["bookId", "timeOfRecord"]
    , foreignKeys = [
        ForeignKey(
            entity = BookView::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )

    ],
    indices = [Index("bookId", "timeOfRecord", unique = true)]
)
/**
 * @param bookId 书本ID
 * @param pageStart 去重起始页码 代表阅读过的页码的开始
 * @param pageEnd 去重结束页码 代表第一个没阅读到的页码
 */
data class PageDeduplication(
    val bookId:Long,
    val timeOfRecord:Long,
    val pageStart:Long,
    val pageEnd:Long,
)
