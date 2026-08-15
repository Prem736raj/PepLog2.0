package com.appvexis.peptidetracker.feature.onboarding

import app.cash.turbine.test
import com.appvexis.peptidetracker.core.datastore.UserPreferencesDataSource
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val userPreferencesDataSource: UserPreferencesDataSource = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OnboardingViewModel(userPreferencesDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_isDefault() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.selectedGoals.isEmpty())
            assertFalse(initialState.isOnboardingCompleted)
        }
    }

    @Test
    fun toggleGoal_addsGoalToSelectedList() = runTest {
        viewModel.uiState.test {
            // Initial emission
            var state = awaitItem()
            assertTrue(state.selectedGoals.isEmpty())

            // Toggle FAT_LOSS goal
            viewModel.toggleGoal(OnboardingGoal.FAT_LOSS)
            state = awaitItem()
            assertTrue(state.selectedGoals.contains(OnboardingGoal.FAT_LOSS))
            assertEquals(1, state.selectedGoals.size)

            // Toggle HEALING goal
            viewModel.toggleGoal(OnboardingGoal.HEALING)
            state = awaitItem()
            assertTrue(state.selectedGoals.contains(OnboardingGoal.FAT_LOSS))
            assertTrue(state.selectedGoals.contains(OnboardingGoal.HEALING))
            assertEquals(2, state.selectedGoals.size)

            // Toggle FAT_LOSS again (should remove it)
            viewModel.toggleGoal(OnboardingGoal.FAT_LOSS)
            state = awaitItem()
            assertFalse(state.selectedGoals.contains(OnboardingGoal.FAT_LOSS))
            assertTrue(state.selectedGoals.contains(OnboardingGoal.HEALING))
            assertEquals(1, state.selectedGoals.size)
        }
    }

    @Test
    fun completeOnboarding_savesToDataSourceAndUpdatesUiState() = runTest {
        viewModel.toggleGoal(OnboardingGoal.FAT_LOSS)
        viewModel.toggleGoal(OnboardingGoal.HEALING)

        viewModel.completeOnboarding()

        // Verify DataStore updates
        coVerify(exactly = 1) {
            userPreferencesDataSource.setSelectedGoals(setOf("FAT_LOSS", "HEALING"))
            userPreferencesDataSource.setOnboardingCompleted(true)
        }

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isOnboardingCompleted)
        }
    }
}
