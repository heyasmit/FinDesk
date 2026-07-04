package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WealthGrowthLineChart(
    dataPoints: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(dataPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200)
        )
    }

    val maxVal = dataPoints.maxOrNull() ?: 1.0
    val minVal = dataPoints.minOrNull() ?: 0.0
    val diff = (maxVal - minVal).coerceAtLeast(1.0)
    
    val paddingY = diff * 0.1 // Give some breathing room at the top and bottom

    // Resolve color states in Composable context
    val labelColor = TextSecondary
    val gridLineColor = SlateBorder
    val nodeBgColor = MidnightBg

    Canvas(modifier = modifier.padding(16.dp)) {
        val width = size.width
        val height = size.height

        // Bottom space for X-axis labels, Right space for Y-axis labels
        val bottomMargin = 40.dp.toPx()
        val rightMargin = 12.dp.toPx()
        val leftMargin = 60.dp.toPx()
        val topMargin = 20.dp.toPx()

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin

        // Draw horizontal gridlines (3 gridlines)
        val gridCount = 4
        val gridStyle = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val valueStep = diff / (gridCount - 1)
        
        for (i in 0 until gridCount) {
            val ratio = i.toFloat() / (gridCount - 1)
            val y = topMargin + chartHeight * (1 - ratio)
            
            // Grid line
            drawLine(
                color = gridLineColor.copy(alpha = 0.5f),
                start = Offset(leftMargin, y),
                end = Offset(leftMargin + chartWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = gridStyle
            )

            // Y Axis text
            val gridValue = minVal + (valueStep * i)
            val formattedValue = formatYAxisValue(gridValue)
            val textLayoutResult = textMeasurer.measure(
                text = formattedValue,
                style = TextStyle(color = labelColor, fontSize = 10.sp)
            )
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = leftMargin - textLayoutResult.size.width - 8.dp.toPx(),
                    y = y - textLayoutResult.size.height / 2
                )
            )
        }

        // Generate points coordinates
        val points = dataPoints.mapIndexed { index, value ->
            val x = leftMargin + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
            val yFactor = if (diff == 0.0) 0.5f else ((value - minVal) / diff).toFloat()
            val y = topMargin + chartHeight * (1f - yFactor)
            Offset(x, y)
        }

        if (points.size >= 2) {
            // Draw gradient shadow path under the line
            val fillPath = Path().apply {
                moveTo(points.first().x, topMargin + chartHeight)
                for (i in 0 until points.size) {
                    val currentPoint = points[i]
                    lineTo(currentPoint.x, topMargin + chartHeight - (topMargin + chartHeight - currentPoint.y) * animatedProgress.value)
                }
                lineTo(points.last().x, topMargin + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EmeraldGreen.copy(alpha = 0.35f),
                        EmeraldGreen.copy(alpha = 0.02f)
                    ),
                    startY = topMargin,
                    endY = topMargin + chartHeight
                )
            )

            // Draw line path
            val strokePath = Path().apply {
                val first = points.first()
                moveTo(first.x, topMargin + chartHeight - (topMargin + chartHeight - first.y) * animatedProgress.value)
                
                // Draw Bezier curves for smooth financial look
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val currentY0 = topMargin + chartHeight - (topMargin + chartHeight - p0.y) * animatedProgress.value
                    val currentY1 = topMargin + chartHeight - (topMargin + chartHeight - p1.y) * animatedProgress.value
                    
                    val controlX = (p0.x + p1.x) / 2
                    cubicTo(
                        x1 = controlX, y1 = currentY0,
                        x2 = controlX, y2 = currentY1,
                        x3 = p1.x, y3 = currentY1
                    )
                }
            }

            drawPath(
                path = strokePath,
                color = EmeraldGlow,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw data nodes/points (circles on nodes)
            points.forEach { point ->
                val currentY = topMargin + chartHeight - (topMargin + chartHeight - point.y) * animatedProgress.value
                drawCircle(
                    color = nodeBgColor,
                    radius = 4.dp.toPx(),
                    center = Offset(point.x, currentY)
                )
                drawCircle(
                    color = EmeraldGlow,
                    radius = 2.dp.toPx(),
                    center = Offset(point.x, currentY)
                )
            }
        }

        // Draw X Axis labels
        labels.forEachIndexed { index, label ->
            if (index < points.size) {
                val point = points[index]
                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(color = labelColor, fontSize = 10.sp)
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = point.x - textLayoutResult.size.width / 2,
                        y = topMargin + chartHeight + 8.dp.toPx()
                    )
                )
            }
        }
    }
}

@Composable
fun AllocationDonutChart(
    stocks: Double,
    mutualFunds: Double,
    cash: Double,
    stocksColor: Color,
    mfColor: Color,
    cashColor: Color,
    modifier: Modifier = Modifier,
    centerContent: @Composable () -> Unit = {}
) {
    val total = stocks + mutualFunds + cash
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(stocks, mutualFunds, cash) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000)
        )
    }

    val stocksAngle = if (total == 0.0) 0f else (stocks / total * 360f).toFloat()
    val mfAngle = if (total == 0.0) 0f else (mutualFunds / total * 360f).toFloat()
    val cashAngle = if (total == 0.0) 0f else (cash / total * 360f).toFloat()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val chartSize = size.minDimension * 0.85f
            val left = (size.width - chartSize) / 2
            val top = (size.height - chartSize) / 2
            val strokeWidthVal = 24.dp.toPx()

            var startAngle = -90f

            // 1. Draw Stocks arc
            if (stocksAngle > 0) {
                drawArc(
                    color = stocksColor,
                    startAngle = startAngle,
                    sweepAngle = stocksAngle * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(chartSize, chartSize)
                )
                startAngle += stocksAngle * animatedProgress.value
            }

            // 2. Draw Mutual Funds arc
            if (mfAngle > 0) {
                drawArc(
                    color = mfColor,
                    startAngle = startAngle,
                    sweepAngle = mfAngle * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(chartSize, chartSize)
                )
                startAngle += mfAngle * animatedProgress.value
            }

            // 3. Draw Cash arc
            if (cashAngle > 0) {
                drawArc(
                    color = cashColor,
                    startAngle = startAngle,
                    sweepAngle = cashAngle * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokeWidthVal, cap = StrokeCap.Round),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(chartSize, chartSize)
                )
            }
        }
        
        // Inner overlay content (e.g. Net Worth Summary text)
        Box(
            modifier = Modifier.padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            centerContent()
        }
    }
}

private fun formatYAxisValue(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return when {
        value >= 10000000 -> {
            val cr = value / 10000000
            "₹${String.format(Locale.US, "%.1f", cr)}Cr"
        }
        value >= 100000 -> {
            val lakh = value / 100000
            "₹${String.format(Locale.US, "%.1f", lakh)}L"
        }
        value >= 1000 -> {
            val k = value / 1000
            "₹${String.format(Locale.US, "%.0f", k)}K"
        }
        else -> format.format(value)
    }
}

private val MidnightBgColor = Color(0xFF0B0E17)
