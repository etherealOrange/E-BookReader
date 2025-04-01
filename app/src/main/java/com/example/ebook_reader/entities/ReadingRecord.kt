package com.example.ebook_reader.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time

@Entity(tableName = "RecordsInfo")
data class ReadingRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Long,
    val startTime: Time,
    val endTime: Time,
    val startPages: Long,
    val endPages: Long,
)
