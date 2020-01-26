package com.devtau.currencies

import android.app.Application
import com.devtau.currencies.di.ApplicationComponent
import com.devtau.currencies.di.ApplicationModule
import com.devtau.currencies.di.DaggerApplicationComponent

class CurrenciesApp: Application() {

    lateinit var component: ApplicationComponent


    override fun onCreate() {
        super.onCreate()
        component = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this)).build()
        component.inject(this)
    }

    companion object {
        private const val LOG_TAG = "CurrenciesApp"
    }
}