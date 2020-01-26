package com.devtau.currencies.util

import android.util.Log
import com.devtau.currencies.BuildConfig

object Logger {

    fun v(tag: String, msg: String?) { if (BuildConfig.WITH_LOGS && msg != null) Log.v(tag, msg) }
    fun d(tag: String, msg: String?) { if (BuildConfig.WITH_LOGS && msg != null) Log.d(tag, msg) }
    fun i(tag: String, msg: String?) { if (BuildConfig.WITH_LOGS && msg != null) Log.i(tag, msg) }
    fun w(tag: String, msg: String?) { if (BuildConfig.WITH_LOGS && msg != null) Log.w(tag, msg) }
    fun e(tag: String, msg: String?) { if (msg != null) Log.e(tag, msg) }
}