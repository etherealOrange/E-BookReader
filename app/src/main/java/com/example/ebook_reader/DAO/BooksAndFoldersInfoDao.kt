package com.example.ebook_reader.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.ebook_reader.ui.BookShelf.BookView
import com.example.ebook_reader.ui.BookShelf.FolderView
import kotlinx.coroutines.flow.Flow

@Dao
interface BooksAndFoldersInfoDao {
    @Insert
    suspend fun insertBook(book: BookView )

    @Insert
    suspend fun insertFolder(folder: FolderView)

    @Query("SELECT count(*) FROM BooksInfo")
    suspend fun getBooksNum(): Long

    @Query("SELECT * FROM BooksInfo")
    fun getAllBooks(): Flow<List<BookView>>

    @Query("SELECT * FROM FoldersInfo")
    fun getAllFolders(): Flow<List<FolderView>>

    @Query("Delete FROM BooksInfo Where uid = :bookId")
    suspend fun deleteBookById(bookId: Long)

    @Query("Delete FROM FoldersInfo Where uid = :folderId")
    suspend fun deleteFolderById(folderId: Long)

}