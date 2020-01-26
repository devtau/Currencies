package com.devtau.currencies.rest

import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.util.Logger
import com.mynameismidori.currencypicker.ExtendedCurrency

class CurrenciesResponse(
    val base: String?,
    val date: String?,
    val rates: Map<String, Float>? = null
) {

    fun ratesAsList(base: Currency?, previousBaseCodes: List<String>): List<Currency> {
        val list = ArrayList<Currency>()
        val head = Array(previousBaseCodes.size) { Currency() }
        val tail = ArrayList<Currency>()

        if (base == null) {
            Logger.e(LOG_TAG, "ratesAsList. base is null. aborting")
            return list
        }
        if (rates != null) {
            for ((code, rate) in rates) {
                val extendedCurrency = ExtendedCurrency.getCurrencyByISO(code)
                val currency = Currency(code, rate, extendedCurrency.name, extendedCurrency.flag)
                previousBaseCodes.indexOf(code).let {
                    if (it == -1) tail.add(currency)
                    else head[it] = currency
                }
            }
        }

        if (head.isNotEmpty()) head[0] = base
        list.addAll(head)
        list.addAll(tail)
        Logger.d(LOG_TAG, "ratesAsList. base=$base, list size=${list.size}, head size=${head.size}, " +
                "tail size=${tail.size}, rates size=${rates?.size}, previousBaseCodes size=${previousBaseCodes.size}")

        return list
    }

    companion object {
        private const val LOG_TAG = "CurrenciesResponse"
    }
}