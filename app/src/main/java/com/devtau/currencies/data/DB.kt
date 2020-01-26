package com.devtau.currencies.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devtau.currencies.BuildConfig
import com.devtau.currencies.data.dao.BaseCodesDao
import com.devtau.currencies.data.dao.CurrencyDao
import com.devtau.currencies.data.model.BaseCodes
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.util.Logger
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

@Database(entities = [
    Currency::class,
    BaseCodes::class
], version = SQLHelper.DB_VERSION)
abstract class DB: RoomDatabase() {

    abstract fun currencyDao(): CurrencyDao
    abstract fun previousBaseCodesDao(): BaseCodesDao


    companion object {
        @Volatile private var INSTANCE: DB? = null
        const val LOG_TAG = "DB"

        fun getInstance(context: Context): DB =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, DB::class.java, BuildConfig.DATABASE_NAME)
                .fallbackToDestructiveMigration().build()
    }
}

fun <T> Flowable<T>?.subscribeDefault(onNext: Consumer<in T>, methodName: String): Disposable? =
    this?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe(onNext, Consumer {
        Logger.e(DB.LOG_TAG, "Error in $methodName: ${it.message}")
    })

fun Completable?.subscribeDefault(onComplete: Action?, methodName: String): Disposable? =
    this?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe(onComplete, Consumer {
        Logger.e(DB.LOG_TAG, "Error in $methodName: ${it.message}")
    })