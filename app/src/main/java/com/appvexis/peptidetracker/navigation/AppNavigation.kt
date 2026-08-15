package com.appvexis.peptidetracker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.appvexis.peptidetracker.MainActivityUiState
import com.appvexis.peptidetracker.MainViewModel
import com.appvexis.peptidetracker.core.model.DashboardRoute
import com.appvexis.peptidetracker.core.model.InsightsRoute
import com.appvexis.peptidetracker.core.model.MoreRoute
import com.appvexis.peptidetracker.core.model.OnboardingRoute
import com.appvexis.peptidetracker.core.model.ProtocolsRoute
import com.appvexis.peptidetracker.core.model.SplashRoute
import com.appvexis.peptidetracker.core.model.LegalRoute
import com.appvexis.peptidetracker.core.ui.components.AnimatedBottomBar
import com.appvexis.peptidetracker.core.ui.components.PepLogLoadingState
import com.appvexis.peptidetracker.core.ui.components.PepLogTopBar
import com.appvexis.peptidetracker.core.ui.splash.SplashScreen
import com.appvexis.peptidetracker.feature.dashboard.DashboardScreen
import com.appvexis.peptidetracker.feature.onboarding.OnboardingScreen
import com.appvexis.peptidetracker.feature.protocol.ProtocolScreen
import com.appvexis.peptidetracker.feature.reports.InsightsScreen
import com.appvexis.peptidetracker.feature.settings.MoreScreen
import com.appvexis.peptidetracker.feature.settings.LegalScreen
import com.appvexis.peptidetracker.core.model.EncyclopediaRoute
import com.appvexis.peptidetracker.core.model.PeptideDetailRoute
import com.appvexis.peptidetracker.core.model.CalculatorRoute
import com.appvexis.peptidetracker.core.model.ProtocolDetailRoute
import com.appvexis.peptidetracker.core.model.CreateProtocolRoute
import com.appvexis.peptidetracker.core.model.AddCompoundRoute
import com.appvexis.peptidetracker.feature.encyclopedia.EncyclopediaScreen
import com.appvexis.peptidetracker.feature.encyclopedia.PeptideDetailScreen
import com.appvexis.peptidetracker.feature.calculator.CalculatorScreen
import com.appvexis.peptidetracker.feature.protocol.ProtocolListScreen
import com.appvexis.peptidetracker.feature.protocol.ProtocolDetailScreen
import com.appvexis.peptidetracker.feature.protocol.CreateProtocolScreen
import com.appvexis.peptidetracker.feature.protocol.AddCompoundScreen
import com.appvexis.peptidetracker.core.model.DailyLogRoute
import com.appvexis.peptidetracker.core.model.DoseHistoryRoute
import com.appvexis.peptidetracker.core.model.InjectionTrackerRoute
import com.appvexis.peptidetracker.core.model.InventoryRoute
import com.appvexis.peptidetracker.feature.log.DailyLogScreen
import com.appvexis.peptidetracker.feature.log.DoseHistoryScreen
import com.appvexis.peptidetracker.feature.injection.InjectionTrackerScreen
import com.appvexis.peptidetracker.feature.inventory.InventoryScreen
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.appvexis.peptidetracker.core.model.ProgressRoute
import com.appvexis.peptidetracker.core.model.PhotoComparisonRoute
import com.appvexis.peptidetracker.core.model.PKVisualizerRoute
import com.appvexis.peptidetracker.core.model.HealthConnectRoute
import com.appvexis.peptidetracker.core.model.PaywallRoute
import com.appvexis.peptidetracker.core.model.BackupSettingsRoute
import com.appvexis.peptidetracker.feature.progress.ProgressScreen
import com.appvexis.peptidetracker.feature.progress.PhotoComparisonScreen
import com.appvexis.peptidetracker.feature.pkcurves.PKVisualizerScreen
import com.appvexis.peptidetracker.feature.health.HealthConnectScreen
import com.appvexis.peptidetracker.feature.paywall.PaywallScreen
import com.appvexis.peptidetracker.feature.settings.BackupSettingsScreen

