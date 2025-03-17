package com.example.ebook_reader.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ebook_reader.ui.BookShelf.BookAndFolderItem
import com.example.ebook_reader.ui.BookShelf.BookView
import com.example.ebook_reader.ui.BookShelf.FolderView

@Database(entities = [BookView::class, FolderView::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    //封装的 一体式 Dao接口
    abstract fun booksAndFoldersInfoDao(): BooksAndFoldersInfoDao
    //连接数据库
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "books_and_folders_info_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}