package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(tableName = "RecordCreated",
    primaryKeys = ["bookId", "recordId"],
    foreignKeys = [
        ForeignKey(
            entity = BookView::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = ReadingRecord::class,
            parentColumns = ["recordId"],
            childColumns = ["recordId"],
            onDelete = CASCADE
        )
    ]
    )
data class CreateRecord(
    val bookId:Long,
    val recordId: Long
)
