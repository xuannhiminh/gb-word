package com.pdf.pdfreader.pdfviewer.editor.di

import com.pdf.pdfreader.pdfviewer.editor.database.AppDatabase
import com.pdf.pdfreader.pdfviewer.editor.database.repository.FileModelRepository
import com.pdf.pdfreader.pdfviewer.editor.database.repository.FileModelRepositoryImpl
import com.pdf.pdfreader.pdfviewer.editor.screen.main.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getInstance(androidApplication()) }
    single { FileModelRepositoryImpl(get()) as FileModelRepository}
    single { MainViewModel(androidApplication(), get()) }
}