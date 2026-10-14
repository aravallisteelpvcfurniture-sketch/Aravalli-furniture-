package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconGreetingsPurple

data class GreetingTemplate(
    val id: String,
    val title: String,
    val category: String,
    val bannerTitle: String,
    val messageText: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreetingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var clientName by remember { mutableStateOf("") }

    val templates = listOf(
        GreetingTemplate(
            id = "diwali",
            title = "Diwali Wishes",
            category = "Festivals",
            bannerTitle = "शुभ दीपावली 🪔",
            messageText = "May the divine festival of Diwali fill your life and home with joy, prosperity, and happiness. Celebrate with lifelong durable Aravalli Steel & PVC furniture!",
            gradientColors = listOf(Color(0xFFFF8F00), Color(0xFFD84315))
        ),
        GreetingTemplate(
            id = "dhanteras",
            title = "Dhanteras Special",
            category = "Festivals",
            bannerTitle = "धनतेरस की हार्दिक शुभकामनाएँ 🪙",
            messageText = "This Dhanteras, bring home auspicious metal and eternal beauty with Aravalli Heavy Steel Almirahs & Modern PVC Modular Kitchens. Special Festive discounts live!",
            gradientColors = listOf(Color(0xFFFFB300), Color(0xFFFF6F00))
        ),
        GreetingTemplate(
            id = "waterproof",
            title = "Monsoon Waterproof Offer",
            category = "Promotional",
            bannerTitle = "100% WATERPROOF GUARANTEE 💧",
            messageText = "Tired of swollen wooden bathroom and balcony doors? Upgrade to Aravalli 100% Waterproof, Termite-Proof PVC Doors & Modular Kitchens today. No rust, no rot, 25-yr life!",
            gradientColors = listOf(Color(0xFF0277BD), Color(0xFF00838F))
        ),
        GreetingTemplate(
            id = "newyear",
            title = "Happy New Year",
            category = "Festivals",
            bannerTitle = "नूतन वर्ष की शुभकामनाएँ ✨",
            messageText = "Wishing you and your family a healthy, prosperous, and joyful New Year. Thank you for making Aravalli Steel & PVC Furniture part of your home sweet home!",
            gradientColors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C))
        ),
        GreetingTemplate(
            id = "carpenter_appreciate",
            title = "Carpenter Mitra",
            category = "Partner",
            bannerTitle = "ARAVALLI CARPENTER MITRA 🔨",
            messageText = "We salute the craftsmanship of our fabricator and carpenter partners! Get guaranteed highest referral rewards and festive bonus on every project.",
            gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
        )
    )

    var selectedTemplate by remember { mutableStateOf(templates[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business & Festive Greetings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Personalized Marketing with Aravalli Branding", fontSize = 11.sp, color = Color.White.copy(0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("greetings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IconGreetingsPurple,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3E5F5).copy(alpha = 0.5f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Recipient / Client Name (Optional)") },
                    placeholder = { Text("e.g. Ramesh ji / Sharma Parivar") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("greeting_name_input")
                )
            }

            item {
                Text("Select Greeting Occasion:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(templates) { tmpl ->
                        FilterChip(
                            selected = selectedTemplate.id == tmpl.id,
                            onClick = { selectedTemplate = tmpl },
                            label = { Text(tmpl.title) }
                        )
                    }
                }
            }

            // Preview card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("greeting_preview_card")
                ) {
                    Column {
                        // Header banner of greeting
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(Brush.horizontalGradient(selectedTemplate.gradientColors))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = selectedTemplate.bannerTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ARAVALLI STEEL & PVC FURNITURE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }

                        // Message body
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (clientName.isNotBlank()) {
                                Text(
                                    text = "Respected $clientName Ji,",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            Text(
                                text = selectedTemplate.messageText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color(0xFF334155)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Aravalli Steel & PVC Furniture Showroom",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrandGreenPrimary
                            )
                            Text(
                                text = "Authorized Real Plast Distributor • Highway Road, Rajsamand / Udaipur",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "Contact: +91 98290 12345",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val greetingText = buildGreetingMessage(selectedTemplate, clientName)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, greetingText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Greeting via"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IconGreetingsPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("greeting_share_whatsapp")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share WhatsApp")
                    }

                    OutlinedButton(
                        onClick = {
                            val greetingText = buildGreetingMessage(selectedTemplate, clientName)
                            clipboardManager.setText(AnnotatedString(greetingText))
                            viewModel.showMessage("Greeting message copied to clipboard!")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("greeting_copy_text")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Text")
                    }
                }
            }
        }
    }
}

fun buildGreetingMessage(template: GreetingTemplate, clientName: String): String {
    val salutation = if (clientName.isNotBlank()) "Respected $clientName Ji,\n\n" else ""
    return """
        $salutation*${template.bannerTitle}*
        
        ${template.messageText}
        
        ------------------------------------
        *ARAVALLI STEEL & PVC FURNITURE*
        ✓ 100% Waterproof PVC Modular Kitchens
        ✓ Designer Steel Wardrobes & Almirahs
        ✓ Premium PVC Bathroom & Balcony Doors
        
        Showroom: Near Highway, Rajsamand / Udaipur (Rajasthan)
        Phone: +91 98290 12345
    """.trimIndent()
}
