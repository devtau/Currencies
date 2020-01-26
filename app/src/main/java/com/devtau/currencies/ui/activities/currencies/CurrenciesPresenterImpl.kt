package com.devtau.currencies.ui.activities.currencies

import com.devtau.currencies.data.dao.BaseCodesDao
import com.devtau.currencies.data.dao.CurrencyDao
import com.devtau.currencies.data.model.BaseCodes
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.data.subscribeDefault
import com.devtau.currencies.rest.NetworkLayer
import com.devtau.currencies.util.Constants.COURSES_REFRESH_TIMEOUT_MS
import com.devtau.currencies.util.Logger
import com.devtau.currencies.util.PreferencesManager
import com.devtau.currencies.util.Serializer
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class CurrenciesPresenterImpl(
    private val view: CurrenciesContract.View,
    private val currencyDao: CurrencyDao,
    private val baseCodesDao: BaseCodesDao,
    private val networkLayer: NetworkLayer,
    private val prefs: PreferencesManager
): CurrenciesContract.Presenter, CurrenciesContract.BaseInfoProvider {

    override val subscriptions = CompositeDisposable()
    var currencies = arrayListOf<Currency>()
    var isReadyToRequestCourses = false
    var baseCurrency = Currency.getDefault()
    var previousBaseCodes = arrayListOf(baseCurrency.code)


    //<editor-fold desc="interface overrides">
    override fun restartLoaders(testMode: Boolean) {
        isReadyToRequestCourses = false
        var currenciesLoaded = false
        var baseCodesLoaded = false

        var currenciesDisposable: Disposable? = null
        currenciesDisposable = currencyDao.getList().subscribeDefault(Consumer {
            Logger.d(LOG_TAG, "got new currencies=$it")
            if (it.isNotEmpty()) baseCurrency = it[0]
            currenciesLoaded = true
            isReadyToRequestCourses = currenciesLoaded && baseCodesLoaded
            currenciesDisposable?.dispose()
        }, "currencyDao.getList")

        var baseCodesDisposable: Disposable? = null
        baseCodesDisposable = baseCodesDao.getList().subscribeDefault(Consumer { baseCodes ->
            Logger.d(LOG_TAG, "got new base codes=${baseCodes.codes}")
            Serializer.deserializeListOfStrings(baseCodes.codes)?.let {
                Logger.d(LOG_TAG, "deserialized list. size=${it.size}")
                previousBaseCodes = it as ArrayList<String>
                baseCodesLoaded = true
                isReadyToRequestCourses = currenciesLoaded && baseCodesLoaded
            }
            baseCodesDisposable?.dispose()
        }, "baseCodesDao.getList")

        disposeOnStop(currencyDao.getList().subscribeDefault(Consumer {
            if (it.isEmpty()) return@Consumer
            Logger.d(LOG_TAG, "got new currencies list. size=${it.size}, base=${it[0]}")
            currencies.clear()
            currencies.addAll(it)
            view.showCurrencies(currencies)
            updateLastSyncDate(prefs.lastSyncDate)
        }, "currencyDao.getList"))

        if (!testMode) disposeOnStop(Flowable.interval(0L, COURSES_REFRESH_TIMEOUT_MS, TimeUnit.MILLISECONDS, Schedulers.io())
            .subscribeDefault(Consumer { getCoursesFromBackend() }, "Flowable.interval"))
    }

    override fun stopLoaders() = subscriptions.clear()

    override fun updateBase(index: Int) {
        if (currencies.isEmpty()) return
        baseCurrency = currencies[index]
        Logger.d(LOG_TAG, "updateBase. new index=$index, new baseCurrency=$baseCurrency")
        currencies.remove(baseCurrency)
        currencies.add(0, baseCurrency)
        previousBaseCodes.remove(baseCurrency.code)
        previousBaseCodes.add(0, baseCurrency.code)
        handleOfflineMode(view.isOnline())
    }

    override fun updateBaseAmount(amount: Float) {
        Logger.d(LOG_TAG, "updateBaseAmount. new amount=$amount")
        baseCurrency.converted = amount
        handleOfflineMode(view.isOnline())
    }

    override fun saveCurrenciesToDB(newList: List<Currency>) {
        Logger.d(LOG_TAG, "saveCurrenciesToDB. newList size=${newList.size}")
        for (i in newList.indices) newList[i].position = i
        currencyDao.insert(newList).subscribeDefault(Action { Logger.d(LOG_TAG, "currencies saved") }, "currencyDao.insert")
        currencies.clear()
        currencies.addAll(newList)
        val baseCodesSerialized = BaseCodes(codes = Serializer.serializeList(previousBaseCodes))
        baseCodesDao.insert(baseCodesSerialized).subscribeDefault(Action { Logger.d(LOG_TAG, "base codes saved") }, "baseCodesDao.insert")
    }

    override fun provideBase() = baseCurrency
    override fun providePreviousBaseCodes() = previousBaseCodes
    //</editor-fold>


    //<editor-fold desc="private methods">
    private fun getCoursesFromBackend() {
        if (!isReadyToRequestCourses || !view.isOnline()) return
        networkLayer.getCourses(this, Consumer {
            Logger.d(LOG_TAG, "got new courses from backend=$it")
            baseCurrency.rate = 1f
            currencies.clear()
            currencies.addAll(it)
            view.showCurrencies(currencies)
        }, Consumer {
            prefs.lastSyncDate = it
            updateLastSyncDate(it)
        })
    }

    private fun disposeOnStop(disposable: Disposable?) {
        if (disposable == null) {
            Logger.w(LOG_TAG, "disposeOnStop. disposable is null. aborting")
        } else {
            subscriptions.add(disposable)
            Logger.d(LOG_TAG, "disposeOnStop. new subscriptions size=${subscriptions.size()}")
        }
    }

    private fun updateLastSyncDate(date: Long) {
        val now = Calendar.getInstance()
        val lastSyncDate = Calendar.getInstance()
        lastSyncDate.timeInMillis = date
        now.add(Calendar.DAY_OF_MONTH, -1)
        view.showOutdatedWarn(lastSyncDate.before(now), lastSyncDate.timeInMillis)
    }

    private fun handleOfflineMode(isOnline: Boolean) {
        if (isOnline) {
            view.showOfflineWarn(false)
        } else {
            currencies = Currency.calculateConverted(baseCurrency, currencies)
            view.showCurrencies(currencies)
            view.showOfflineWarn(true)
        }
    }
    //</editor-fold>


    companion object {
        private const val LOG_TAG = "CurrenciesPresenter"
    }
}