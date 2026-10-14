package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.repository.FirestoreRepository
import com.example.data.repository.FurnitureEstimateItem
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconEstimateRed
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEstimateFirestoreScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firestoreRepository = remember { FirestoreRepository(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Customer state
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("100% Waterproof PVC, anti-termite, 25-yr durability guarantee") }

    // Dynamic Items list
    var items by remember {
        mutableStateOf(
            listOf(
                FurnitureEstimateItem(
                    name = "PVC Modular Kitchen (Acrylic High Gloss)",
                    category = "Modular Kitchen",
                    dimensions = "12 x 8 ft",
                    quantity = 1,
                    unitPrice = 135000.0,
                    subtotal = 135000.0
                )
            )
        )
    }

    var hardwareCostText by remember { mutableStateOf("12000") }
    var discountText by remember { mutableStateOf("7000") }

    val hardwareCost = hardwareCostText.toDoubleOrNull() ?: 0.0
    val discountAmount = discountText.toDoubleOrNull() ?: 0.0
    val itemsSubtotal = items.sumOf { it.subtotal }
    val grandTotalPrice = (itemsSubtotal + hardwareCost - discountAmount).coerceAtLeast(0.0)

    val categories = listOf(
        "Modular Kitchen",
        "Steel Almirahs",
        "PVC Doors",
        "Wall Panels",
        "Office Racks",
        "Custom Furniture"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("New Estimate (Firestore)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Cloud Database • Aravalli Steel & PVC", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("estimate_new_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IconEstimateRed,
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
            // Cloud status bar
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentUser != null) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = if (currentUser != null) BrandGreenPrimary else Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (currentUser != null) "Firestore Cloud Connected" else "Sign in to save to Cloud",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (currentUser != null) BrandGreenPrimary else Color(0xFFE65100)
                                )
                                Text(
                                    text = currentUser?.displayName ?: currentUser?.email ?: "Saves locally & syncs on login",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (currentUser == null) {
                            Button(
                                onClick = {
                                    isSigningIn = true
                                    val credManager = CredentialManager.create(context)
                                    val clientId = try {
                                        context.getString(R.string.default_web_client_id)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Web client ID missing in google-services.json")
                                        isSigningIn = false
                                        return@Button
                                    }

                                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                                    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                                    scope.launch {
                                        try {
                                            val result = credManager.getCredential(context as Activity, request)
                                            val credential = result.credential
                                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                                Firebase.auth.signInWithCredential(authCredential).await()
                                                viewModel.showMessage("Signed in with Google!")
                                            }
                                        } catch (e: GetCredentialCancellationException) {
                                            Log.w("Auth", "Sign-in cancelled", e)
                                        } catch (e: Exception) {
                                            viewModel.showMessage("Sign-in error: ${e.localizedMessage}")
                                        } finally {
                                            isSigningIn = false
                                        }
                                    }
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary)
                            ) {
                                Text("Sign In", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Customer Information Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Customer Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name *") },
                            placeholder = { Text("e.g. Mukesh Choudhary") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_name")
                        )

                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = { customerPhone = it },
                            label = { Text("Phone Number *") },
                            placeholder = { Text("+91 98290 12345") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_phone")
                        )

                        OutlinedTextField(
                            value = customerAddress,
                            onValueChange = { customerAddress = it },
                            label = { Text("Site / Delivery Address") },
                            placeholder = { Text("Plot 24, Near Shreenath Mandir, Rajsamand") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_customer_address")
                        )
                    }
                }
            }

            // Furniture Items List Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Furniture Items (${items.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )

                    TextButton(
                        onClick = {
                            items = items + FurnitureEstimateItem(
                                name = "Designer Steel Almirah (18-Gauge)",
                                category = "Steel Almirahs",
                                dimensions = "6.5 x 4 ft",
                                quantity = 1,
                                unitPrice = 45000.0,
                                subtotal = 45000.0
                            )
                        },
                        modifier = Modifier.testTag("btn_add_item")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = IconEstimateRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Item", fontWeight = FontWeight.Bold, color = IconEstimateRed)
                    }
                }
            }

            // Items list
            itemsIndexed(items) { index, item ->
                ItemCard(
                    index = index,
                    item = item,
                    categories = categories,
                    onUpdate = { updated ->
                        items = items.toMutableList().also { it[index] = updated }
                    },
                    onDelete = {
                        if (items.size > 1) {
                            items = items.toMutableList().also { it.removeAt(index) }
                        }
                    },
                    canDelete = items.size > 1
                )
            }

            // Hardware & Discount Adjustments
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Fittings, Accessories & Discount",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = hardwareCostText,
                                onValueChange = { hardwareCostText = it },
                                label = { Text("Hardware / Fittings (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("input_hardware_cost")
                            )

                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                label = { Text("Special Discount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("input_discount_amount")
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Specifications & Notes") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_notes")
                        )
                    }
                }
            }

            // Total Price Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBE9E7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Items Subtotal:", fontSize = 13.sp, color = Color(0xFF5D4037))
                            Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", itemsSubtotal)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        if (hardwareCost > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Hardware & Accessories:", fontSize = 13.sp, color = Color(0xFF5D4037))
                                Text("+ ₹${String.format(java.util.Locale.getDefault(), "%,.0f", hardwareCost)}", fontSize = 13.sp)
                            }
                        }
                        if (discountAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Promotional Discount:", fontSize = 13.sp, color = Color(0xFF2E7D32))
                                Text("- ₹${String.format(java.util.Locale.getDefault(), "%,.0f", discountAmount)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFFFCCBC))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Price Quote",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFFBF360C)
                                )
                                Text(
                                    text = "All inclusive with 25-yr durability",
                                    fontSize = 10.sp,
                                    color = Color(0xFF8D6E63)
                                )
                            }

                            Text(
                                text = "₹${String.format(java.util.Locale.getDefault(), "%,.0f", grandTotalPrice)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IconEstimateRed
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Primary Save to Firestore Button
                    Button(
                        onClick = {
                            if (customerName.isBlank() || customerPhone.isBlank()) {
                                viewModel.showMessage("Please fill Customer Name and Phone Number")
                                return@Button
                            }

                            isSaving = true
                            scope.launch {
                                try {
                                    // 1. Save to local Room database for offline instant access
                                    viewModel.addEstimate(
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        customerAddress = customerAddress,
                                        productType = items.joinToString(", ") { it.name },
                                        dimensions = items.joinToString(", ") { "${it.name}: ${it.dimensions}" },
                                        areaSqFt = items.size.toDouble(),
                                        ratePerSqFt = if (items.isNotEmpty()) items[0].unitPrice else 0.0,
                                        hardwareCost = hardwareCost,
                                        discountAmount = discountAmount,
                                        notes = notes
                                    )

                                    // 2. Save directly to Firestore if logged in
                                    if (currentUser != null) {
                                        val result = firestoreRepository.saveEstimateToFirestore(
                                            customerName = customerName,
                                            customerPhone = customerPhone,
                                            customerAddress = customerAddress,
                                            items = items,
                                            hardwareCost = hardwareCost,
                                            discountAmount = discountAmount,
                                            totalAmount = grandTotalPrice,
                                            notes = notes
                                        )
                                        if (result.isSuccess) {
                                            viewModel.showMessage("Estimate ${result.getOrNull()} saved to Firestore Cloud!")
                                        } else {
                                            viewModel.showMessage("Saved locally. Firestore sync note: ${result.exceptionOrNull()?.localizedMessage}")
                                        }
                                    } else {
                                        viewModel.showMessage("Estimate saved locally! Sign in with Google in Support to sync to Cloud.")
                                    }

                                    onBack()
                                } catch (e: Exception) {
                                    viewModel.showMessage("Error saving estimate: ${e.localizedMessage}")
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = IconEstimateRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_firestore_estimate")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Estimate to Firestore", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // Save & Share on WhatsApp
                    OutlinedButton(
                        onClick = {
                            if (customerName.isBlank() || customerPhone.isBlank()) {
                                viewModel.showMessage("Please fill Customer Name and Phone Number")
                                return@OutlinedButton
                            }

                            val itemsText = items.joinToString("\n") {
                                "• ${it.name} [${it.dimensions}] x ${it.quantity} = ₹${String.format(java.util.Locale.getDefault(), "%,.0f", it.subtotal)}"
                            }

                            val quoteMessage = """
                                *ARAVALLI STEEL & PVC FURNITURE*
                                _Official Estimate & Quotation_
                                ------------------------------------
                                *Customer:* $customerName
                                *Phone:* $customerPhone
                                *Site Address:* $customerAddress
                                
                                *Items Details:*
                                $itemsText
                                
                                Hardware & Fittings: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", hardwareCost)}
                                Discount: -₹${String.format(java.util.Locale.getDefault(), "%,.0f", discountAmount)}
                                ------------------------------------
                                *Total Price:* ₹${String.format(java.util.Locale.getDefault(), "%,.0f", grandTotalPrice)}
                                ------------------------------------
                                *Quality Guarantee:*
                                ✓ 100% Waterproof PVC (Zero swelling in water)
                                ✓ Heavy Gauge CRCA Steel Construction
                                ✓ 25-Year Long Durability
                                
                                Aravalli Steel & PVC Showroom
                                Highway Junction, Rajsamand / Udaipur
                                Contact: +91 98290 12345
                            """.trimIndent()

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, quoteMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Quotation via"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_share_whatsapp_estimate")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BrandGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Quotation on WhatsApp", color = BrandGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(
    index: Int,
    item: FurnitureEstimateItem,
    categories: List<String>,
    onUpdate: (FurnitureEstimateItem) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("estimate_item_$index")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Item #${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Remove Item", tint = Color(0xFFD32F2F))
                    }
                }
            }

            OutlinedTextField(
                value = item.name,
                onValueChange = { onUpdate(item.copy(name = it)) },
                label = { Text("Item Name / Description") },
                placeholder = { Text("e.g. L-Shape PVC Kitchen Cabinet") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category picker
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = item.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                onUpdate(item.copy(category = cat))
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = item.dimensions,
                    onValueChange = { onUpdate(item.copy(dimensions = it)) },
                    label = { Text("Size / Dimensions") },
                    placeholder = { Text("12 x 8 ft") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = if (item.quantity > 0) item.quantity.toString() else "1",
                    onValueChange = {
                        val q = it.toIntOrNull() ?: 1
                        onUpdate(item.copy(quantity = q, subtotal = q * item.unitPrice))
                    },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.7f)
                )

                OutlinedTextField(
                    value = if (item.unitPrice > 0) item.unitPrice.toLong().toString() else "0",
                    onValueChange = {
                        val price = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPrice = price, subtotal = item.quantity * price))
                    },
                    label = { Text("Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Subtotal: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", item.subtotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandGreenPrimary
                )
            }
        }
    }
}
