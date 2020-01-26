package com.devtau.currencies.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.devtau.currencies.util.Constants.DATE_FORMATTER_TO_SHOW
import com.devtau.currencies.util.Constants.SERVER_DATE_FORMATTER
import io.reactivex.functions.Action
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {

    const val LOG_TAG = "AppUtils"


    fun checkConnection(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return false
        return networkInfo.isConnectedOrConnecting
    }

    fun formatDate(timeInMillis: Long?): String {
        val date = Calendar.getInstance()
        if (timeInMillis != null) date.timeInMillis = timeInMillis
        return formatDate(date)
    }
    fun formatDate(cal: Calendar): String = formatAnyDate(cal, DATE_FORMATTER_TO_SHOW)

    private fun formatAnyDate(cal: Calendar, formatter: String): String =
        SimpleDateFormat(formatter, Locale.getDefault()).format(cal.time)

    fun parseDate(date: String?): Calendar {
        val calendar = Calendar.getInstance()
        val inputDf = SimpleDateFormat(SERVER_DATE_FORMATTER, Locale.getDefault())
        try {
            calendar.timeInMillis = inputDf.parse(date).time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return calendar
    }

    fun toggleSoftInput(show: Boolean, field: EditText?, activity: AppCompatActivity?) {
        Logger.d(LOG_TAG, "toggleSoftInput. " + (if (show) "show" else "hide")
                + ", field " + (if (field == null) "is null" else "ok")
                + ", activity " + (if (activity == null) "is null" else "ok"))
        if (show) {
            field?.requestFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT)
        } else {
            field?.clearFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            val windowToken = field?.windowToken ?: activity?.currentFocus?.windowToken ?: return
            imm?.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun alertD(logTag: String?, msg: String, context: Context?, confirmedListener: Action? = null, cancelledListener: Action? = null) {
        context ?: return
        Logger.d(logTag ?: LOG_TAG, msg)
        try {
            val builder = AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    confirmedListener?.run()
                    dialog.dismiss()
                }
            if (cancelledListener != null) {
                builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    cancelledListener.run()
                    dialog.dismiss()
                }
            }

            builder.setMessage(msg).show()
        } catch (e: WindowManager.BadTokenException) {
            Logger.e(logTag ?: LOG_TAG, "in alertD. cannot show dialog")
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context?.toast(@StringRes msgId: Int) { this?.toast(this.getString(msgId)) }
fun Context?.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context?.toastLong(@StringRes msgId: Int) { this?.toastLong(this.getString(msgId)) }
fun Context?.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun dispatchMain(block: Action) {
    Handler(Looper.getMainLooper()).post {
        try {
            block.run()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun View.visible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun String.toFloatOrZero(): Float {
    var parsed = 0f
    if (this.isNotEmpty()) try {
        parsed = this.toFloat()
    } catch (e: NumberFormatException) {/*NOP*/}
    return parsed
}

fun Float.round() = String.format(Locale.getDefault(), if (this %1 == 0f) "%.0f" else "%.2f", this)