package com.appvexis.peptidetracker.feature.encyclopedia

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.PeptideDetailRoute
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PeptideDetailViewModelTest {

    private val peptideRepository: PeptideRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakePeptide = Peptide(
        id = "bpc157",
        name = "BPC-157",
        category = "Healing & Recovery",
        description = "Healing peptide description",
        halfLifeHours = 4.0,
        halfLifeDisplay = "4 hours",
        adminRoute = "Subcutaneous",
        typicalFrequency = "1-2x daily",
        typicalDoseRange = "250-500mcg",
        storageInfo = "Fridge",
        sideEffects = emptyList(),
        synergies = listOf("TB-500"),
        contraindications = listOf("Cancer"),
        isBookmarked = false
    )

    private val peptideFlow = MutableStateFlow<Peptide?>(fakePeptide)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock the static toRoute extension function on SavedStateHandle to prevent Bundle/Parcel crash on JVM
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        
        every { peptideRepository.getPeptideById("bpc157") } returns peptideFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
    }

    @Test
    fun loadPeptideDetails_loadsFromRepository() = runTest {
        val savedStateHandle: SavedStateHandle = mockk()
        every { savedStateHandle.toRoute<PeptideDetailRoute>() } returns PeptideDetailRoute(peptideId = "bpc157")
        
        val viewModel = PeptideDetailViewModel(peptideRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.peptide)
            assertEquals("bpc157", state.peptide?.id)
            assertEquals("BPC-157", state.peptide?.name)
        }
    }

    @Test
    fun toggleBookmark_triggersRepositoryUpdate() = runTest {
        val savedStateHandle: SavedStateHandle = mockk()
        every { savedStateHandle.toRoute<PeptideDetailRoute>() } returns PeptideDetailRoute(peptideId = "bpc157")
        
        val viewModel = PeptideDetailViewModel(peptideRepository, savedStateHandle)

        viewModel.toggleBookmark(false)

        coVerify(exactly = 1) {
            peptideRepository.setBookmarked("bpc157", true)
        }
    }
}
