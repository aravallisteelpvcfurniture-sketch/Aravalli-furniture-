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
import com.example.data.model.Estimate
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconEstimateRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimatesScreen(
    viewModel: MainViewModel,
    estimates: List<Estimate>,
    onBack: () -> Unit,
    onCreateNew: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedEstimateForConvert by remember { mutableStateOf<Estimate?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = estimates.filter {
        it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.customerPhone.contains(searchQuery, ignoreCase = true) ||
                it.productType.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quotation & Estimates", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Aravalli Steel & PVC Furniture", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("estimate_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandGreenPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNew,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Estimate") },
                containerColor = IconEstimateRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_create_estimate")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F8F2))
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by customer, phone, or product...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("search_estimate_input")
            )

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No estimates found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ New Estimate' to create instant quotation",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        EstimateItemCard(
                            estimate = item,
                            onShareWhatsApp = { shareEstimateOnWhatsApp(context, item) },
                            onConvertToOrder = { selectedEstimateForConvert = item }
                        )
                    }
                }
            }
        }
    }



    selectedEstimateForConvert?.let { est ->
        ConvertToOrderDialog(
            estimate = est,
            onDismiss = { selectedEstimateForConvert = null },
            onConfirm = { advance ->
                viewModel.convertEstimateToOrder(est, advance)
                selectedEstimateForConvert = null
            }
        )
    }
}

@Composable
fun EstimateItemCard(
    estimate: Estimate,
    onShareWhatsApp: () -> Unit,
    onConvertToOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("estimate_card_${estimate.id}"),
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
                    Text(
                        text = estimate.customerName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = estimate.customerPhone,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (estimate.status) {
                        "Converted to Order" -> Color(0xFFE8F5E9)
                        "Sent" -> Color(0xFFE3F2FD)
                        else -> Color(0xFFFFF3E0)
                    }
                ) {
                    Text(
                        text = estimate.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (estimate.status) {
                            "Converted to Order" -> Color(0xFF2E7D32)
                            "Sent" -> Color(0xFF1565C0)
                            else -> Color(0xFFE65100)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Product:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(text = estimate.productType, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    if (estimate.dimensions.isNotEmpty()) {
                        Text(text = "Size: ${estimate.dimensions} (${estimate.areaSqFt} sq.ft)", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Quote:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(
                        text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", estimate.totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IconEstimateRed
                    )
                    Text(text = "Date: ${estimate.createdDate}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            if (estimate.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${estimate.notes}",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                if (estimate.status != "Converted to Order") {
                    Button(
                        onClick = onConvertToOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Order", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEstimateDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        address: String,
        product: String,
        dims: String,
        sqft: Double,
        rate: Double,
        hardware: Double,
        discount: Double,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var lengthFt by remember { mutableStateOf("10") }
    var heightFt by remember { mutableStateOf("8") }
    var ratePerSqFt by remember { mutableStateOf("1350") }
    var hardwareCost by remember { mutableStateOf("12000") }
    var discount by remember { mutableStateOf("5000") }
    var notes by remember { mutableStateOf("100% Waterproof PVC with soft-close hinges") }

    val products = listOf(
        "PVC Modular Kitchen",
        "Heavy Steel Almirah (Wardrobe)",
        "PVC Waterproof Bathroom Door",
        "PVC Fluted Ceiling & Wall Panels",
        "Steel Storage Office Rack"
    )
    var selectedProduct by remember { mutableStateOf(products[0]) }

    val length = lengthFt.toDoubleOrNull() ?: 0.0
    val height = heightFt.toDoubleOrNull() ?: 0.0
    val sqft = length * height
    val rate = ratePerSqFt.toDoubleOrNull() ?: 0.0
    val hardware = hardwareCost.toDoubleOrNull() ?: 0.0
    val disc = discount.toDoubleOrNull() ?: 0.0
    val total = ((sqft * rate) + hardware - disc).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Estimate", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
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
                        label = { Text("Phone Number *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Site Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Select Product:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                    products.forEach { prod ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedProduct == prod,
                                onClick = {
                                    selectedProduct = prod
                                    if (prod.contains("Kitchen")) ratePerSqFt = "1350"
                                    else if (prod.contains("Ceiling")) ratePerSqFt = "110"
                                    else if (prod.contains("Door")) ratePerSqFt = "350"
                                    else ratePerSqFt = "2500"
                                }
                            )
                            Text(prod, fontSize = 13.sp)
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lengthFt,
                            onValueChange = { lengthFt = it },
                            label = { Text("Length (ft)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightFt,
                            onValueChange = { heightFt = it },
                            label = { Text("Height (ft)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Calculated Area: $sqft sq.ft",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandGreenPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ratePerSqFt,
                            onValueChange = { ratePerSqFt = it },
                            label = { Text("Rate / sq.ft (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hardwareCost,
                            onValueChange = { hardwareCost = it },
                            label = { Text("Hardware (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = discount,
                        onValueChange = { discount = it },
                        label = { Text("Discount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Specifications / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Final Total Quote", fontSize = 12.sp, color = Color(0xFF2E7D32))
                            Text(
                                text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", total)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandGreenPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val dims = "${lengthFt}x${heightFt} ft"
                        onSave(name, phone, address, selectedProduct, dims, sqft, rate, hardware, disc, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
            ) {
                Text("Save Estimate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ConvertToOrderDialog(
    estimate: Estimate,
    onDismiss: () -> Unit,
    onConfirm: (advancePaid: Double) -> Unit
) {
    var advanceText by remember { mutableStateOf("${(estimate.totalAmount * 0.5).toInt()}") }
    val advance = advanceText.toDoubleOrNull() ?: 0.0
    val balance = (estimate.totalAmount - advance).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Convert to Confirmed Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Customer: ${estimate.customerName}")
                Text("Product: ${estimate.productType}")
                Text("Total Quote Amount: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", estimate.totalAmount)}", fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = advanceText,
                    onValueChange = { advanceText = it },
                    label = { Text("Advance Payment Received (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Remaining Balance: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", balance)}",
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(advance) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
            ) {
                Text("Confirm Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun shareEstimateOnWhatsApp(context: Context, estimate: Estimate) {
    val quoteMessage = """
        *ARAVALLI STEEL & PVC FURNITURE*
        _Official Quotation / Estimate_
        ----------------------------------
        *Customer:* ${estimate.customerName}
        *Phone:* ${estimate.customerPhone}
        *Product:* ${estimate.productType}
        *Dimensions:* ${estimate.dimensions} (${estimate.areaSqFt} sq.ft)
        *Hardware & Accessories:* Included
        ----------------------------------
        *Total Estimated Amount:* ₹${String.format(java.util.Locale.getDefault(), "%,.0f", estimate.totalAmount)}
        ----------------------------------
        *Special Features:*
        ✓ 100% Waterproof & Termite Proof PVC
        ✓ Heavy Gauge CRCA Steel Construction
        ✓ 25-Year Long Durability Guarantee
        
        *Aravalli Steel & PVC Furniture Showroom*
        Near Highway, Rajsamand / Udaipur (Raj.)
        Call/WhatsApp: +91 98290 12345
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, quoteMessage)
    }
    context.startActivity(Intent.createChooser(intent, "Share Quotation via"))
}
