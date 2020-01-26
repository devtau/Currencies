package com.devtau.currencies.di

import android.app.Application
import com.devtau.currencies.CurrenciesApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val baseApp: CurrenciesApp) {

    @Provides
    @Singleton
    fun provideApplication(): Application = baseApp
}