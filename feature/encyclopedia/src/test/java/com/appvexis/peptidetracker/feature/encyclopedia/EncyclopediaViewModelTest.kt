package com.appvexis.peptidetracker.feature.encyclopedia

import app.cash.turbine.test
import com.appvexis.peptidetracker.core.model.Peptide
import com.appvexis.peptidetracker.core.model.repository.PeptideRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EncyclopediaViewModelTest {

    private val peptideRepository: PeptideRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakePeptides = listOf(
        Peptide(
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
        ),
        Peptide(
            id = "semaglutide",
            name = "Semaglutide",
            category = "Weight Loss / Metabolic",
            description = "Weight loss peptide",
            halfLifeHours = 168.0,
            halfLifeDisplay = "7 days",
            adminRoute = "Subcutaneous",
            typicalFrequency = "Weekly",
            typicalDoseRange = "0.25-2.4mg",
            storageInfo = "Fridge",
            sideEffects = listOf("Nausea"),
            synergies = emptyList(),
            contraindications = listOf("Thyroid cancer"),
            isBookmarked = true
        )
    )

    private val peptidesFlow = MutableStateFlow(fakePeptides)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { peptideRepository.getAllPeptides() } returns peptidesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_loadsAllPeptides() = runTest {
        val viewModel = EncyclopediaViewModel(peptideRepository)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.peptides.size)
            assertEquals("BPC-157", state.peptides[0].name)
            assertEquals("Semaglutide", state.peptides[1].name)
        }
    }

    @Test
    fun onSearchQueryChanged_filtersPeptidesByNameAndDescription() = runTest {
        val viewModel = EncyclopediaViewModel(peptideRepository)
        viewModel.uiState.test {
            // Initial load
            var state = awaitItem()
            assertEquals(2, state.peptides.size)

            // Search "bpc"
            viewModel.onSearchQueryChanged("bpc")
            state = awaitItem()
            assertEquals(1, state.peptides.size)
            assertEquals("bpc157", state.peptides[0].id)

            // Search "weight" (matches Semaglutide description)
            viewModel.onSearchQueryChanged("weight")
            state = awaitItem()
            assertEquals(1, state.peptides.size)
            assertEquals("semaglutide", state.peptides[0].id)
        }
    }

    @Test
    fun onCategorySelected_filtersPeptidesByCategory() = runTest {
        val viewModel = EncyclopediaViewModel(peptideRepository)
        viewModel.uiState.test {
            // Initial load
            var state = awaitItem()
            assertEquals(2, state.peptides.size)

            // Select "Healing & Recovery"
            viewModel.onCategorySelected("Healing & Recovery")
            state = awaitItem()
            assertEquals(1, state.peptides.size)
            assertEquals("bpc157", state.peptides[0].id)

            // Select "Weight Loss / Metabolic"
            viewModel.onCategorySelected("Weight Loss / Metabolic")
            state = awaitItem()
            assertEquals(1, state.peptides.size)
            assertEquals("semaglutide", state.peptides[0].id)
        }
    }

    @Test
    fun toggleBookmarkFilter_filtersBookmarkedOnly() = runTest {
        val viewModel = EncyclopediaViewModel(peptideRepository)
        viewModel.uiState.test {
            // Initial load
            var state = awaitItem()
            assertEquals(2, state.peptides.size)

            // Show Bookmarks only
            viewModel.toggleBookmarkFilter()
            state = awaitItem()
            assertEquals(1, state.peptides.size)
            assertEquals("semaglutide", state.peptides[0].id)

            // Toggle back to show all
            viewModel.toggleBookmarkFilter()
            state = awaitItem()
            assertEquals(2, state.peptides.size)
        }
    }

    @Test
    fun toggleBookmark_triggersRepositoryUpdate() = runTest {
        val viewModel = EncyclopediaViewModel(peptideRepository)
        viewModel.toggleBookmark("bpc157", false)

        coVerify(exactly = 1) {
            peptideRepository.setBookmarked("bpc157", true)
        }
    }
}
