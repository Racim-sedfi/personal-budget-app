package com.application.personal_budget_app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides fun clock(): Clock = Clock.systemDefaultZone()
}