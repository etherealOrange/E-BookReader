package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time
import java.sql.Timestamp

@Entity(tableName = "RecordsInfo")
data class ReadingRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Long,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val startPages: Long,
    val endPages: Long,
)
