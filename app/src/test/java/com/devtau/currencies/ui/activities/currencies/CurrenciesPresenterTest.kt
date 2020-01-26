package com.devtau.currencies.ui.activities.currencies

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.devtau.currencies.data.dao.BaseCodesDao
import com.devtau.currencies.data.dao.CurrencyDao
import com.devtau.currencies.data.model.BaseCodes
import com.devtau.currencies.data.model.Currency
import com.devtau.currencies.rest.NetworkLayer
import com.devtau.currencies.util.PreferencesManager
import com.devtau.currencies.util.Serializer
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.disposables.Disposable
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class CurrenciesPresenterTest {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    @Mock private lateinit var view: CurrenciesContract.View
    @Mock private lateinit var currencyDao: CurrencyDao
    @Mock private lateinit var baseCodesDao: BaseCodesDao
    @Mock private lateinit var networkLayer: NetworkLayer
    @Mock private lateinit var prefs: PreferencesManager

    private lateinit var presenter: CurrenciesPresenterImpl


    @Before fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = CurrenciesPresenterImpl(view, currencyDao, baseCodesDao, networkLayer, prefs)
    }

    @Test fun restartLoaders() {
        Mockito.`when`(currencyDao.getList()).thenReturn(Flowable.just(MOCK_CURRENCIES))
        Mockito.`when`(baseCodesDao.getList()).thenReturn(Flowable.just(MOCK_BASE_CODES))
        assertEquals(0, presenter.currencies.size)
        assertEquals(false, presenter.isReadyToRequestCourses)
        presenter.restartLoaders(true)

        assertEquals(6, presenter.currencies.size)
        assertEquals(true, presenter.isReadyToRequestCourses)
        verify(currencyDao, times(2)).getList()
        assertEquals(1, presenter.subscriptions.size())
    }

    @Test fun stopLoaders() {
        Mockito.`when`(currencyDao.getList()).thenReturn(Flowable.just(MOCK_CURRENCIES))
        presenter.restartLoaders(true)
        assertEquals(1, presenter.subscriptions.size())
        presenter.stopLoaders()
        assertEquals(0, presenter.subscriptions.size())
    }

    @Test fun updateBase() {
        presenter.currencies = Currency.getMockList()
        presenter.updateBase(1)
        assertEquals(Currency.getMockList()[1].code, presenter.currencies[0].code)
        assertEquals(Currency.getMockList()[1].code, presenter.previousBaseCodes[0])
    }

    @Test fun updateBaseAmount() {
        presenter.updateBaseAmount(200f)
        assertEquals(1f, presenter.baseCurrency.rate)
        assertEquals(200f, presenter.baseCurrency.converted)
    }

    @Test fun saveCurrenciesToDB() {
        presenter.saveCurrenciesToDB(MOCK_CURRENCIES)
        verify(currencyDao, times(1)).insert(MOCK_CURRENCIES)
        verify(baseCodesDao, times(1)).insert(BaseCodes(codes = Serializer.serializeList(listOf(Currency.getDefault().code))))
        for (i in presenter.currencies.indices) {
            val next = presenter.currencies[i]
            assertEquals(i, next.position)
        }
    }


    companion object {
        private val MOCK_CURRENCIES = Currency.getMockList()
        private val MOCK_BASE_CODES = BaseCodes.getMockList()

        //necessary if code under test involves AndroidSchedulers
        @BeforeClass @JvmStatic fun setUpRxSchedulers() {
            val immediate = object: Scheduler() {
                override fun createWorker() = ExecutorScheduler.ExecutorWorker(Executor { it.run() }, true)
                //prevents StackOverflowErrors when scheduling with a delay
                override fun scheduleDirect(run: Runnable, delay: Long, unit: TimeUnit): Disposable {
                    return super.scheduleDirect(run, 0, unit)
                }
            }

            RxJavaPlugins.setInitIoSchedulerHandler { immediate }
            RxJavaPlugins.setInitComputationSchedulerHandler { immediate }
            RxJavaPlugins.setInitNewThreadSchedulerHandler { immediate }
            RxJavaPlugins.setInitSingleSchedulerHandler { immediate }
            RxAndroidPlugins.setInitMainThreadSchedulerHandler { immediate }
        }
    }
}