package com.devtau.currencies.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devtau.currencies.util.Constants
import com.devtau.currencies.util.Logger
import com.mynameismidori.currencypicker.ExtendedCurrency

@Entity(tableName = "Currencies")
data class Currency(
    @PrimaryKey
    val code: String = "",
    var rate: Float = 1f,
    var name: String = "",
    var flag: Int? = null,
    var converted: Float = 0f,
    var position: Int = 0
) {

    companion object {
        private const val LOG_TAG = "Currency"

        fun getDefault(): Currency {
            val curr = ExtendedCurrency.getCurrencyByISO(Constants.CURRENCY_EUR)
            return Currency(curr.code, 1f, curr.name, curr.flag, 100f)
        }

        fun getMockList() = arrayListOf(
            getDefault(),
            Currency("AUD", 1.6159f, position = 1),
            Currency("BGN", 1.9552f, position = 2),
            Currency("BRL", 4.7903f, position = 3),
            Currency("USD", 1.163f, position = 4),
            Currency("RUB", 79.549f, position = 5)
        )

        fun calculateConverted(base: Currency?, list: List<Currency>?): ArrayList<Currency> {
            if (base == null || list == null || list.isEmpty()) {
                Logger.e(LOG_TAG, "calculateConverted. invalid data. aborting")
                return arrayListOf()
            }
            val multiplier = base.converted / base.rate
            for (next in list) next.converted = next.rate * multiplier
            return list as ArrayList<Currency>
        }
    }
}