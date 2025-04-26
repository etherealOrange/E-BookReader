package com.example.ebook_reader.Repository

import android.content.Context
import androidx.room.Room
import com.example.ebook_reader.DAO.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        ).fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob()+ Dispatchers.IO)

    @Provides
    fun provideDataInfoDao(
        db: AppDatabase
    ) = db.DataInfoDao()

    //提供Dao实例
    @Provides
    fun provideBooksAndFoldersInfoDao(
        db: AppDatabase
    ) = db.booksAndFoldersInfoDao()
    //提供章节信息Dao实例
    @Provides
    fun provideChapterInfoDao(
        db: AppDatabase
    ) = db.chapterInfoDao()

    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ) = context



}