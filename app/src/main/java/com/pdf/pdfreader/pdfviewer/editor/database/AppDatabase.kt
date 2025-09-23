package com.pdf.pdfreader.pdfviewer.editor.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pdf.pdfreader.pdfviewer.editor.model.FileModel

@Database(entities = [FileModel::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): FileModelDAO

    companion object {
        private const val DB_NAME = "pdf_file"

        fun getInstance(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, DB_NAME)
//                .allowMainThreadQueries()
                .addMigrations(object : Migration(1,3) {
                    override fun migrate(database: SupportSQLiteDatabase) {

                    }

                })
                .build()
        }
    }
}