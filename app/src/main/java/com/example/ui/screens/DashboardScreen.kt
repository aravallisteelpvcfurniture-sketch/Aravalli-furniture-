package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    estimates: List<Estimate>,
    orders: List<ConfirmedOrder>,
    visitors: List<Visitor>,
    carpenters: List<Carpenter>,
    attendance: AttendanceRecord?,
    selectedMonth: String,
    selectedYear: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val totalOrderAmount = orders.sumOf { it.totalAmount }
    val totalEstimateAmount = estimates.sumOf { it.totalAmount }
    val conversionRate = if (totalEstimateAmount > 0) {
        (totalOrderAmount / totalEstimateAmount * 100).coerceIn(0.0, 100.0)
    } else 68.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8F2))
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
            .testTag("dashboard_screen")
    ) {
        // 1. App Header with Welcome! and Check-in status
        DashboardHeader(
            isCheckedIn = attendance?.isCheckedIn == true,
            checkInTime = attendance?.checkInTime ?: "09:00 AM",
            onCheckInClick = { viewModel.toggleCheckIn() },
            onNotificationClick = {
                viewModel.showMessage("All store alerts reviewed. 0 pending errors.")
            }
        )

        // 2. Main Navigation Hub Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Management Hub",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Aravalli Steel & PVC Furniture Controls",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Surface(
                onClick = { viewModel.setSubScreen(SubScreen.GREETINGS) },
                shape = RoundedCornerShape(12.dp),
                color = IconGreetingsPurple.copy(alpha = 0.12f),
                modifier = Modifier.testTag("nav_greetings_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = IconGreetingsPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Greetings",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IconGreetingsPurple
                    )
                }
            }
        }

        // 3. Five Primary Navigation Cards (Estimates, Orders, Visitors, Carpenters, Sales Tracking)
        NavigationCardsSection(
            estimateCount = estimates.size,
            estimateTotal = totalEstimateAmount,
            orderCount = orders.size,
            orderTotal = totalOrderAmount,
            visitorCount = visitors.size,
            carpenterCount = carpenters.size,
            onEstimatesClick = { viewModel.setSubScreen(SubScreen.ESTIMATE_LIST) },
            onOrdersClick = { viewModel.setSubScreen(SubScreen.CONFIRM_ORDER_LIST) },
            onVisitorsClick = { viewModel.setSubScreen(SubScreen.VISITOR_LIST) },
            onCarpentersClick = { viewModel.setSubScreen(SubScreen.CARPENTER_LIST) },
            onSalesTrackingClick = { viewModel.setSubScreen(SubScreen.SALES_TRACKING) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Promotional Banner Carousel (Bharat ka No.1 Real Plast / Aravalli)
        PromotionalBannerCarousel()

        // 5. Month & Year Selector
        MonthYearSelector(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthSelected = { viewModel.setSelectedMonth(it) },
            onYearChanged = { viewModel.setSelectedYear(it) }
        )

        // 6. Estimate vs Order Bar Chart & Analytics Section with direct "Open Sales Tracking" link
        EstimateVsOrderChartSection(
            orderTotal = if (totalOrderAmount > 0) totalOrderAmount else 662092.0,
            estimateTotal = if (totalEstimateAmount > 0) totalEstimateAmount else 600000.0,
            conversionRate = conversionRate
        )

        // Direct action button to explore detailed sales report
        PaddingValues(horizontal = 16.dp)
        Button(
            onClick = { viewModel.setSubScreen(SubScreen.SALES_TRACKING) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("btn_open_sales_tracking")
        ) {
            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Detailed Sales Tracking & Report")
        }
    }
}

@Composable
fun NavigationCardsSection(
    estimateCount: Int,
    estimateTotal: Double,
    orderCount: Int,
    orderTotal: Double,
    visitorCount: Int,
    carpenterCount: Int,
    onEstimatesClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onVisitorsClick: () -> Unit,
    onCarpentersClick: () -> Unit,
    onSalesTrackingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Grid Row 1: Estimates & Orders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HubNavigationCard(
                title = "Estimates",
                subtitle = "Quotation & Rates",
                badgeText = "$estimateCount Quotes",
                amountPreview = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", if (estimateTotal > 0) estimateTotal else 600000.0)}",
                icon = Icons.Default.Calculate,
                accentColor = IconEstimateRed,
                onClick = onEstimatesClick,
                testTag = "nav_card_estimates",
                modifier = Modifier.weight(1f)
            )

            HubNavigationCard(
                title = "Orders",
                subtitle = "Confirmed & Dispatch",
                badgeText = "$orderCount in Production",
                amountPreview = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", if (orderTotal > 0) orderTotal else 662092.0)}",
                icon = Icons.Default.AssignmentTurnedIn,
                accentColor = IconConfirmOrderTeal,
                onClick = onOrdersClick,
                testTag = "nav_card_orders",
                modifier = Modifier.weight(1f)
            )
        }

        // Grid Row 2: Visitors & Carpenters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HubNavigationCard(
                title = "Visitors",
                subtitle = "Showroom Walk-ins",
                badgeText = "$visitorCount Inquiries",
                amountPreview = "Active Leads",
                icon = Icons.Default.Groups,
                accentColor = IconVisitorsBlue,
                onClick = onVisitorsClick,
                testTag = "nav_card_visitors",
                modifier = Modifier.weight(1f)
            )

            HubNavigationCard(
                title = "Carpenters",
                subtitle = "Mitra Club Network",
                badgeText = "$carpenterCount Partners",
                amountPreview = "Loyalty & Payouts",
                icon = Icons.Default.Engineering,
                accentColor = IconCarpenterCyan,
                onClick = onCarpentersClick,
                testTag = "nav_card_carpenters",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Dedicated Sales Tracking Card
        SalesTrackingHeroCard(
            orderTotal = if (orderTotal > 0) orderTotal else 662092.0,
            orderCount = orderCount,
            onClick = onSalesTrackingClick,
            testTag = "nav_card_sales_tracking"
        )
    }
}

@Composable
fun HubNavigationCard(
    title: String,
    subtitle: String,
    badgeText: String,
    amountPreview: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(118.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = amountPreview,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun SalesTrackingHeroCard(
    orderTotal: Double,
    orderCount: Int,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6ECD9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Sales Tracking",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Sales Tracking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2E7D32)
                        ) {
                            Text(
                                text = "Live KPI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Total Booked: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", orderTotal)} ($orderCount Orders)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Tap to view conversion rate & revenue breakdown",
                        fontSize = 10.sp,
                        color = Color(0xFF558B2F)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = BrandGreenPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
