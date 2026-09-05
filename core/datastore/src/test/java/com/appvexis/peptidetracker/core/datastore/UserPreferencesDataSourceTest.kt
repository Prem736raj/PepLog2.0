package com.appvexis.peptidetracker.core.datastore

import app.cash.turbine.test
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesDataSourceTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var dataSource: UserPreferencesDataSource

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "user_prefs.preferences_pb") }
        )
        dataSource = UserPreferencesDataSource(dataStore)
    }

    @Test
    fun isOnboardingCompleted_defaultsToFalse() = runTest(testDispatcher) {
        dataSource.isOnboardingCompleted.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun selectedGoals_defaultsToEmpty() = runTest(testDispatcher) {
        dataSource.selectedGoals.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun isAppLockEnabled_defaultsToFalse() = runTest(testDispatcher) {
        dataSource.isAppLockEnabled.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun setOnboardingCompleted_updatesFlow() = runTest(testDispatcher) {
        dataSource.isOnboardingCompleted.test {
            assertFalse(awaitItem())
            
            dataSource.setOnboardingCompleted(true)
            assertTrue(awaitItem())
            
            dataSource.setOnboardingCompleted(false)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun setSelectedGoals_updatesFlow() = runTest(testDispatcher) {
        dataSource.selectedGoals.test {
            assertTrue(awaitItem().isEmpty())
            
            val goals = setOf("FAT_LOSS", "HEALING")
            dataSource.setSelectedGoals(goals)
            assertEquals(goals, awaitItem())
        }
    }

    @Test
    fun setAppLockEnabled_updatesFlow() = runTest(testDispatcher) {
        dataSource.isAppLockEnabled.test {
            assertFalse(awaitItem())

            dataSource.setAppLockEnabled(true)
            assertTrue(awaitItem())

            dataSource.setAppLockEnabled(false)
            assertFalse(awaitItem())
        }
    }
}
