package com.devtau.currencies

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devtau.currencies.data.DB
import com.devtau.currencies.data.dao.CurrencyDao
import com.devtau.currencies.data.model.Currency
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurrencyDaoTest {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var db: DB
    private lateinit var dao: CurrencyDao


    @Before fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DB::class.java).allowMainThreadQueries().build()
        dao = db.currencyDao()
    }

    @After fun closeDb() = db.close()


    @Test fun insertAndGet() {
        dao.insert(listOf(MOCK_CURRENCY))?.blockingAwait()
        dao.getByCode(MOCK_CURRENCY.code).test().assertValue { MOCK_CURRENCY.code == it.code }
    }


    @Test fun testQtyAndSortOrder() {
        dao.insert(listOf(MOCK_LIST[2], MOCK_LIST[1], MOCK_LIST[0], MOCK_LIST[3], MOCK_LIST[5], MOCK_LIST[4]))?.blockingAwait()
        dao.getList().test().assertValue { list ->
            val sorted = arrayListOf<Currency>()
            sorted.addAll(list)
            sorted.sortBy { it.position }
            6 == list.size
                    && sorted[0].code == list[0].code
                    && sorted[1].code == list[1].code
                    && sorted[2].code == list[2].code
                    && sorted[3].code == list[3].code
                    && sorted[4].code == list[4].code
                    && sorted[5].code == list[5].code
        }
    }

    @Test fun deleteAllAndGet() {
        dao.insert(MOCK_CURRENCY).blockingAwait()
        dao.delete().blockingAwait()
        dao.getByCode(MOCK_CURRENCY.code).test().assertNoValues()
    }

    @Test fun deleteListAndGet() {
        dao.insert(MOCK_LIST)?.blockingAwait()
        dao.delete(MOCK_LIST).blockingAwait()
        dao.getByCode(MOCK_CURRENCY.code).test().assertNoValues()
    }


    companion object {
        private val MOCK_CURRENCY = Currency.getDefault()
        private val MOCK_LIST = Currency.getMockList()
    }
}