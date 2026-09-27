package com.application.personal_budget_app.di

import android.content.Context
import androidx.room.Room
import com.application.personal_budget_app.data.local.*
import com.application.personal_budget_app.data.repository.*
import com.application.personal_budget_app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addCallback(SeedCallback())
            .build()

    @Provides fun transactionDao(db: AppDatabase) = db.transactionDao()
    @Provides fun categoryDao(db: AppDatabase) = db.categoryDao()
    @Provides fun noExpenseDayDao(db: AppDatabase) = db.noExpenseDayDao()
    @Provides fun budgetDao(db: AppDatabase) = db.budgetDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun transactions(impl: RoomTransactionRepository): TransactionRepository
    @Binds @Singleton abstract fun categories(impl: RoomCategoryRepository): CategoryRepository
    @Binds @Singleton abstract fun dayStatus(impl: RoomDayStatusRepository): DayStatusRepository
    @Binds @Singleton abstract fun budget(impl: RoomBudgetRepository): BudgetRepository
}