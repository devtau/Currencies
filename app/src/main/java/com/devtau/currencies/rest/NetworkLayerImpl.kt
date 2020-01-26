package com.devtau.currencies.rest

import com.devtau.currencies.BuildConfig
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.ui.StandardView
import com.devtau.currencies.ui.activities.currencies.CurrenciesContract
import com.devtau.currencies.util.AppUtils
import com.devtau.currencies.util.Logger
import com.google.gson.GsonBuilder
import io.reactivex.functions.Consumer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkLayerImpl(val view: StandardView): NetworkLayer {

    private var httpClientLogging = buildClient(true)
    private var httpClientNotLogging = buildClient(false)


    //<editor-fold desc="interface overrides">
    override fun getCourses(
        baseInfoProvider: CurrenciesContract.BaseInfoProvider,
        listener: Consumer<List<Currency>>,
        dateListener: Consumer<Long>) {
        val baseCode = baseInfoProvider.provideBase()?.code ?: return
        getBackendApiClient(true).getCourses(baseCode)
            .enqueue(object: BaseCallback<CurrenciesResponse>(view) {
                override fun processBody(responseBody: CurrenciesResponse?) {
                    val list = responseBody?.ratesAsList(baseInfoProvider.provideBase(), baseInfoProvider.providePreviousBaseCodes())
                    if (list != null && list.isNotEmpty()) list[0].rate = 1f
                    val convertedList = Currency.calculateConverted(baseInfoProvider.provideBase(), list)
                    if (baseInfoProvider.provideBase()?.code == responseBody?.base) {
                        listener.accept(convertedList)
                        dateListener.accept(AppUtils.parseDate(responseBody?.date).timeInMillis)
                    } else {
                        Logger.w(LOG_TAG, "current base differs from server response. skipping list")
                    }
                }
            })
    }
    //</editor-fold>


    //<editor-fold desc="public methods">
    //</editor-fold>


    //<editor-fold desc="private methods">
    private fun getBackendApiClient(loggerNeeded: Boolean): BackendAPI = Retrofit.Builder()
        .baseUrl(BuildConfig.REVOLUT_SERVER)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .client(if (loggerNeeded) httpClientLogging else httpClientNotLogging)
        .build()
        .create(BackendAPI::class.java)

    private fun buildClient(loggerNeeded: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
        if (BuildConfig.WITH_LOGS && loggerNeeded) {
            builder.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        return builder.build()
    }
    //</editor-fold>


    companion object {
        private const val LOG_TAG = "NetworkLayer"
        private const val TIMEOUT_CONNECT = 10L
        private const val TIMEOUT_READ = 60L
        private const val TIMEOUT_WRITE = 120L
    }
}