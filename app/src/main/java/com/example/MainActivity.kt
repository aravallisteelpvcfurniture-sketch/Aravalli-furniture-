package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.statusBars())

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = mainViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentSubScreen by viewModel.currentSubScreen.collectAsState()
    val estimates by viewModel.estimates.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val visitors by viewModel.visitors.collectAsState()
    val carpenters by viewModel.carpenters.collectAsState()
    val carpenterAssignments by viewModel.carpenterAssignments.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Calculate dynamic totals for selected period
    val totalOrderAmount = orders.sumOf { it.totalAmount }
    val totalEstimateAmount = estimates.sumOf { it.totalAmount }
    val conversionRate = if (totalEstimateAmount > 0) {
        (totalOrderAmount / totalEstimateAmount * 100).coerceIn(0.0, 100.0)
    } else 68.0

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                FloatingBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { viewModel.setScreen(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentSubScreen == SubScreen.NONE) 0.dp else innerPadding.calculateBottomPadding())
        ) {
            when (currentSubScreen) {
                SubScreen.ESTIMATE_LIST -> {
                    EstimatesScreen(
                        viewModel = viewModel,
                        estimates = estimates,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) },
                        onCreateNew = { viewModel.setSubScreen(SubScreen.ESTIMATE_NEW) }
                    )
                }
                SubScreen.ESTIMATE_NEW -> {
                    CreateEstimateFirestoreScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.setSubScreen(SubScreen.ESTIMATE_LIST) }
                    )
                }
                SubScreen.GREETINGS -> {
                    GreetingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) }
                    )
                }
                SubScreen.VISITOR_LIST, SubScreen.VISITOR_NEW -> {
                    VisitorsScreen(
                        viewModel = viewModel,
                        visitors = visitors,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) }
                    )
                }
                SubScreen.CONFIRM_ORDER_LIST, SubScreen.CONFIRM_ORDER_NEW -> {
                    ConfirmOrdersScreen(
                        viewModel = viewModel,
                        orders = orders,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) }
                    )
                }
                SubScreen.CARPENTER_LIST, SubScreen.CARPENTER_NEW -> {
                    CarpentersScreen(
                        viewModel = viewModel,
                        carpenters = carpenters,
                        assignments = carpenterAssignments,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) }
                    )
                }
                SubScreen.SALES_TRACKING -> {
                    SalesTrackingScreen(
                        viewModel = viewModel,
                        orders = orders,
                        estimates = estimates,
                        onBack = { viewModel.setSubScreen(SubScreen.NONE) }
                    )
                }
                SubScreen.NONE -> {
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                estimates = estimates,
                                orders = orders,
                                visitors = visitors,
                                carpenters = carpenters,
                                attendance = attendance,
                                selectedMonth = selectedMonth,
                                selectedYear = selectedYear
                            )
                        }
                        AppScreen.CATALOG -> {
                            CatalogScreen(
                                onRequestQuote = { productName ->
                                    viewModel.setSubScreen(SubScreen.ESTIMATE_LIST)
                                }
                            )
                        }
                        AppScreen.SCAN -> {
                            ScanScreen(viewModel = viewModel)
                        }
                        AppScreen.CARPENTERS -> {
                            CarpentersScreen(
                                viewModel = viewModel,
                                carpenters = carpenters,
                                assignments = carpenterAssignments,
                                onBack = { viewModel.setScreen(AppScreen.HOME) }
                            )
                        }
                        AppScreen.PROFILE -> {
                            ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
