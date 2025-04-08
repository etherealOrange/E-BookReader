package com.example.ebook_reader.Repository

import android.content.Context
import androidx.room.Room
import com.example.ebook_reader.DAO.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    //提供数据库实例(使用依赖注入的方式)
    @Provides
    @Singleton
    fun provideAppDatabase(
      @ApplicationContext context: Context
    ) = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "total_database"
        ).build()

    //提供Dao实例(使用依赖注入的方式)
    @Provides
    fun provideBooksAndFoldersInfoDao(
        db: AppDatabase
    ) = db.booksAndFoldersInfoDao()

}