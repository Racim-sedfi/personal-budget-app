package com.application.personal_budget_app

import android.app.Application
import com.application.personal_budget_app.widget.WidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BudgetApp : Application() {

    @Inject lateinit var widgetUpdater: WidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.start()
    }
}