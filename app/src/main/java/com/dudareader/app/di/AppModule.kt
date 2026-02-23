package com.dudareader.app.di

import android.content.Context
import androidx.room.Room
import com.dudareader.data.local.AppDatabase
import com.dudareader.data.local.dao.BookDao
import com.dudareader.data.local.dao.HighlightDao
import com.dudareader.data.repository.BookRepositoryImpl
import com.dudareader.data.repository.HighlightRepositoryImpl
import com.dudareader.domain.repository.BookRepository
import com.dudareader.domain.repository.HighlightRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "duda_reader.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    fun provideHighlightDao(db: AppDatabase): HighlightDao = db.highlightDao()

    @Provides @Singleton
    fun provideBookRepository(dao: BookDao): BookRepository = BookRepositoryImpl(dao)

    @Provides @Singleton
    fun provideHighlightRepository(dao: HighlightDao): HighlightRepository = HighlightRepositoryImpl(dao)
}
