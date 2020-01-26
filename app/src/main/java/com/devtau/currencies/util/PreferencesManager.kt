package com.devtau.currencies.util

import android.content.Context
import androidx.preference.PreferenceManager

open class PreferencesManager(context: Context) {

    private var prefs = PreferenceManager.getDefaultSharedPreferences(context)


    var lastSyncDate: Long
        get() = prefs?.getLong(LAST_SYNC_DATE, 0L) ?: 0L
        set(value) {
            val editor = prefs?.edit()
            editor?.putLong(LAST_SYNC_DATE, value)
            editor?.apply()
        }

    fun clear() = prefs?.edit()?.clear()?.apply()


    companion object {
        private const val LAST_SYNC_DATE = "lastSyncDate"
    }
}