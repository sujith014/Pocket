package com.sujith.pocket.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.sujith.pocket.data.local.AppDatabase
import com.sujith.pocket.data.local.BookMarkDao
import com.sujith.pocket.data.local.HistoryDao
import com.sujith.pocket.data.repository.BookmarkRepositoryImpl
import com.sujith.pocket.domain.repository.BookmarkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun ProvidesAppDatabase(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesBookMarkDao(db : AppDatabase) = db.bookMarkDao()

    @Provides
    @Singleton
    fun providesHistoryDao(db : AppDatabase) = db.historyDao()


    @Provides
    @Singleton
    fun providesBookmarkRepository(dao: BookMarkDao , historyDao: HistoryDao) : BookmarkRepository = BookmarkRepositoryImpl(dao,historyDao)



}