/**
 * Main application shell managing bottom navigation bar state and transitions.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        MainActivityUiState.Loading -> {
            PepLogLoadingState()
        }
        is MainActivityUiState.Success -> {
            AppNavigationContent(
                isOnboardingCompleted = state.isOnboardingCompleted,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun AppNavigationContent(
    isOnboardingCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Extract current route from backstack
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Check if we should show top/bottom bars (hide on sub-screens)
    val showBars = currentRoute != null && 
            !currentRoute.contains("OnboardingRoute") && 
            !currentRoute.contains("SplashRoute") &&
            !currentRoute.contains("PeptideDetailRoute") &&
            !currentRoute.contains("CalculatorRoute") &&
            !currentRoute.contains("ProtocolDetailRoute") &&
            !currentRoute.contains("CreateProtocolRoute") &&
            !currentRoute.contains("AddCompoundRoute") &&
            !currentRoute.contains("InjectionTrackerRoute") &&
            !currentRoute.contains("InventoryRoute") &&
            !currentRoute.contains("ProgressRoute") &&
            !currentRoute.contains("PhotoComparisonRoute") &&
            !currentRoute.contains("PKVisualizerRoute") &&
            !currentRoute.contains("HealthConnectRoute") &&
            !currentRoute.contains("BackupSettingsRoute") &&
            !currentRoute.contains("LegalRoute")

    // Dynamic TopBar Title based on destination route
    val topBarTitle = when {
        currentRoute?.contains("DashboardRoute") == true -> "PepLog Dashboard"
        currentRoute?.contains("ProtocolsRoute") == true -> "My Protocols"
        currentRoute?.contains("InsightsRoute") == true -> "Analytics Insights"
        currentRoute?.contains("MoreRoute") == true -> "More Options"
        currentRoute?.contains("EncyclopediaRoute") == true -> "Peptide Encyclopedia"
        else -> "PepLog"
    }

    val showTopBar = showBars && currentRoute?.contains("DashboardRoute") != true

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                val isEncyclopedia = currentRoute?.contains("EncyclopediaRoute") == true
                PepLogTopBar(
                    title = topBarTitle,
                    isCenterAligned = true,
                    navigationIcon = if (isEncyclopedia) {
                        {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = com.appvexis.peptidetracker.core.ui.theme.PepLogTheme.colors.textPrimary
                                )
                            }
                        }
                    } else null
                )
            }
        },
        bottomBar = {
            if (showBars) {
                AnimatedBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingCompleted) SplashRoute else OnboardingRoute,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBars) innerPadding else PaddingValues(0.dp)),
            enterTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val direction = getTransitionDirection(initialRoute, targetRoute)
                slideIntoContainer(direction, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val direction = getTransitionDirection(initialRoute, targetRoute)
                slideOutOfContainer(direction, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val direction = getTransitionDirection(initialRoute, targetRoute)
                slideIntoContainer(direction, animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                val initialRoute = initialState.destination.route
                val targetRoute = targetState.destination.route
                val direction = getTransitionDirection(initialRoute, targetRoute)
                slideOutOfContainer(direction, animationSpec = tween(350)) + fadeOut(animationSpec = tween(350))
            }
        ) {
            composable<DashboardRoute>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "peplog://dashboard" }
                )
            ) {
                DashboardScreen(
                    onNavigateToCreateProtocol = { navController.navigate(CreateProtocolRoute) },
                    onNavigateToProtocolDetail = { protocolId -> navController.navigate(ProtocolDetailRoute(protocolId)) },
                    onNavigateToCalculator = { navController.navigate(CalculatorRoute) },
                    onNavigateToInjectionSites = { navController.navigate(InjectionTrackerRoute) },
                    onNavigateToInventory = { navController.navigate(InventoryRoute) },
                    onNavigateToEncyclopedia = { navController.navigate(EncyclopediaRoute) },
                    onNavigateToDailyLog = { navController.navigate(DailyLogRoute) }
                )
            }
            
            composable<ProtocolsRoute>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "peplog://protocols" }
                )
            ) {
                ProtocolListScreen(
                    onCreateClick = { navController.navigate(CreateProtocolRoute) },
                    onProtocolClick = { id -> navController.navigate(ProtocolDetailRoute(id)) }
                )
            }
            
            composable<ProtocolDetailRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<ProtocolDetailRoute>()
                ProtocolDetailScreen(
                    protocolId = args.protocolId, 
                    onBackClick = { navController.popBackStack() }, 
                    onAddCompound = { navController.navigate(AddCompoundRoute(args.protocolId)) }
                )
            }
            
            composable<CreateProtocolRoute> {
                CreateProtocolScreen(
                    onBackClick = { navController.popBackStack() }, 
                    onProtocolCreated = { id -> 
                        navController.popBackStack()
                        navController.navigate(ProtocolDetailRoute(id)) 
                    }
                )
            }
            
            composable<AddCompoundRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<AddCompoundRoute>()
                AddCompoundScreen(
                    protocolId = args.protocolId, 
                    onBackClick = { navController.popBackStack() }, 
                    onCompoundAdded = { navController.popBackStack() }
                )
            }
            
            composable<DailyLogRoute> {
                DailyLogScreen()
            }
            
            composable<DoseHistoryRoute> {
                DoseHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable<InsightsRoute>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "peplog://insights" }
                )
            ) {
                InsightsScreen()
            }
            
            composable<MoreRoute>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "peplog://more" }
                )
            ) {
                MoreScreen(
                    onNavigateToEncyclopedia = {
                        navController.navigate(EncyclopediaRoute)
                    },
                    onNavigateToCalculator = {
                        navController.navigate(CalculatorRoute)
                    },
                    onNavigateToInjection = {
                        navController.navigate(InjectionTrackerRoute)
                    },
                    onNavigateToInventory = {
                        navController.navigate(InventoryRoute)
                    },
                    onNavigateToProgress = {
                        navController.navigate(ProgressRoute)
                    },
                    onNavigateToPKVisualizer = {
                        navController.navigate(PKVisualizerRoute)
                    },
                    onNavigateToHealthConnect = {
                        navController.navigate(HealthConnectRoute)
                    },
                    onNavigateToPaywall = {
                        navController.navigate(PaywallRoute)
                    },
                    onNavigateToBackupSettings = {
                        navController.navigate(BackupSettingsRoute)
                    },
                    onNavigateToPrivacyPolicy = {
                        navController.navigate(LegalRoute(type = "privacy"))
                    },
                    onNavigateToTermsOfService = {
                        navController.navigate(LegalRoute(type = "terms"))
                    }
                )
            }

            composable<EncyclopediaRoute> {
                EncyclopediaScreen(
                    onPeptideClick = { peptideId ->
                        navController.navigate(PeptideDetailRoute(peptideId = peptideId))
                    }
                )
            }

            composable<PeptideDetailRoute> {
                PeptideDetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<CalculatorRoute> {
                CalculatorScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<InjectionTrackerRoute> {
                InjectionTrackerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<InventoryRoute> {
                InventoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<ProgressRoute> {
                ProgressScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToPhotoComparison = { before, after ->
                        navController.navigate(PhotoComparisonRoute(beforePhotoUri = before, afterPhotoUri = after))
                    }
                )
            }

            composable<PhotoComparisonRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<PhotoComparisonRoute>()
                PhotoComparisonScreen(
                    initialBeforeUri = args.beforePhotoUri,
                    initialAfterUri = args.afterPhotoUri,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<PKVisualizerRoute> {
                PKVisualizerScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<HealthConnectRoute> {
                HealthConnectScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<PaywallRoute> {
                PaywallScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<BackupSettingsRoute> {
                BackupSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable<SplashRoute> {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<OnboardingRoute> {
                OnboardingScreen(
                    onOnboardingCompleted = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<LegalRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<LegalRoute>()
                LegalScreen(
                    type = args.type,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Helper function to determine transition slide direction based on relative tab indices.
 */
