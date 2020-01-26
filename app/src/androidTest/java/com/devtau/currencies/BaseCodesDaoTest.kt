package com.devtau.currencies

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devtau.currencies.data.DB
import com.devtau.currencies.data.dao.BaseCodesDao
import com.devtau.currencies.data.model.BaseCodes
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaseCodesDaoTest {

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var db: DB
    private lateinit var dao: BaseCodesDao


    @Before fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DB::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.previousBaseCodesDao()
    }

    @After fun closeDb() = db.close()


    @Test fun insertAndGet() {
        dao.insert(MOCK_LIST)?.blockingAwait()
        dao.getList().test().assertValue { MOCK_LIST.codes == it.codes }
    }

    @Test fun deleteAllAndGet() {
        dao.insert(MOCK_LIST)?.blockingAwait()
        dao.delete().blockingAwait()
        dao.getList().test().assertNoValues()
    }


    companion object {
        private val MOCK_LIST = BaseCodes.getMockList()
    }
}