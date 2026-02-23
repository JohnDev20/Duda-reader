package com.dudareader.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dudareader.data.local.dao.BookDao
import com.dudareader.data.local.dao.HighlightDao
import com.dudareader.data.local.entity.BookEntity
import com.dudareader.data.local.entity.HighlightEntity

@Database(
    entities = [BookEntity::class, HighlightEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun highlightDao(): HighlightDao
}
