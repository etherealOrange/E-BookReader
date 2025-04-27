package com.example.ebook_reader.InterfacePackage.ReadingBook

import com.example.ebook_reader.Repository.ReadingBook.ChapterIndex

interface GetChapterIndexes: GetCurrentPos {
    fun getChapterIndexes(start : Long, size: Long): List<ChapterIndex>
}