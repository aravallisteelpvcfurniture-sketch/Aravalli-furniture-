package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.ChartDayData
import com.example.ui.theme.*

@Composable
fun DashboardHeader(
    isCheckedIn: Boolean,
    checkInTime: String,
    onCheckInClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandGreenHeader,
                        Color(0xFF2E7D32)
                    )
                )
            )
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Welcome!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("welcome_text")
                )
                Text(
                    text = "Aravalli Steel & PVC Furniture",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Check In Pill Button matching screenshot
                Surface(
                    onClick = onCheckInClick,
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCheckedIn) Color(0xFF1B5E20) else Color(0xFF1B5E20).copy(alpha = 0.85f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("check_in_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isCheckedIn) Color(0xFF69F0AE) else Color(0xFFFFD54F))
                        )
                        Text(
                            text = if (isCheckedIn) "Checked In" else "Check In",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Notification Bell Icon with Badge
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("notification_button")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onEstimateClick: () -> Unit,
    onGreetingsClick: () -> Unit,
    onVisitorsClick: () -> Unit,
    onConfirmOrderClick: () -> Unit,
    onMyCarpenterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Estimate & Greetings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCardItem(
                title = "Estimate",
                icon = Icons.Default.Calculate,
                circleColor = IconEstimateRed,
                onClick = onEstimateClick,
                testTag = "action_estimate",
                modifier = Modifier.weight(1f)
            )
            ActionCardItem(
                title = "Greetings",
                icon = Icons.Default.Handshake,
                circleColor = IconGreetingsPurple,
                onClick = onGreetingsClick,
                testTag = "action_greetings",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Visitors & Confirm Order
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCardItem(
                title = "Visitors",
                icon = Icons.Default.Groups,
                circleColor = IconVisitorsBlue,
                onClick = onVisitorsClick,
                testTag = "action_visitors",
                modifier = Modifier.weight(1f)
            )
            ActionCardItem(
                title = "Confirm Order",
                icon = Icons.Default.AssignmentTurnedIn,
                circleColor = IconConfirmOrderTeal,
                onClick = onConfirmOrderClick,
                testTag = "action_confirm_order",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Wide My Carpenter Card
        WideCarpenterCardItem(
            title = "My Carpenter",
            icon = Icons.Default.Engineering,
            circleColor = IconCarpenterCyan,
            onClick = onMyCarpenterClick,
            testTag = "action_my_carpenter"
        )
    }
}

@Composable
fun ActionCardItem(
    title: String,
    icon: ImageVector,
    circleColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(84.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD3ECD7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Circular vibrant icon container matching screenshot
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WideCarpenterCardItem(
    title: String,
    icon: ImageVector,
    circleColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD3ECD7)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun PromotionalBannerCarousel(
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val bannerCount = 4

    // Auto-advance banner every few seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4500)
            currentPage = (currentPage + 1) % bannerCount
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Banner card matching the real plast / india no.1 banner in screenshot
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("promo_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (currentPage) {
                    0 -> Color(0xFFB0BEC5)
                    1 -> Color(0xFF546E7A)
                    2 -> Color(0xFF37474F)
                    else -> Color(0xFF455A64)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = when (currentPage) {
                                0 -> listOf(Color(0xFF8D99AE), Color(0xFF2B2D42))
                                1 -> listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
                                2 -> listOf(Color(0xFF263238), Color(0xFF37474F))
                                else -> listOf(Color(0xFF3E2723), Color(0xFF4E342E))
                            }
                        )
                    )
                    .padding(14.dp)
            ) {
                when (currentPage) {
                    0 -> {
                        // Replica of India No. 1 banner from screenshot
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Yellow badge
                                Surface(
                                    color = Color(0xFFFFD600),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "भारत का No.1",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFD50000),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = "ARAVALLI STEEL & PVC",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "100% Waterproof • Termite Proof • 25 Yr Warranty",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD54F)
                                )
                            }

                            // Showroom graphic container
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(width = 90.dp, height = 110.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Kitchen,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Text(
                                        text = "PVC Kitchen",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFF69F0AE),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "FESTIVAL OFFER",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Modular PVC Kitchens",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Flat 12% Discount on Complete House Package",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC8E6C9)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Countertops,
                                contentDescription = null,
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFFFF9100),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "HEAVY GAUGE STEEL",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Designer Almirah & Safes",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "18-Gauge CRCA Steel with Digital Electronic Locker",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFE0B2)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.DoorSliding,
                                contentDescription = null,
                                tint = Color(0xFFFF9100),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFF00E5FF),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "CARPENTER CLUB",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF006064),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Earn Commission on Every Site",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Instant UPI payout + Gold Coin milestone rewards",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0F7FA)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }
            }
        }

        // Indicator dots matching screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until bannerCount) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == currentPage) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) Color(0xFF2E7D32) else Color(0xFFB0BEC5)
                        )
                        .clickable { currentPage = i }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearSelector(
    selectedMonth: String,
    selectedYear: String,
    onMonthSelected: (String) -> Unit,
    onYearChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Select Month dropdown matching screenshot
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Select Month",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMonth,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrandGreenPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("month_selector")
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                onMonthSelected(month)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Enter Year selector matching screenshot
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Enter Year",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = selectedYear,
                onValueChange = { if (it.length <= 4) onYearChanged(it) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandGreenPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("year_input")
            )
        }
    }
}

