package com.devtau.currencies.di

import com.devtau.currencies.ui.activities.currencies.CurrenciesActivity
import dagger.Component

@Component(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(currenciesActivity: CurrenciesActivity)
}