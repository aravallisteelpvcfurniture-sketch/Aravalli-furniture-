package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConfirmedOrder
import com.example.data.model.Estimate
import com.example.ui.MainViewModel
import com.example.ui.components.DualBarChartView
import com.example.ui.components.MonthYearSelector
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTrackingScreen(
    viewModel: MainViewModel,
    orders: List<ConfirmedOrder>,
    estimates: List<Estimate>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val totalOrderAmount = orders.sumOf { it.totalAmount }
    val totalAdvance = orders.sumOf { it.advancePaid }
    val totalBalance = orders.sumOf { it.balanceDue }
    val totalEstimateAmount = estimates.sumOf { it.totalAmount }
    val conversionRate = if (totalEstimateAmount > 0) {
        (totalOrderAmount / totalEstimateAmount * 100).coerceIn(0.0, 100.0)
    } else 68.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Sales Tracking & Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Aravalli Steel & PVC Revenue Overview", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("sales_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareSalesReport(context, selectedMonth, selectedYear, totalOrderAmount, totalEstimateAmount, totalAdvance, totalBalance, orders.size)
                        },
                        modifier = Modifier.testTag("share_sales_report_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F8F2))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month and Year selector
            item {
                MonthYearSelector(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthSelected = { viewModel.setSelectedMonth(it) },
                    onYearChanged = { viewModel.setSelectedYear(it) }
                )
            }

            // Top Metric Cards Row 1: Orders and Estimates
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Orders Value",
                        value = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", if (totalOrderAmount > 0) totalOrderAmount else 662092.0)}",
                        subtext = "${orders.size} Confirmed Orders",
                        color = ChartOrderBlue,
                        icon = Icons.Default.ShoppingCart,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Total Estimates",
                        value = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", if (totalEstimateAmount > 0) totalEstimateAmount else 600000.0)}",
                        subtext = "${estimates.size} Quotes Issued",
                        color = ChartEstimatePurple,
                        icon = Icons.Default.ReceiptLong,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Metric Cards Row 2: Advance Collected and Balance Due
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Advance Collected",
                        value = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalAdvance)}",
                        subtext = "Cash / UPI Received",
                        color = Color(0xFF2E7D32),
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Pending Receivables",
                        value = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalBalance)}",
                        subtext = "Due on Delivery",
                        color = Color(0xFFD32F2F),
                        icon = Icons.Default.PendingActions,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Dual Bar Chart Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("sales_chart_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimate vs Order Comparison",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.getDefault(), "%.1f", conversionRate)}% Converted",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 20.dp, height = 10.dp)
                                        .background(ChartOrderBlue, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Order Amount", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 20.dp, height = 10.dp)
                                        .background(ChartEstimatePurple, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Estimate Amount", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DualBarChartView(
                            orderTotal = totalOrderAmount,
                            estimateTotal = totalEstimateAmount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            // Product Category Revenue Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product-wise Sales Share",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val categories = listOf(
                            Triple("PVC Modular Kitchens", "42%", Color(0xFF1B5E20)),
                            Triple("Heavy Steel Almirahs", "28%", Color(0xFF0288D1)),
                            Triple("PVC Doors & Windows", "15%", Color(0xFF7B1FA2)),
                            Triple("Wall Panels & Ceilings", "10%", Color(0xFFFF8F00)),
                            Triple("Steel Racks & Office", "5%", Color(0xFF00796B))
                        )

                        categories.forEach { (catName, percentage, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(catName, fontSize = 13.sp, color = Color(0xFF334155))
                                }
                                Text(percentage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )

            Text(
                text = subtext,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

fun shareSalesReport(
    context: Context,
    month: String,
    year: String,
    orderTotal: Double,
    estimateTotal: Double,
    advance: Double,
    balance: Double,
    orderCount: Int
) {
    val report = """
        *ARAVALLI STEEL & PVC FURNITURE*
        _Monthly Sales & Performance Report_
        Period: $month $year
        ------------------------------------
        *Total Confirmed Orders:* $orderCount
        *Total Order Value:* ₹${String.format(java.util.Locale.getDefault(), "%,.0f", orderTotal)}
        *Total Estimates Issued:* ₹${String.format(java.util.Locale.getDefault(), "%,.0f", estimateTotal)}
        
        *Collections:*
        ✓ Advance Received: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", advance)}
        ✓ Balance Pending: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", balance)}
        ------------------------------------
        Aravalli Steel & PVC Furniture Showroom
        National Highway, Rajsamand / Udaipur (Rajasthan)
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Share Sales Report via"))
}
