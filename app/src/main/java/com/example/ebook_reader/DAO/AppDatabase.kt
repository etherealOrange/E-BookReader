package com.example.ebook_reader.DAO

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ebook_reader.entities.BookMarkView
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.entities.ChapterView
import com.example.ebook_reader.entities.CreateRecord
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.ReadingRecord
import com.example.ebook_reader.entities.ReadingSetting


@Database(entities = [
    BookView::class
    , FolderView::class
    , BookMarkView::class
    , ChapterView::class
    , CreateRecord::class
    , ReadingRecord::class
                     ], version = 3)
abstract class AppDatabase: RoomDatabase() {
    //封装的 一体式 Dao接口
    abstract fun booksAndFoldersInfoDao(): BooksAndFoldersInfoDao
    //章节信息Dao
    abstract fun chapterInfoDao(): ChapterInfoDao
    //连接数据库
/*    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "total_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }*/
}