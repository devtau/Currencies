package com.devtau.currencies.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PreviousBaseCodes")
data class BaseCodes(
    @PrimaryKey
    val id: Long = 0L,
    var codes: String
) {
    companion object {
        fun getMockList(): BaseCodes {
            val mockCurrencies = Currency.getMockList()
            val codes = "[${mockCurrencies[0].code}, ${mockCurrencies[1].code}, ${mockCurrencies[2].code}" +
                    ", ${mockCurrencies[3].code}, ${mockCurrencies[4].code}, ${mockCurrencies[5].code}]"
            return BaseCodes(codes = codes)
        }
    }
}