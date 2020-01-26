package com.devtau.currencies.ui

import androidx.annotation.StringRes
import io.reactivex.functions.Action

interface StandardView {
    fun showToast(@StringRes msgId: Int)
    fun showToast(msg: String)
    fun showMsg(msgId: Int, confirmedListener: Action? = null, cancelledListener: Action? = null)
    fun showMsg(msg: String, confirmedListener: Action? = null, cancelledListener: Action? = null)
    fun isOnline(): Boolean
}