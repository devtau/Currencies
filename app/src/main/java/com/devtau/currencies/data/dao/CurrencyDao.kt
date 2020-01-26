package com.devtau.currencies.data.dao

import androidx.room.*
import com.devtau.currencies.data.model.Currency
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currency: Currency): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<Currency>): Completable?

    @Query("SELECT * FROM Currencies WHERE code = :code")
    fun getByCode(code: String): Flowable<Currency>

    @Query("SELECT * FROM Currencies ORDER BY position")
    fun getList(): Flowable<List<Currency>>

    @Delete
    fun delete(list: List<Currency>): Completable

    @Query("DELETE FROM Currencies")
    fun delete(): Completable
}