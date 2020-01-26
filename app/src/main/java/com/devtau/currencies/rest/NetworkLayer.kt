package com.devtau.currencies.rest

import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.ui.activities.currencies.CurrenciesContract
import io.reactivex.functions.Consumer

interface NetworkLayer {
    fun getCourses(baseInfoProvider: CurrenciesContract.BaseInfoProvider, listener: Consumer<List<Currency>>, dateListener: Consumer<Long>)
}