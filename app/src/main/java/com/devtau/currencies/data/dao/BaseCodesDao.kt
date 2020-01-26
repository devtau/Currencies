package com.devtau.currencies.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devtau.currencies.data.model.BaseCodes
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface BaseCodesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(codes: BaseCodes): Completable?

    @Query("SELECT * FROM PreviousBaseCodes WHERE id = 0")
    fun getList(): Flowable<BaseCodes>

    @Query("DELETE FROM PreviousBaseCodes")
    fun delete(): Completable
}