@Composable
fun EstimateVsOrderChartSection(
    orderTotal: Double,
    estimateTotal: Double,
    conversionRate: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("estimate_vs_order_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Estimate vs Order",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Legend matching screenshot: Blue (Order Amount), Purple (Estimate Amount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ChartOrderBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Order Amount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ChartEstimatePurple)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estimate Amount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric display matching screenshot with ₹662092, 600000, 400000
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Order Value",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", orderTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChartOrderBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Estimates",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", estimateTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ChartEstimatePurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Realistic Dual Bar Chart matching the screenshot
            DualBarChartView(
                orderTotal = orderTotal,
                estimateTotal = estimateTotal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Conversion status bar
            Surface(
                color = Color(0xFFF1F8F2),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Conversion Rate:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }
                    Text(
                        text = "${String.format(java.util.Locale.getDefault(), "%.1f", conversionRate)}% Closed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun DualBarChartView(
    orderTotal: Double,
    estimateTotal: Double,
    modifier: Modifier = Modifier
) {
    // Generate realistic multi-day bars resembling the dense bar sequence in screenshot
    val barRatios = listOf(
        0.45f to 0.60f,
        0.70f to 0.85f,
        0.30f to 0.40f,
        0.90f to 0.95f,
        0.65f to 0.70f,
        0.80f to 0.90f,
        0.55f to 0.65f,
        0.40f to 0.50f,
        0.85f to 0.92f,
        0.75f to 0.80f,
        0.60f to 0.70f,
        0.95f to 1.00f,
        0.50f to 0.60f,
        0.35f to 0.45f,
        0.80f to 0.85f,
        0.65f to 0.75f,
        0.45f to 0.55f,
        0.90f to 0.92f,
        0.70f to 0.80f,
        0.60f to 0.65f
    )

    Box(
        modifier = modifier
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top axis mark matching screenshot '662092' / '600000'
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹6,62,092",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Peak: Oct 2026",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bars row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                barRatios.forEach { (orderRatio, estimateRatio) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Order bar (Blue)
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight(orderRatio)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(ChartOrderBlue)
                        )
                        // Estimate bar (Purple)
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight(estimateRatio)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(ChartEstimatePurple)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom axis mark matching screenshot '400000'
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹4,00,000",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "1st  5th  10th  15th  20th  25th  30th",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun FloatingBottomNavigation(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF2E7D32),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home item (matches active pill from screenshot)
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == AppScreen.HOME,
                onClick = { onScreenSelected(AppScreen.HOME) },
                testTag = "nav_home"
            )

            // Catalog / Store
            BottomNavItem(
                icon = Icons.Outlined.ShoppingBag,
                label = "Catalog",
                isSelected = currentScreen == AppScreen.CATALOG,
                onClick = { onScreenSelected(AppScreen.CATALOG) },
                testTag = "nav_catalog"
            )

            // Scan QR
            BottomNavItem(
                icon = Icons.Default.QrCodeScanner,
                label = "Scan",
                isSelected = currentScreen == AppScreen.SCAN,
                onClick = { onScreenSelected(AppScreen.SCAN) },
                testTag = "nav_scan"
            )

            // Carpenters / Club
            BottomNavItem(
                icon = Icons.Outlined.Person,
                label = "Club",
                isSelected = currentScreen == AppScreen.CARPENTERS,
                onClick = { onScreenSelected(AppScreen.CARPENTERS) },
                testTag = "nav_carpenters"
            )

            // Business Profile & Support
            BottomNavItem(
                icon = Icons.Outlined.Chat,
                label = "Support",
                isSelected = currentScreen == AppScreen.PROFILE,
                onClick = { onScreenSelected(AppScreen.PROFILE) },
                testTag = "nav_support"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    if (isSelected) {
        // Active item with soft light pill matching screenshot
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF66BB6A).copy(alpha = 0.45f),
            modifier = Modifier.testTag(testTag)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(44.dp)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
