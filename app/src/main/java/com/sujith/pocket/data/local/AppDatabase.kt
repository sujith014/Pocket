package com.sujith.pocket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sujith.pocket.data.model.BookMarkEntity
import com.sujith.pocket.data.model.HistoryEntity

@Database(version = 1 , exportSchema =  false , entities = [BookMarkEntity::class, HistoryEntity::class])
abstract class AppDatabase  : RoomDatabase(){

    abstract fun bookMarkDao() : BookMarkDao

    abstract fun historyDao() : HistoryDao



}