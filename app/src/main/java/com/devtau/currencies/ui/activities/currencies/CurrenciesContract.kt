package com.devtau.currencies.ui.activities.currencies

import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.ui.StandardView
import io.reactivex.disposables.CompositeDisposable

interface CurrenciesContract {
    interface Presenter {
        val subscriptions: CompositeDisposable
        fun restartLoaders(testMode: Boolean = false)
        fun stopLoaders()
        fun updateBase(index: Int)
        fun updateBaseAmount(amount: Float)
        fun saveCurrenciesToDB(newList: List<Currency>)
    }

    interface View: StandardView {
        fun showCurrencies(list: List<Currency>)
        fun showOutdatedWarn(show: Boolean, date: Long)
        fun showOfflineWarn(show: Boolean)
    }

    interface ListListener {
        fun rowSelected(position: Int)
        fun editTextSelected(position: Int)
    }

    interface BaseInfoProvider {
        fun provideBase(): Currency?
        fun providePreviousBaseCodes(): ArrayList<String>
    }
}