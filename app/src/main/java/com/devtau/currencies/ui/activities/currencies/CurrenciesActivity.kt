package com.devtau.currencies.ui.activities.currencies

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.devtau.currencies.BuildConfig
import com.devtau.currencies.R
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.di.ActivityModule
import com.devtau.currencies.di.DaggerActivityComponent
import com.devtau.currencies.ui.Coordinator
import com.devtau.currencies.ui.CoordinatorImpl
import com.devtau.currencies.util.*
import com.devtau.currencies.util.Constants.CLICKS_DEBOUNCE_RATE_MS
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import kotlinx.android.synthetic.main.activity_currencies.*
import kotlinx.android.synthetic.main.list_item_currency.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CurrenciesActivity: AppCompatActivity(), CurrenciesContract.View {

    @Inject lateinit var presenter: CurrenciesContract.Presenter

    private var coordinator: Coordinator = CoordinatorImpl
    private var currenciesAdapter: CurrenciesAdapter? = null
    private var canListenToTouches = true
    private var baseAmountDisposable: Disposable? = null
    private var snackbar: Snackbar? = null
    private var snackbarDismissedByUser = false


    //<editor-fold desc="Framework overrides">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currencies)
        injectDependency()
        initList()
        showOfflineWarn(!isOnline())
    }

    override fun onStart() {
        super.onStart()
        presenter.restartLoaders()
    }

    override fun onStop() {
        super.onStop()
        presenter.stopLoaders()
        val currencies = currenciesAdapter?.provideCurrencies()
        if (currencies != null) presenter.saveCurrenciesToDB(currencies)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (BuildConfig.DEBUG) menuInflater.inflate(R.menu.menu_currencies, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.openDB -> {
            coordinator.launchDBViewerActivity(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    //</editor-fold>


    //<editor-fold desc="Interface overrides">
    override fun showToast(msgId: Int) = toast(msgId)
    override fun showToast(msg: String) = toast(msg)
    override fun showMsg(msgId: Int, confirmedListener: Action?, cancelledListener: Action?)
            = showMsg(getString(msgId), confirmedListener, cancelledListener)
    override fun showMsg(msg: String, confirmedListener: Action?, cancelledListener: Action?)
            = AppUtils.alertD(LOG_TAG, msg, this, confirmedListener, cancelledListener)
    override fun isOnline(): Boolean = AppUtils.checkConnection(this)

    override fun showCurrencies(list: List<Currency>) = dispatchMain(Action {
        currenciesAdapter?.setList(list)
        progress?.visible(list.isEmpty())
    })

    override fun showOutdatedWarn(show: Boolean, date: Long) {
        val formatter = getString(R.string.courses_relevant_formatter)
        val msg = String.format(Locale.getDefault(), formatter, AppUtils.formatDate(date))
        showSnackbar(show, msg)
    }

    override fun showOfflineWarn(show: Boolean) {
        val msg = getString(R.string.check_internet_connection)
        showSnackbar(show, msg)
    }
    //</editor-fold>


    //<editor-fold desc="Public methods">
    //</editor-fold>


    //<editor-fold desc="Private methods">
    private fun injectDependency() = DaggerActivityComponent.builder()
        .activityModule(ActivityModule(this))
        .build()
        .inject(this)

    private fun initList() {
        currenciesAdapter = CurrenciesAdapter(object: CurrenciesContract.ListListener {
            override fun rowSelected(position: Int) {
                canListenToTouches = false
                updateRow(position)
            }

            override fun editTextSelected(position: Int) {
                if (canListenToTouches) updateRow(position)
                else canListenToTouches = true
            }
        })
        listView?.layoutManager = CustomLinearLayoutManager(this)
        listView?.adapter = currenciesAdapter

        listView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    canListenToTouches = true
                } else {
                    baseAmountDisposable?.dispose()
                    canListenToTouches = false
                    AppUtils.toggleSoftInput(true, null, this@CurrenciesActivity)
                }
            }
        })
    }

    private fun updateRow(position: Int) {
        presenter.updateBase(position)
        currenciesAdapter?.notifyItemMoved(position, 0)
        listView.scrollToPosition(0)
        val holder = listView.findViewHolderForAdapterPosition(0) as CurrenciesAdapter.CurrenciesViewHolder?
        holder?.let { initBaseAmountListener(it.currencyCourse) }
    }

    private fun initBaseAmountListener(currencyCourse: EditText) {
        Logger.d(LOG_TAG, "initBaseAmountListener")
        baseAmountDisposable?.dispose()
        baseAmountDisposable = RxTextView.textChanges(currencyCourse)
            .debounce(CLICKS_DEBOUNCE_RATE_MS, TimeUnit.MILLISECONDS)
            .map(CharSequence::toString)
            .map(String::toFloatOrZero)
            .skip(1)
            .subscribe { presenter.updateBaseAmount(it) }
        AppUtils.toggleSoftInput(true, currencyCourse, this)
    }

    private fun showSnackbar(show: Boolean, msg: String) = dispatchMain(Action {
        when {
            show && snackbar?.isShown != true && !snackbarDismissedByUser -> {
                snackbar = Snackbar.make(root, msg, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.close) { snackbarDismissedByUser = true }
                snackbar?.show()
            }
            show && snackbar?.isShown == true -> snackbar?.setText(msg)
            !show && snackbar?.isShown == true -> snackbar?.dismiss()
            else -> {}
        }
    })
    //</editor-fold>


    companion object {
        private const val LOG_TAG = "CurrenciesActivity"
    }
}

