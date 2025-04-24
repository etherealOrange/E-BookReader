package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(tableName = "BookRecord",
    primaryKeys = ["bookId", "timeOfRecord"],
    foreignKeys = [
        ForeignKey(
            entity = BookView::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = CASCADE
        ),
    ],
    indices = [
        Index(value = ["bookId","timeOfRecord"], unique = true)
    ]
    )
/**
 * @param bookId 书本ID
 * @param timeOfRecord 实际记录的时间
 * @param duration 阅读总时间(每次更新累加)
 */
data class BookRecord(
    val bookId:Long,
    val timeOfRecord:Long,
    val duration:Long,
)
