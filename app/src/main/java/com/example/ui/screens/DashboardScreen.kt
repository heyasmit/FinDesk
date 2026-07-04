package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HoldingEntity
import com.example.data.PortfolioEntity
import com.example.data.BrokerAccountEntity
import com.example.data.NewsState
import com.example.data.NewsArticle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import com.example.ui.theme.SlateSurfaceVariant
import java.util.Locale
import com.example.ui.components.AllocationDonutChart
import com.example.ui.components.WealthGrowthLineChart
import com.example.ui.theme.BlueMf
import com.example.ui.theme.CashTeal
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RedLoss
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.utils.Formatters

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DashboardScreen(
    activePortfolio: PortfolioEntity?,
    holdings: List<HoldingEntity>,
    brokerAccounts: List<BrokerAccountEntity> = emptyList(),
    newsState: NewsState = NewsState.Loading,
    onToggleBookmark: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Aggregate holdings by type
    val stocksTotal = holdings.filter { it.assetType == "STOCKS" }.sumOf { it.value }
    val mfTotal = holdings.filter { it.assetType == "MUTUAL_FUNDS" }.sumOf { it.value }
    val cashTotal = holdings.filter { it.assetType == "CASH" }.sumOf { it.value }
    val holdingsTotal = stocksTotal + mfTotal + cashTotal
    val netWorth = if (brokerAccounts.isNotEmpty()) brokerAccounts.sumOf { it.balance } else holdingsTotal

    // Dynamic historical data based on active portfolio
    val (historicalData, labels, growthText, growthPercent, isPositive) = when (activePortfolio?.id) {
        "p2" -> Triple(
            listOf(5800000.0, 5950000.0, 5900000.0, 6200000.0, 6350000.0, 6500000.0),
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            "+₹7,00,000 (+12.1%)"
        ).let { (data, labels, txt) -> quintuple(data, labels, txt, "12.1%", true) }

        "p3" -> Triple(
            listOf(1200000.0, 1500000.0, 1350000.0, 1800000.0, 1600000.0, 2200000.0),
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            "+₹10,00,000 (+83.3%)"
        ).let { (data, labels, txt) -> quintuple(data, labels, txt, "83.3%", true) }

        else -> Triple(
            listOf(12500000.0, 13100000.0, 12900000.0, 13500000.0, 13800000.0, 14250000.0),
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
            "+₹17,50,000 (+14.0%)"
        ).let { (data, labels, txt) -> quintuple(data, labels, txt, "14.0%", true) }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedStockName by remember { mutableStateOf<String?>(null) }

    // Dynamic suggestions based on available holdings in the portfolio
    val uniqueStocks = remember(holdings) {
        val directStocks = holdings.filter { it.assetType == "STOCKS" }.map { it.name }
        val indirectStocks = holdings.filter { it.assetType == "MUTUAL_FUNDS" }
            .flatMap { it.overlappingStocks.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
        (directStocks + indirectStocks).distinct().sorted()
    }

    val aiSummaryInsight = remember(activePortfolio, holdings, brokerAccounts, growthPercent) {
        val portfolioName = activePortfolio?.name ?: "your portfolio"
        val growth = growthPercent ?: "0%"
        
        // Find if they have uninvested cash or general CASH holdings
        val cashHoldings = holdings.filter { it.assetType == "CASH" }
        val cashTotalVal = cashHoldings.sumOf { it.value }
        val idleCashStr = if (cashTotalVal > 0) {
            Formatters.formatIndianCurrency(cashTotalVal)
        } else {
            "₹0"
        }
        
        val stockHub = holdings.filter { it.assetType == "STOCKS" }.maxByOrNull { it.value }?.name ?: "tech stocks"
        val cleanStockName = stockHub.substringBefore(" Ltd.")
        
        val sentence1 = "Your $portfolioName is up $growth this week due to strong performance in stocks like $cleanStockName."
        val sentence2 = if (cashTotalVal > 0) {
            "You have $idleCashStr idle cash sitting in liquid reserves that could be optimized for higher returns."
        } else {
            "All your connected accounts are fully deployed and actively synchronized across your brokerages."
        }
        
        "$sentence1 $sentence2"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // X-Ray Global Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("xray_search_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "X-Ray Search",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "X-RAY GLOBAL SEARCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextSecondary,
                        letterSpacing = 1.5.sp
                    )
                }

                @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        // Clear selected stock if text changes and does not match
                        if (selectedStockName != null && !it.equals(selectedStockName, ignoreCase = true)) {
                            selectedStockName = null
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Search stock (e.g. Reliance, HDFC, Infosys)...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("xray_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = SlateBorder,
                        focusedContainerColor = MidnightBg,
                        unfocusedContainerColor = MidnightBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .clickable {
                                        searchQuery = ""
                                        selectedStockName = null
                                    }
                                    .size(18.dp)
                            )
                        }
                    }
                )

                // Quick suggestions row
                val popularSuggestions = listOf("Reliance", "HDFC Bank", "Infosys", "Zomato", "ICICI Bank")
                val activeSuggestions = popularSuggestions.filter { sugg ->
                    uniqueStocks.any { it.contains(sugg, ignoreCase = true) }
                }

                if (activeSuggestions.isNotEmpty() && selectedStockName == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Try:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        activeSuggestions.forEach { sugg ->
                            val actualStock = uniqueStocks.find { it.contains(sugg, ignoreCase = true) } ?: sugg
                            Box(
                                modifier = Modifier
                                    .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
                                    .border(1.dp, SlateBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        searchQuery = actualStock
                                        selectedStockName = actualStock
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sugg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGlow
                                )
                            }
                        }
                    }
                }

                // Autocomplete dropdown matching stock names
                if (searchQuery.isNotEmpty() && selectedStockName == null) {
                    val matches = uniqueStocks.filter { it.contains(searchQuery, ignoreCase = true) }.take(3)
                    if (matches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(12.dp))
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            matches.forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchQuery = match
                                            selectedStockName = match
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = match,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Select",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // X-Ray Deep Dive Results
                selectedStockName?.let { stock ->
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Divider(color = SlateBorder.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "INTEGRATED STOCK ANALYSIS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = PremiumGold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = stock,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct Holdings list
                    val directHoldings = holdings.filter {
                        it.assetType == "STOCKS" && it.name.equals(stock, ignoreCase = true)
                    }

                    // Indirect Holdings list
                    val indirectHoldings = holdings.filter {
                        it.assetType == "MUTUAL_FUNDS" && it.overlappingStocks.contains(stock, ignoreCase = true)
                    }

                    val totalDirectVal = directHoldings.sumOf { it.value }
                    // Estimate 10% exposure in mutual funds that contain it
                    val totalIndirectVal = indirectHoldings.sumOf { it.value * 0.10 }
                    val totalExposure = totalDirectVal + totalIndirectVal

                    // Summary Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL CONSOLIDATED EXPOSURE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = Formatters.formatIndianCurrency(totalExposure),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGlow
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(EmeraldGreen, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "X-RAY ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (directHoldings.isNotEmpty()) {
                        Text(
                            text = "Direct Brokerage Holdings",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        directHoldings.forEach { holding ->
                            val broker = brokerAccounts.find { it.id == holding.brokerId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(EmeraldGlow, CircleShape)
                                    )
                                    Text(
                                        text = broker?.name ?: "Connected Broker",
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (broker != null) {
                                        Text(
                                            text = "(${broker.accountNo})",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = Formatters.formatIndianCurrency(holding.value),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    if (indirectHoldings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Indirect Exposure via Mutual Funds",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        indirectHoldings.forEach { holding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = holding.name,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Fund Value: ${Formatters.formatIndianCurrency(holding.value)} • Overlap weight ~10%",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = Formatters.formatIndianCurrency(holding.value * 0.10),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueMf
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Summary Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_summary_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(SlateBorder, PremiumGold.copy(alpha = 0.35f))))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle Gold gradient/glow on top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(PremiumGold.copy(alpha = 0.08f), Color.Transparent),
                                radius = 240f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "AI Insight",
                                tint = PremiumGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "FINDESK AI INSIGHTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = PremiumGold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(PremiumGold.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .border(1.dp, PremiumGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "REAL-TIME",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = PremiumGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = aiSummaryInsight,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("ai_summary_text")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(PremiumGold, CircleShape)
                        )
                        Text(
                            text = "Based on aggregate analysis of connected brokerages",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        // 1. Total Assets Overview Card (Aggregated Brokerages)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("net_worth_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(SlateBorder, EmeraldGreen.copy(alpha = 0.45f))))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Glow effect top-right (blur-3xl effect)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(180.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(EmeraldGreen.copy(alpha = 0.15f), Color.Transparent),
                                radius = 300f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL ASSETS OVERVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Text(
                                text = "LIVE SYNCHRONIZED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animated Counter for Net Worth
                    AnimatedContent(
                        targetState = netWorth,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically { height -> height } + fadeIn() with
                                        slideOutVertically { height -> -height } + fadeOut()
                            } else {
                                slideInVertically { height -> -height } + fadeIn() with
                                        slideOutVertically { height -> height } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "NetWorthAnimation"
                    ) { targetNetWorth ->
                        Text(
                            text = Formatters.formatIndianCurrency(targetNetWorth),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("net_worth_value")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isPositive) EmeraldGreen.copy(alpha = 0.15f) else RedLoss.copy(alpha = 0.15f))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = "Trend direction",
                                tint = if (isPositive) EmeraldGlow else RedLoss,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = growthText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPositive) EmeraldGlow else RedLoss
                        )
                        Text(
                            text = "vs Last Quarter",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Divider separating Consolidated net worth and aggregated accounts
                    androidx.compose.material3.Divider(
                        color = SlateBorder.copy(alpha = 0.4f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Aggregated Accounts Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Sync",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (brokerAccounts.isEmpty()) "SECURE ACCOUNT INTEGRATION" else "AGGREGATED FROM ${brokerAccounts.size} ACCOUNTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    if (brokerAccounts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .border(1.dp, SlateBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "No brokerage accounts connected to this portfolio. Go to the 'Brokers' tab to link accounts and view live assets.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            brokerAccounts.forEach { account ->
                                val brandGradient = when (account.name.lowercase()) {
                                    "zerodha" -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
                                    "groww" -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                                    "upstox" -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)))
                                    else -> Brush.linearGradient(listOf(EmeraldGlow, Color(0xFF0E7490)))
                                }

                                Box(
                                    modifier = Modifier
                                        .background(SlateSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                        .border(1.dp, SlateBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(brandGradient, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = account.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = account.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(
                                                            if (account.status == "Synced") EmeraldGlow else PremiumGold,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                            Text(
                                                text = account.accountNo,
                                                fontSize = 9.sp,
                                                color = TextSecondary,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = Formatters.formatIndianCurrency(account.balance),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGlow
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Line Chart: Historical Wealth Growth
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Growth",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Wealth Trajectory",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "6M Growth",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PremiumGold,
                        modifier = Modifier
                            .background(PremiumGold.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                WealthGrowthLineChart(
                    dataPoints = historicalData,
                    labels = labels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }

        // 3. Donut Chart: Asset Allocation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            border = BorderStroke(1.dp, SlateBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Asset Allocation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Custom Donut Canvas Box
                    AllocationDonutChart(
                        stocks = stocksTotal,
                        mutualFunds = mfTotal,
                        cash = cashTotal,
                        stocksColor = EmeraldGreen,
                        mfColor = BlueMf,
                        cashColor = CashTeal,
                        modifier = Modifier
                            .size(140.dp)
                            .weight(1.2f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val stocksPercent = if (netWorth == 0.0) 0f else (stocksTotal / netWorth * 100).toFloat()
                            Text(
                                text = "STOCKS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", stocksPercent)}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldGreen
                            )
                        }
                    }

                    // Legend & Allocation Details
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AssetLegendItem(
                            title = "Stocks",
                            value = stocksTotal,
                            percent = if (netWorth == 0.0) 0.0 else (stocksTotal / netWorth * 100),
                            color = EmeraldGreen
                        )
                        AssetLegendItem(
                            title = "Mutual Funds",
                            value = mfTotal,
                            percent = if (netWorth == 0.0) 0.0 else (mfTotal / netWorth * 100),
                            color = BlueMf
                        )
                        AssetLegendItem(
                            title = "Cash / Liquid",
                            value = cashTotal,
                            percent = if (netWorth == 0.0) 0.0 else (cashTotal / netWorth * 100),
                            color = CashTeal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Curated News Feed Section
        val topAssetClass = listOf(
            "STOCKS" to stocksTotal,
            "MUTUAL_FUNDS" to mfTotal,
            "CASH" to cashTotal
        ).maxByOrNull { it.second }?.first ?: "STOCKS"

        NewsFeedSection(
            newsState = newsState,
            topAssetClass = topAssetClass,
            onToggleBookmark = onToggleBookmark
        )
    }
}

@Composable
fun AssetLegendItem(
    title: String,
    value: Double,
    percent: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(color, CircleShape)
        )
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", percent)}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
            Text(
                text = Formatters.formatIndianCurrency(value),
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

// Simple Quintuple class for scaling returning params
data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

fun <A, B, C, D, E> quintuple(first: A, second: B, third: C, fourth: D, fifth: E): Quintuple<A, B, C, D, E> {
    return Quintuple(first, second, third, fourth, fifth)
}

@Composable
fun NewsFeedSection(
    newsState: NewsState,
    topAssetClass: String,
    onToggleBookmark: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FINANCIAL INTELLIGENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = PremiumGold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Curated Headlines",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
            
            Box(
                modifier = Modifier
                    .background(EmeraldGreen.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .border(1.dp, EmeraldGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "FOCUS: $topAssetClass",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldGlow
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf(
                "ALL" to "All Feed",
                "STOCKS" to "Stocks",
                "MUTUAL_FUNDS" to "Mutual Funds",
                "CASH" to "Cash & Money"
            )
            filters.forEach { (key, label) ->
                val isSelected = selectedFilter == key
                val bg = if (isSelected) EmeraldGreen else SlateSurface
                val borderCol = if (isSelected) Color.Transparent else SlateBorder
                val textCol = if (isSelected) Color.Black else TextSecondary
                
                Box(
                    modifier = Modifier
                        .background(bg, RoundedCornerShape(12.dp))
                        .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                        .clickable { selectedFilter = key }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textCol
                    )
                }
            }
        }

        when (newsState) {
            is NewsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = EmeraldGreen,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            is NewsState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurface, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to fetch news headlines: ${newsState.message}",
                        color = RedLoss,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is NewsState.Success -> {
                val filteredArticles = if (selectedFilter == "ALL") {
                    newsState.articles
                } else {
                    newsState.articles.filter { it.assetType == selectedFilter }
                }

                if (filteredArticles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No news",
                                tint = TextSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No curated articles in this category at the moment.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredArticles.forEach { article ->
                            NewsArticleItem(
                                article = article,
                                onToggleBookmark = { onToggleBookmark(article.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsArticleItem(
    article: NewsArticle,
    onToggleBookmark: () -> Unit
) {
    val tagColor = when (article.assetType) {
        "STOCKS" -> EmeraldGreen
        "MUTUAL_FUNDS" -> BlueMf
        else -> CashTeal
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("news_item_${article.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .border(1.dp, tagColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = article.assetType,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = tagColor
                        )
                    }
                    
                    Text(
                        text = article.source,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(TextSecondary.copy(alpha = 0.5f), CircleShape)
                    )
                    
                    Text(
                        text = article.timeAgo,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) PremiumGold else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = article.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = article.summary,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${article.category}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tagColor
                )
                
                Text(
                    text = article.readTime,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
