package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BrokerAccountEntity
import com.example.ui.theme.BlueMf
import com.example.ui.theme.CashTeal
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.utils.Formatters
import kotlinx.coroutines.delay

@Composable
fun BrokerAccountsScreen(
    accounts: List<BrokerAccountEntity>,
    refreshingAccounts: Set<String>,
    isRefreshingAll: Boolean,
    onRefreshAccount: (String) -> Unit,
    onRefreshAll: () -> Unit,
    onConnectAccount: (name: String, balance: Double, accountNo: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConnectDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Sync Panel
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SyncSummaryPanel(
                    syncedCount = accounts.filter { it.status == "Synced" }.size,
                    totalCount = accounts.size,
                    isRefreshingAll = isRefreshingAll,
                    onSyncAll = onRefreshAll
                )
            }

            // Broker Accounts list
            if (accounts.isEmpty()) {
                item {
                    EmptyBrokerAccountsState { showConnectDialog = true }
                }
            } else {
                items(accounts, key = { it.id }) { account ->
                    val isRefreshing = refreshingAccounts.contains(account.id)
                    BrokerAccountCard(
                        account = account,
                        isRefreshing = isRefreshing,
                        onRefresh = { onRefreshAccount(account.id) }
                    )
                }
            }

            // Margin bottom for FAB
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        // Connect Account FAB
        FloatingActionButton(
            onClick = { showConnectDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("connect_account_fab"),
            containerColor = EmeraldGreen,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(20.dp))
                Text("Connect New Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Modern Dialog Workflow
        if (showConnectDialog) {
            ConnectAccountDialog(
                onDismiss = { showConnectDialog = false },
                onConnect = { name, balance, accNo ->
                    onConnectAccount(name, balance, accNo)
                    showConnectDialog = false
                }
            )
        }
    }
}

@Composable
fun SyncSummaryPanel(
    syncedCount: Int,
    totalCount: Int,
    isRefreshingAll: Boolean,
    onSyncAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRefreshingAll) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = "Sync",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Account Aggregator Sync",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isRefreshingAll) "Pulling latest credentials..." else "Active Connection: $syncedCount/$totalCount Synced",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Button(
                onClick = onSyncAll,
                enabled = !isRefreshingAll,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlateSurfaceVariant,
                    contentColor = EmeraldGlow
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                modifier = Modifier.testTag("sync_all_button")
            ) {
                if (isRefreshingAll) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = EmeraldGlow
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Text("Sync All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BrokerAccountCard(
    account: BrokerAccountEntity,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpinnerAnimation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    // Select color representation based on Broker
    val brokerColor = when (account.name.lowercase()) {
        "zerodha" -> PremiumGold
        "groww" -> EmeraldGreen
        "upstox" -> BlueMf
        else -> CashTeal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("broker_card_${account.name.lowercase()}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, if (isRefreshing) EmeraldGreen.copy(alpha = 0.5f) else SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Broker Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stylized Avatar/Logo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brokerColor.copy(alpha = 0.15f))
                            .border(1.dp, brokerColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.name.take(2).uppercase(),
                            color = brokerColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Column {
                        Text(
                            text = account.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Account: ${account.accountNo}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Synced Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isRefreshing) PremiumGold.copy(alpha = 0.1f) else EmeraldGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isRefreshing) PremiumGold else EmeraldGreen)
                    )
                    Text(
                        text = if (isRefreshing) "Syncing" else "Synced",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRefreshing) PremiumGold else EmeraldGlow
                    )
                }
            }

            // Balance Details
            Column {
                Text(
                    text = "CONSOLIDATED HOLDING VALUE",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = Formatters.formatIndianCurrency(account.balance),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            // Sync Stats & Refresh Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Updated ${Formatters.formatRelativeTime(account.lastUpdated)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SlateSurfaceVariant)
                        .clickable(enabled = !isRefreshing) { onRefresh() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("refresh_broker_${account.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync aggregator",
                        tint = if (isRefreshing) PremiumGold else EmeraldGlow,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(if (isRefreshing) rotationAngle else 0f)
                    )
                    Text(
                        text = "Aggregator Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRefreshing) PremiumGold else EmeraldGlow
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyBrokerAccountsState(onConnectClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "No Connections",
                tint = TextSecondary,
                modifier = Modifier.size(36.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No Brokerages Connected",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connect Zerodha, Groww, or Upstox via automated consent flows to consolidate your wealth.",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = onConnectClick,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Link First Brokerage", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectAccountDialog(
    onDismiss: () -> Unit,
    onConnect: (name: String, balance: Double, accountNo: String) -> Unit
) {
    // 1. Data Definitions for India's Verified Stockbrokers
    val techDiscount = listOf(
        "Groww", "Zerodha Kite", "Angel One", "Upstox", "Dhan", "Paytm Money", "Fyers",
        "5paisa", "m.Stock", "Shoonya", "Alice Blue", "Rupeezy", "Pocketful"
    ).map { BrokerInfo(it, "Tech-First Discount Platforms") }

    val bankBacked = listOf(
        "HDFC Sky", "HDFC Securities", "ICICI Direct", "Kotak Securities", "SBI Securities",
        "Axis Direct", "Yes Securities", "Aditya Birla Money", "Bajaj Broking"
    ).map { BrokerInfo(it, "Bank-Backed & Full-Service Ecosystems") }

    val legacyWealth = listOf(
        "Sharekhan", "Motilal Oswal", "IIFL Securities", "Geojit", "Nuvama", "Anand Rathi",
        "SMC Global", "Nirmal Bang", "Monarch Networth", "Religare", "Jainam", "Marwadi",
        "Arihant", "TradeSmart", "SAMCO"
    ).map { BrokerInfo(it, "Legacy Wealth Houses") }

    val aggregators = listOf(
        "INDmoney", "Share.Market", "BlinkX"
    ).map { BrokerInfo(it, "Ecosystem Aggregators") }

    val allBrokers = techDiscount + bankBacked + legacyWealth + aggregators

    // 2. States
    var searchQuery by remember { mutableStateOf("") }
    var selectedBroker by remember { mutableStateOf<BrokerInfo?>(null) }
    
    // AA Flow States
    var aaStep by remember { mutableStateOf(AAStep.INITIATE) }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var panNumber by remember { mutableStateOf("ABCDE1234F") }
    var otpValue by remember { mutableStateOf("123456") }
    
    // Customization states in AA Consent step
    var balanceString by remember { mutableStateOf("150000") }
    var accountNo by remember { mutableStateOf("") }

    // Auto-update default account number when a broker is selected
    LaunchedEffect(selectedBroker) {
        selectedBroker?.let {
            val shortCode = it.name.replace(" ", "").take(3).uppercase()
            accountNo = "AA-${shortCode}-${(1000 + (Math.random() * 8999).toInt())}"
            // Set realistic random default balances per broker category to keep UI rich
            val randomBal = when (it.category) {
                "Tech-First Discount Platforms" -> 125000.0 + (Math.random() * 150000.0).toInt()
                "Bank-Backed & Full-Service Ecosystems" -> 350000.0 + (Math.random() * 400000.0).toInt()
                "Legacy Wealth Houses" -> 500000.0 + (Math.random() * 1000000.0).toInt()
                else -> 75000.0 + (Math.random() * 100000.0).toInt()
            }
            balanceString = randomBal.toInt().toString()
        }
    }

    // 3. Render Dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MidnightBg
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenWidthDp = maxWidth
                val isWideScreen = screenWidthDp >= 600.dp
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MidnightBg)
                ) {
                    // COMMON HEADER
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure Lock",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (selectedBroker == null) "Connect Broker Account" else "Account Aggregator Consent",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "SEBI Registered AA Consent Flow",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = SlateSurfaceVariant),
                            modifier = Modifier.testTag("dismiss_connect_dialog")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Explorer",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                    if (selectedBroker == null) {
                        // ==========================================
                        // SCREEN A: SEARCHABLE MASTER LIST GRID VIEW
                        // ==========================================
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // LIVE FILTERING SEARCH BAR
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Search 35+ verified Indian stockbrokers (e.g. HDFC, Groww, Zerodha)...",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldGreen,
                                    unfocusedBorderColor = SlateBorder,
                                    focusedContainerColor = SlateSurface,
                                    unfocusedContainerColor = SlateSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("broker_search_input")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Filter Brokers
                            val filteredBrokers = if (searchQuery.isBlank()) {
                                allBrokers
                            } else {
                                allBrokers.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            }

                            // Active Scrollable Area
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                if (filteredBrokers.isEmpty()) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 64.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Not found",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text(
                                                text = "No Verified Brokers Match \"$searchQuery\"",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Double check the spelling or search by category below.",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                } else {
                                    // Group filtered list by Category
                                    val groupedCategories = listOf(
                                        "Tech-First Discount Platforms",
                                        "Bank-Backed & Full-Service Ecosystems",
                                        "Legacy Wealth Houses",
                                        "Ecosystem Aggregators"
                                    )

                                    groupedCategories.forEach { catName ->
                                        val listForCat = filteredBrokers.filter { it.category == catName }
                                        if (listForCat.isNotEmpty()) {
                                            item {
                                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    // Category Title Banner
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = catName.uppercase(),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (catName.contains("Tech")) EmeraldGlow else PremiumGold,
                                                            letterSpacing = 1.2.sp
                                                        )
                                                        Text(
                                                            text = "${listForCat.size} Verified",
                                                            fontSize = 10.sp,
                                                            color = TextSecondary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }

                                                    // Responsive grid representation
                                                    val columnsCount = if (screenWidthDp < 600.dp) 2 else if (screenWidthDp < 900.dp) 3 else 4
                                                    val chunkedRows = listForCat.chunked(columnsCount)

                                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        chunkedRows.forEach { rowItems ->
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                            ) {
                                                                rowItems.forEach { broker ->
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .height(72.dp)
                                                                            .clip(RoundedCornerShape(16.dp))
                                                                            .background(SlateSurface)
                                                                            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                                                                            .clickable {
                                                                                selectedBroker = broker
                                                                                aaStep = AAStep.INITIATE
                                                                            }
                                                                            .padding(12.dp)
                                                                            .testTag("broker_item_${broker.name.replace(" ", "_").lowercase()}"),
                                                                        contentAlignment = Alignment.CenterStart
                                                                    ) {
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                                        ) {
                                                                            // Stylized colored avatar logo
                                                                            val brandColor = when (broker.name.lowercase()) {
                                                                                "zerodha kite" -> Color(0xFFE0533C)
                                                                                "groww" -> Color(0xFF00D09C)
                                                                                "upstox" -> Color(0xFF412883)
                                                                                "angel one" -> Color(0xFF0052FE)
                                                                                "dhan" -> Color(0xFFFFD400)
                                                                                "hdfc sky" -> Color(0xFF004C8C)
                                                                                "icici direct" -> Color(0xFFFF7900)
                                                                                "kotak securities" -> Color(0xFFE61C24)
                                                                                "indmoney" -> Color(0xFF22C55E)
                                                                                "share.market" -> Color(0xFF4F46E5)
                                                                                else -> CashTeal
                                                                            }

                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .size(36.dp)
                                                                                    .clip(RoundedCornerShape(10.dp))
                                                                                    .background(brandColor.copy(alpha = 0.12f))
                                                                                    .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Text(
                                                                                    text = broker.iconLetters,
                                                                                    color = brandColor,
                                                                                    fontSize = 12.sp,
                                                                                    fontWeight = FontWeight.Black
                                                                                )
                                                                            }

                                                                            Column {
                                                                                Text(
                                                                                    text = broker.name,
                                                                                    fontSize = 13.sp,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    color = TextPrimary,
                                                                                    maxLines = 1
                                                                                )
                                                                                Text(
                                                                                    text = "Tap to Link",
                                                                                    fontSize = 10.sp,
                                                                                    color = TextSecondary
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                // Fill empty spaces if row has fewer columns
                                                                if (rowItems.size < columnsCount) {
                                                                    repeat(columnsCount - rowItems.size) {
                                                                        Spacer(modifier = Modifier.weight(1f))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(48.dp))
                                }
                            }
                        }
                    } else {
                        // ==========================================
                        // SCREEN B: STEP-BY-STEP ACCOUNT AGGREGATOR
                        // ==========================================
                        val broker = selectedBroker!!
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // AA Flow Progress Stepper
                            AAStepIndicator(currentStep = aaStep)

                            Spacer(modifier = Modifier.height(8.dp))

                            when (aaStep) {
                                AAStep.INITIATE -> {
                                    // Step 1: Initiate Linking Screen
                                    Card(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                        border = BorderStroke(1.dp, SlateBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(EmeraldGreen.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudSync,
                                                        contentDescription = "Sync",
                                                        tint = EmeraldGreen,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = "Initiate Aggregator Linking",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = "Linking with verified broker: ${broker.name}",
                                                        fontSize = 12.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                                            Text(
                                                text = "Provide your mobile number registered with your demat/trading accounts at NSDL or CDSL. This will retrieve consent instructions via a licensed SEBI Account Aggregator.",
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 16.sp
                                            )

                                            // Inputs
                                            OutlinedTextField(
                                                value = mobileNumber,
                                                onValueChange = { mobileNumber = it },
                                                label = { Text("Registered Mobile Number (+91)", color = TextSecondary) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldGreen,
                                                    unfocusedBorderColor = SlateBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_mobile_input")
                                            )

                                            OutlinedTextField(
                                                value = panNumber,
                                                onValueChange = { panNumber = it.uppercase() },
                                                label = { Text("PAN Number (Optional Verification)", color = TextSecondary) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldGreen,
                                                    unfocusedBorderColor = SlateBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_pan_input")
                                            )

                                            // Secure banner
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(EmeraldGreen.copy(alpha = 0.08f))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Lock, "Secure", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = "100% Encrypted & Read-Only. Neither FinDesk nor CAMS/KFintech can view your trade passwords or place order executions.",
                                                    fontSize = 11.sp,
                                                    color = EmeraldGlow,
                                                    lineHeight = 14.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = { selectedBroker = null },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant, contentColor = TextPrimary),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Back to list", fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { aaStep = AAStep.OTP },
                                                    enabled = mobileNumber.isNotBlank(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1.5f).testTag("aa_send_otp_button")
                                                ) {
                                                    Text("Request Secure OTP", fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                }

                                AAStep.OTP -> {
                                    // Step 2: OTP Verification
                                    Card(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                        border = BorderStroke(1.dp, SlateBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text(
                                                text = "Verification Required",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "An authorization code has been sent to +91 ******${mobileNumber.takeLast(4)} to fetch trading repositories.",
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                lineHeight = 16.sp
                                            )

                                            OutlinedTextField(
                                                value = otpValue,
                                                onValueChange = { otpValue = it },
                                                label = { Text("Enter 6-Digit One-Time Password", color = TextSecondary) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldGreen,
                                                    unfocusedBorderColor = SlateBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_otp_input")
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "Didn't receive? Resend OTP",
                                                    fontSize = 11.sp,
                                                    color = EmeraldGlow,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable { /* mock trigger */ }
                                                )
                                                Text(
                                                    text = "Expires in 02:45",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = { aaStep = AAStep.INITIATE },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant, contentColor = TextPrimary),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Resend / Edit", fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { aaStep = AAStep.CONSENT },
                                                    enabled = otpValue.length >= 4,
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1.5f).testTag("aa_verify_otp_button")
                                                ) {
                                                    Text("Verify & Continue", fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                }

                                AAStep.CONSENT -> {
                                    // Step 3: Detailed Read-Only Consent Options
                                    Card(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                        border = BorderStroke(1.dp, SlateBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(PremiumGold.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Lock, "Lock", tint = PremiumGold, modifier = Modifier.size(18.dp))
                                                }
                                                Column {
                                                    Text(
                                                        text = "Read-Only Portfolio Linking Consent",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = "Requested by FinDesk Aggregator Integration",
                                                        fontSize = 11.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                                            // Consent Terms Details
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                ConsentBulletItem(title = "Consent Scope", detail = "Demat active holdings, stock equities, mutual funds, capital gains details & ledger balances.")
                                                ConsentBulletItem(title = "Access Authorization", detail = "Strictly Read-Only. No transaction, execute, buy, or redeem permissions are requested.")
                                                ConsentBulletItem(title = "Access Validity", detail = "Continuous sync allowed for 1 Year (Expires in 365 days). Auto-revocable from account at any moment.")
                                                ConsentBulletItem(title = "Sync Frequency", detail = "Automatic once per day (or on manual refresh requests).")
                                            }

                                            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                                            Text(
                                                text = "PORTFOLIO PREVIEW & EDIT (MOCK DETAILS)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PremiumGold,
                                                letterSpacing = 1.sp
                                            )

                                            // Input customizable initial holdings parameters so dashboard works beautifully
                                            OutlinedTextField(
                                                value = accountNo,
                                                onValueChange = { accountNo = it.uppercase() },
                                                label = { Text("Demat / Client ID Account Code", color = TextSecondary) },
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldGreen,
                                                    unfocusedBorderColor = SlateBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_account_input")
                                            )

                                            OutlinedTextField(
                                                value = balanceString,
                                                onValueChange = { balanceString = it },
                                                label = { Text("Consolidated Assets Evaluation (₹ INR)", color = TextSecondary) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = EmeraldGreen,
                                                    unfocusedBorderColor = SlateBorder,
                                                    focusedTextColor = TextPrimary,
                                                    unfocusedTextColor = TextPrimary
                                                ),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_balance_input")
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = { selectedBroker = null },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant, contentColor = TextPrimary),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject", fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { aaStep = AAStep.LINKING },
                                                    enabled = balanceString.isNotBlank() && balanceString.toDoubleOrNull() != null && accountNo.isNotBlank(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1.5f).testTag("aa_approve_consent_button")
                                                ) {
                                                    Text("Approve & Link", fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                }

                                AAStep.LINKING -> {
                                    // Step 4: Automated Linking Animation
                                    // Timer trigger to success
                                    var linkingSubtitle by remember { mutableStateOf("Connecting to CAMS AA Hub...") }
                                    
                                    LaunchedEffect(Unit) {
                                        delay(700)
                                        linkingSubtitle = "Fetching active demat stocks from NSDL/CDSL..."
                                        delay(800)
                                        linkingSubtitle = "Retrieving mutual funds ledger records..."
                                        delay(700)
                                        aaStep = AAStep.SUCCESS
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                        border = BorderStroke(1.dp, SlateBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(24.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(56.dp),
                                                strokeWidth = 4.dp,
                                                color = EmeraldGreen
                                            )
                                            
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "Authorizing Aggregator Sync",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = linkingSubtitle,
                                                    fontSize = 12.sp,
                                                    color = TextSecondary,
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MidnightBg)
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(Icons.Default.Lock, "Lock", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                                    Text(
                                                        text = "Establishing AES-256 Encrypted Tunnel",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = EmeraldGlow
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                AAStep.SUCCESS -> {
                                    // Step 5: Successful link screen with checkmark
                                    Card(
                                        modifier = Modifier.fillMaxWidth().widthIn(max = 550.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                                        border = BorderStroke(1.dp, SlateBorder)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldGreen.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudDone,
                                                    contentDescription = "Success",
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "Demat Linked Successfully!",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Real-time holdings from ${broker.name} are now compiled in your portfolio dashboard.",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 16.sp
                                                )
                                            }

                                            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Linked Demat Code:", fontSize = 12.sp, color = TextSecondary)
                                                Text(accountNo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Consolidated Assets:", fontSize = 12.sp, color = TextSecondary)
                                                Text(
                                                    text = Formatters.formatIndianCurrency(balanceString.toDoubleOrNull() ?: 0.0),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldGreen
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    val finalBalance = balanceString.toDoubleOrNull() ?: 150000.0
                                                    onConnect(broker.name, finalBalance, accountNo)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.Black),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("aa_finish_button")
                                            ) {
                                                Text("Consolidate & Go to Dashboard", fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AAStepIndicator(currentStep: AAStep) {
    val steps = listOf(
        AAStep.INITIATE to "1. Initiate",
        AAStep.OTP to "2. Verify",
        AAStep.CONSENT to "3. Consent",
        AAStep.LINKING to "4. Syncing",
        AAStep.SUCCESS to "5. Linked"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (step, label) ->
            val isActive = step == currentStep
            val isCompleted = step.ordinal < currentStep.ordinal
            val textColor = when {
                isActive -> EmeraldGreen
                isCompleted -> EmeraldGlow
                else -> TextSecondary
            }
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> EmeraldGreen.copy(alpha = 0.2f)
                                isCompleted -> EmeraldGreen
                                else -> SlateSurfaceVariant
                            }
                        )
                        .border(
                            1.dp,
                            if (isActive) EmeraldGreen else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Done",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) EmeraldGreen else TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label.split(" ").last(),
                    fontSize = 10.sp,
                    fontWeight = fontWeight,
                    color = textColor,
                    maxLines = 1
                )
            }
            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(0.5f)
                        .background(
                            if (isCompleted) EmeraldGreen else SlateBorder
                        )
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun ConsentBulletItem(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(EmeraldGreen)
        )
        Column {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            Text(detail, fontSize = 10.sp, color = TextSecondary, lineHeight = 13.sp)
        }
    }
}

// 4. Auxiliary Data Classes & Enums
data class BrokerInfo(
    val name: String,
    val category: String,
    val iconLetters: String = name.take(2).uppercase()
)

enum class AAStep {
    INITIATE, OTP, CONSENT, LINKING, SUCCESS
}

