package com.devtau.currencies.di

import androidx.appcompat.app.AppCompatActivity
import com.devtau.currencies.data.DB
import com.devtau.currencies.rest.NetworkLayerImpl
import com.devtau.currencies.ui.activities.currencies.CurrenciesActivity
import com.devtau.currencies.ui.activities.currencies.CurrenciesContract
import com.devtau.currencies.ui.activities.currencies.CurrenciesPresenterImpl
import com.devtau.currencies.util.PreferencesManager
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(private val activity: CurrenciesActivity) {

    @Provides
    fun provideActivity(): AppCompatActivity = activity

    @Provides
    fun providePresenter(): CurrenciesContract.Presenter {
        val db = DB.getInstance(activity)
        return CurrenciesPresenterImpl(
            activity,
            db.currencyDao(),
            db.previousBaseCodesDao(),
            NetworkLayerImpl(activity),
            PreferencesManager(activity)
        )
    }
}