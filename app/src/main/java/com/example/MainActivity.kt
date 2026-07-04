package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WealthViewModel
import com.example.ui.components.SidebarPanel
import com.example.ui.screens.BrokerAccountsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class Screen {
    Dashboard,
    BrokerAccounts,
    Diagnostics
}

class MainActivity : ComponentActivity() {
    private val viewModel: WealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: WealthViewModel) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.Dashboard) }
    var isSidebarOpen by remember { mutableStateOf(false) }

    // Collect Reactive States
    val portfolios by viewModel.portfolios.collectAsState()
    val selectedPortfolioId by viewModel.selectedPortfolioId.collectAsState()
    val activePortfolio by viewModel.activePortfolio.collectAsState()
    val brokerAccounts by viewModel.brokerAccounts.collectAsState()
    val holdings by viewModel.holdings.collectAsState()
    val refreshingAccounts by viewModel.refreshingAccounts.collectAsState()
    val isRefreshingAll by viewModel.isRefreshingAll.collectAsState()

    val shortTermGains by viewModel.shortTermGainsInput.collectAsState()
    val shortTermLosses by viewModel.shortTermLossesInput.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val newsState by viewModel.newsState.collectAsState()

    // Sync Rotating Animation
    val infiniteTransition = rememberInfiniteTransition(label = "TopSyncAnimation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg)
    ) {
        // Main scaffold containing TopBar, BottomBar, and Screen Content
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = activePortfolio?.name ?: "Loading...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Consolidated Portfolio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { isSidebarOpen = true },
                            modifier = Modifier.testTag("sidebar_hamburger")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu Drawer",
                                tint = EmeraldGreen
                            )
                        }
                    },
                    actions = {
                        // Premium Tier Badge
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(SlateSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                                .border(1.dp, SlateBorder.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PREMIUM TIER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                        }

                        // Theme Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.Brightness4,
                                contentDescription = "Toggle Light/Dark Theme",
                                tint = if (isDarkTheme) PremiumGold else EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshAllAccounts() },
                            enabled = !isRefreshingAll,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("toolbar_sync_button")
                        ) {
                            Icon(
                                imageVector = if (isRefreshingAll) Icons.Default.CloudSync else Icons.Default.Refresh,
                                contentDescription = "Sync All",
                                tint = if (isRefreshingAll) PremiumGold else EmeraldGlow,
                                modifier = Modifier
                                    .size(22.dp)
                                    .rotate(if (isRefreshingAll) rotationAngle else 0f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MidnightBg,
                        titleContentColor = TextPrimary
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
            },
            floatingActionButton = {
                if (currentScreen == Screen.Dashboard) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.refreshAllAccounts() },
                        expanded = !isRefreshingAll,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(if (isRefreshingAll) rotationAngle else 0f)
                            )
                        },
                        text = {
                            Text(
                                text = if (isRefreshingAll) "Syncing..." else "Sync All Accounts",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        },
                        containerColor = EmeraldGreen,
                        contentColor = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 16.dp, end = 8.dp)
                            .testTag("fab_sync_all")
                    )
                }
            },
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    Divider(color = SlateBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = MidnightBg,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .height(64.dp)
                            .testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.Dashboard,
                            onClick = { currentScreen = Screen.Dashboard },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldGlow,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag("nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.BrokerAccounts,
                            onClick = { currentScreen = Screen.BrokerAccounts },
                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Brokers") },
                            label = { Text("Brokers", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldGlow,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag("nav_brokers")
                        )
                        NavigationBarItem(
                            selected = currentScreen == Screen.Diagnostics,
                            onClick = { currentScreen = Screen.Diagnostics },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = "Insights") },
                            label = { Text("Insights", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldGlow,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = EmeraldGreen
                            ),
                            modifier = Modifier.testTag("nav_insights")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Dashboard -> {
                        DashboardScreen(
                            activePortfolio = activePortfolio,
                            holdings = holdings,
                            brokerAccounts = brokerAccounts,
                            newsState = newsState,
                            onToggleBookmark = { id -> viewModel.toggleBookmark(id) }
                        )
                    }
                    Screen.BrokerAccounts -> {
                        BrokerAccountsScreen(
                            accounts = brokerAccounts,
                            refreshingAccounts = refreshingAccounts,
                            isRefreshingAll = isRefreshingAll,
                            onRefreshAccount = { id -> viewModel.refreshBrokerAccount(id) },
                            onRefreshAll = { viewModel.refreshAllAccounts() },
                            onConnectAccount = { name, balance, acc ->
                                viewModel.connectNewBrokerAccount(name, balance, acc)
                            }
                        )
                    }
                    Screen.Diagnostics -> {
                        DiagnosticsScreen(
                            holdings = holdings,
                            shortTermGains = shortTermGains,
                            shortTermLosses = shortTermLosses,
                            onGainsChange = { valStr -> viewModel.updateTaxSTCG(valStr) },
                            onLossesChange = { valStr -> viewModel.updateTaxLosses(valStr) }
                        )
                    }
                }
            }
        }

        // Overlay Sliding Drawer Sidebar
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isSidebarOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally(animationSpec = tween(300)) { -it },
            exit = slideOutHorizontally(animationSpec = tween(250)) { -it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                SidebarPanel(
                    portfolios = portfolios,
                    selectedId = selectedPortfolioId,
                    onSelect = { id -> viewModel.selectPortfolio(id) },
                    onCreatePortfolio = { name -> viewModel.createPortfolio(name) },
                    onClose = { isSidebarOpen = false }
                )
            }
        }
    }
}
