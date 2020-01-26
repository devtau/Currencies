package com.devtau.currencies.di

import com.devtau.currencies.CurrenciesApp
import dagger.Component

@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(application: CurrenciesApp)
}