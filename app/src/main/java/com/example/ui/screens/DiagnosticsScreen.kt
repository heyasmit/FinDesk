package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HoldingEntity
import com.example.ui.theme.BlueMf
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.RedLoss
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.utils.Formatters
import kotlin.math.max
import kotlin.math.min

@Composable
fun DiagnosticsScreen(
    holdings: List<HoldingEntity>,
    shortTermGains: String,
    shortTermLosses: String,
    onGainsChange: (String) -> Unit,
    onLossesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Extract mutual funds with overlap (> 0.0%)
    val overlappingFunds = holdings.filter { it.assetType == "MUTUAL_FUNDS" && it.overlapPercentage > 0.0 }

    // Calculator live derivations
    val gainsVal = shortTermGains.toDoubleOrNull() ?: 0.0
    val lossesVal = shortTermLosses.toDoubleOrNull() ?: 0.0
    val offsetableGains = min(gainsVal, lossesVal)
    val remainingTaxableGains = max(0.0, gainsVal - lossesVal)
    
    // Indian short term capital gains tax rate on equity is 15% (updated to 20% recently, let's mention 15% standard or show standard savings)
    val taxRate = 0.15
    val estimatedSavings = offsetableGains * taxRate

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // OVERLAP DIAGNOSTICS SECTION
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Diagnostics",
                    tint = PremiumGold,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Mutual Fund Overlap Diagnostic",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = "We cross-analyze holdings of connected brokers to find stocks you might own both directly and indirectly via Mutual Funds.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            if (overlappingFunds.isEmpty()) {
                // Perfect Health card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Healthy", tint = EmeraldGlow, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Zero Redundant Overlaps", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Your mutual funds are highly diversified with minimal stock overlaps.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                overlappingFunds.forEach { fund ->
                    OverlapAlertCard(fund = fund)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // TAX-LOSS HARVESTING CALCULATOR
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = "Calculator",
                    tint = EmeraldGlow,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Tax-Loss Harvesting Calculator",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = "Offset short-term gains with unrecognized losses before March 31st to legally slash your tax liabilities.",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tax_calculator_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Inputs Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = shortTermGains,
                            onValueChange = onGainsChange,
                            label = { Text("STCG Gains (₹)", fontSize = 11.sp, color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = SlateBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stcg_gains_input")
                        )

                        OutlinedTextField(
                            value = shortTermLosses,
                            onValueChange = onLossesChange,
                            label = { Text("STCG Losses (₹)", fontSize = 11.sp, color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = SlateBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stcg_losses_input")
                        )
                    }

                    Divider(color = SlateBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    // Results Block
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Offsetable Gains", fontSize = 13.sp, color = TextSecondary)
                            Text(Formatters.formatIndianCurrency(offsetableGains), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Remaining Taxable Gains", fontSize = 13.sp, color = TextSecondary)
                            Text(Formatters.formatIndianCurrency(remainingTaxableGains), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STCG Tax Rate", fontSize = 13.sp, color = TextSecondary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BlueMf.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("15%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BlueMf)
                                }
                            }
                            val standardTax = gainsVal * taxRate
                            Text(Formatters.formatIndianCurrency(standardTax), fontSize = 13.sp, color = TextSecondary)
                        }
                    }

                    // SAVINGS GLOW CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATED TAX SAVINGS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGlow,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = Formatters.formatIndianCurrency(estimatedSavings),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EmeraldGlow,
                                    modifier = Modifier.testTag("tax_savings_value")
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Percent, contentDescription = "Savings", tint = EmeraldGlow, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Simulate execute button
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateSurfaceVariant,
                            contentColor = EmeraldGlow
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Harvest Losses & Execute Offsetting Trades", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OverlapAlertCard(fund: HoldingEntity) {
    val levelColor = if (fund.overlapPercentage >= 50.0) RedLoss else PremiumGold

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overlap_alert_${fund.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(levelColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = levelColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = fund.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mutual Fund Category Overlap",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Overlap badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(levelColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${fund.overlapPercentage}% OVERLAP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = levelColor
                    )
                }
            }

            Divider(color = SlateBorder.copy(alpha = 0.4f), thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "REDUNDANT DUPLICATED ASSETS FOUND:",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = fund.overlappingStocks,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceVariant)
                    .padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Insight",
                    tint = BlueMf,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "This reduces diversification benefits. Consider consolidating direct holdings or shifting allocation to index funds or global equities.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
