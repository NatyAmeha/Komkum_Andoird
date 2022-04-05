package com.komkum.komkum.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class SampleMigration : Migration(2 , 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
       database.execSQL("ALTER TABLE SongDbInfo  ADD COLUMN sampleColumn INTEGER")
    }
}