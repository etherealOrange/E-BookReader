package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

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
    ],
    indices = [
        Index(value = ["bookId","recordId"], unique = true)
    ]
    )
data class CreateRecord(
    val bookId:Long,
    val recordId: Long
)
