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
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconConfirmOrderTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmOrdersScreen(
    viewModel: MainViewModel,
    orders: List<ConfirmedOrder>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var selectedOrderForPayment by remember { mutableStateOf<ConfirmedOrder?>(null) }

    val stages = listOf(
        "Order Confirmed",
        "Material Cutting",
        "Assembling",
        "Quality Check",
        "Ready for Dispatch",
        "Delivered"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Confirmed Orders", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Production Tracking & Delivery", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("orders_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IconConfirmOrderTeal,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewOrderDialog = true },
                icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                text = { Text("New Order") },
                containerColor = IconConfirmOrderTeal,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_order")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE0F2F1).copy(alpha = 0.5f))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderItemCard(
                        order = order,
                        stages = stages,
                        onStageChanged = { stage -> viewModel.updateOrderStage(order, stage) },
                        onAddPaymentClick = { selectedOrderForPayment = order },
                        onShareChallan = { shareOrderChallan(context, order) }
                    )
                }
            }
        }
    }

    if (showNewOrderDialog) {
        CreateOrderDialog(
            onDismiss = { showNewOrderDialog = false },
            onSave = { name, phone, address, summary, total, advance, deliveryDate, paymentMode ->
                viewModel.addConfirmedOrder(name, phone, address, summary, total, advance, deliveryDate, paymentMode)
                showNewOrderDialog = false
            }
        )
    }

    selectedOrderForPayment?.let { order ->
        RecordPaymentDialog(
            order = order,
            onDismiss = { selectedOrderForPayment = null },
            onConfirm = { paymentAmt ->
                viewModel.addOrderPayment(order, paymentAmt)
                selectedOrderForPayment = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItemCard(
    order: ConfirmedOrder,
    stages: List<String>,
    onStageChanged: (String) -> Unit,
    onAddPaymentClick: () -> Unit,
    onShareChallan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stageMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = order.orderNumber, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = IconConfirmOrderTeal)
                    Text(text = order.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = order.customerPhone, fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Box {
                    AssistChip(
                        onClick = { stageMenuExpanded = true },
                        label = { Text(order.stage, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (order.stage) {
                                "Delivered" -> Color(0xFFE8F5E9)
                                "Ready for Dispatch" -> Color(0xFFFFF9C4)
                                else -> Color(0xFFE0F2F1)
                            }
                        )
                    )
                    DropdownMenu(
                        expanded = stageMenuExpanded,
                        onDismissRequest = { stageMenuExpanded = false }
                    ) {
                        stages.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage) },
                                onClick = {
                                    onStageChanged(stage)
                                    stageMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            Text(text = order.productSummary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (order.deliveryAddress.isNotEmpty()) {
                Text(text = "Site: ${order.deliveryAddress}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Text(text = "Promised Delivery: ${order.deliveryDate}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))

            Spacer(modifier = Modifier.height(10.dp))

            // Payment summary bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Amount", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Advance Paid", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.advancePaid)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Balance Due", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.balanceDue)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD32F2F))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareChallan,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delivery Slip", fontSize = 12.sp)
                }

                if (order.balanceDue > 0) {
                    Button(
                        onClick = onAddPaymentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = IconConfirmOrderTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Payment", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateOrderDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        address: String,
        summary: String,
        total: Double,
        advance: Double,
        deliveryDate: String,
        paymentMode: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("PVC Modular Kitchen + Cabinets") }
    var totalText by remember { mutableStateOf("125000") }
    var advanceText by remember { mutableStateOf("60000") }
    var deliveryDate by remember { mutableStateOf("2026-10-25") }
    var paymentMode by remember { mutableStateOf("UPI / GPay") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Confirmed Order") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Product Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalText,
                            onValueChange = { totalText = it },
                            label = { Text("Total (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = advanceText,
                            onValueChange = { advanceText = it },
                            label = { Text("Advance (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = deliveryDate,
                        onValueChange = { deliveryDate = it },
                        label = { Text("Promised Delivery Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("Payment Mode") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalText.toDoubleOrNull() ?: 0.0
                    val adv = advanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, address, summary, tot, adv, deliveryDate, paymentMode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IconConfirmOrderTeal)
            ) {
                Text("Confirm Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RecordPaymentDialog(
    order: ConfirmedOrder,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("${order.balanceDue.toInt()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Balance Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Order: ${order.orderNumber} - ${order.customerName}")
                Text("Current Balance Due: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.balanceDue)}", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IconConfirmOrderTeal)
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun shareOrderChallan(context: Context, order: ConfirmedOrder) {
    val text = """
        *ARAVALLI STEEL & PVC FURNITURE*
        _Delivery Challan & Order Receipt_
        ------------------------------------
        *Order ID:* ${order.orderNumber}
        *Date:* ${order.orderDate}
        *Customer:* ${order.customerName}
        *Phone:* ${order.customerPhone}
        *Site Address:* ${order.deliveryAddress}
        
        *Item Summary:*
        ${order.productSummary}
        
        *Financial Details:*
        Total Amount: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.totalAmount)}
        Advance Received: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.advancePaid)}
        *Balance Due:* ₹${String.format(java.util.Locale.getDefault(), "%,.0f", order.balanceDue)}
        
        *Current Stage:* ${order.stage}
        *Estimated Delivery:* ${order.deliveryDate}
        ------------------------------------
        Aravalli Showroom • Rajsamand / Udaipur
        Contact: +91 98290 12345
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Challan via"))
}
