package com.devtau.currencies.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devtau.currencies.BuildConfig

class SQLHelper private constructor(context: Context):
        SQLiteOpenHelper(context, BuildConfig.DATABASE_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}


    companion object {
        const val DB_VERSION = 1

        @Synchronized
        fun getInstance(context: Context): SQLHelper = SQLHelper(context)
    }
}