package com.application.personal_budget_app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.application.personal_budget_app.data.local.AppDatabase
import com.application.personal_budget_app.data.local.SeedCallback
import com.application.personal_budget_app.domain.repository.DataResetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomDataReset @Inject constructor(
    private val db: AppDatabase,
    private val store: DataStore<Preferences>,
) : DataResetRepository {

    override suspend fun resetAll() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()                           // interdit sur le thread principal
            SeedCallback.seed(db.openHelper.writableDatabase)
        }
        // En dernier : effacer les réglages relance l'onboarding, la base est déjà propre.
        store.edit { it.clear() }
    }
}
