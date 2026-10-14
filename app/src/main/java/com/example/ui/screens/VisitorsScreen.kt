package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.data.model.Visitor
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconVisitorsBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitorsScreen(
    viewModel: MainViewModel,
    visitors: List<Visitor>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filtered = when (selectedFilter) {
        "Hot" -> visitors.filter { it.priority == "Hot" }
        "Warm" -> visitors.filter { it.priority == "Warm" }
        "Cold" -> visitors.filter { it.priority == "Cold" }
        else -> visitors
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Showroom Visitors Register", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Walk-in Inquiries & Lead Management", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("visitors_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IconVisitorsBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Log Visitor") },
                containerColor = IconVisitorsBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_visitor")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE8EAF6).copy(alpha = 0.5f))
        ) {
            // Priority Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Hot", "Warm", "Cold").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No visitor entries found for $selectedFilter", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { visitor ->
                        VisitorItemCard(
                            visitor = visitor,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${visitor.phone}"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                val cleanPhone = visitor.phone.replace(Regex("[^0-9]"), "")
                                val uri = Uri.parse("https://wa.me/$cleanPhone?text=Hello%20${visitor.name}%20Ji,%20thank%20you%20for%20visiting%20Aravalli%20Steel%20%26%20PVC%20Furniture%20showroom.")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            },
                            onCreateEstimate = {
                                viewModel.setSubScreen(SubScreen.ESTIMATE_LIST)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVisitorDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, address, product, budget, priority, followUp, notes ->
                viewModel.addVisitor(name, phone, address, product, budget, priority, followUp, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun VisitorItemCard(
    visitor: Visitor,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onCreateEstimate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visitor_card_${visitor.id}"),
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
                    Text(text = visitor.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(text = visitor.phone, fontSize = 13.sp, color = Color(0xFF64748B))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (visitor.priority) {
                        "Hot" -> Color(0xFFFFEBEE)
                        "Warm" -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE8F5E9)
                    }
                ) {
                    Text(
                        text = "${visitor.priority} Lead",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = when (visitor.priority) {
                            "Hot" -> Color(0xFFC62828)
                            "Warm" -> Color(0xFFEF6C00)
                            else -> Color(0xFF2E7D32)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            Text(text = "Interested in: ${visitor.interestedProduct}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            if (visitor.address.isNotEmpty()) {
                Text(text = "Address: ${visitor.address}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            if (visitor.budget.isNotEmpty()) {
                Text(text = "Budget: ${visitor.budget}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            if (visitor.notes.isNotEmpty()) {
                Text(text = "Note: ${visitor.notes}", fontSize = 12.sp, color = Color(0xFF475569))
            }
            if (visitor.followUpDate.isNotEmpty()) {
                Text(text = "Follow up: ${visitor.followUpDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = IconVisitorsBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp)
                }

                Button(
                    onClick = onCreateEstimate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text("Estimate", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddVisitorDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        phone: String,
        address: String,
        product: String,
        budget: String,
        priority: String,
        followUp: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("PVC Modular Kitchen") }
    var budget by remember { mutableStateOf("₹1.5 Lakh - ₹2 Lakh") }
    var priority by remember { mutableStateOf("Hot") }
    var followUp by remember { mutableStateOf("2026-10-15") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log New Visitor", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Visitor Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Area / Locality") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = product,
                        onValueChange = { product = it },
                        label = { Text("Interested Product") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = budget,
                        onValueChange = { budget = it },
                        label = { Text("Budget Range") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Lead Priority:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Hot", "Warm", "Cold").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = followUp,
                        onValueChange = { followUp = it },
                        label = { Text("Follow-up Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Discussion Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, address, product, budget, priority, followUp, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IconVisitorsBlue)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