private fun getTransitionDirection(
    initialRoute: String?,
    targetRoute: String?
): AnimatedContentTransitionScope.SlideDirection {
    val initialIndex = getRouteIndex(initialRoute)
    val targetIndex = getRouteIndex(targetRoute)
    return if (targetIndex > initialIndex) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
}

/**
 * Maps a route string to its respective tab index for transition computations.
 */
private fun getRouteIndex(route: String?): Int {
    if (route == null) return 0
    return when {
        route.contains("DashboardRoute") -> 0
        route.contains("ProtocolsRoute") -> 1
        route.contains("InsightsRoute") -> 2
        route.contains("MoreRoute") -> 3
        route.contains("EncyclopediaRoute") -> 4
        route.contains("CalculatorRoute") -> 5
        route.contains("PeptideDetailRoute") -> 6
        route.contains("InjectionTrackerRoute") -> 7
        route.contains("InventoryRoute") -> 8
        route.contains("ProgressRoute") -> 9
        route.contains("PhotoComparisonRoute") -> 10
        route.contains("PKVisualizerRoute") -> 11
        route.contains("HealthConnectRoute") -> 12
        route.contains("PaywallRoute") -> 13
        route.contains("BackupSettingsRoute") -> 14
        route.contains("SplashRoute") -> -1
        route.contains("LegalRoute") -> 15
        else -> 0
    }
}
