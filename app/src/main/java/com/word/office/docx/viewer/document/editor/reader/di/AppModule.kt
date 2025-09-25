package com.word.office.docx.viewer.document.editor.reader.di

import com.word.office.docx.viewer.document.editor.reader.database.AppDatabase
import com.word.office.docx.viewer.document.editor.reader.database.repository.FileModelRepository
import com.word.office.docx.viewer.document.editor.reader.database.repository.FileModelRepositoryImpl
import com.word.office.docx.viewer.document.editor.reader.screen.main.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { FileModelRepositoryImpl(get()) as FileModelRepository}
    single { MainViewModel(androidApplication(), get()) }
}