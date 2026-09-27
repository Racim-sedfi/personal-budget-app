package com.application.personal_budget_app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter fun toDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
    @TypeConverter fun fromDate(date: LocalDate?): Long? = date?.toEpochDay()
}