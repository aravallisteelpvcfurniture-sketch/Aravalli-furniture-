package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandGreenPrimary

data class FurnitureProduct(
    val id: String,
    val title: String,
    val category: String,
    val priceEstimate: String,
    val features: List<String>,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onRequestQuote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Modular Kitchen", "Steel Almirahs", "PVC Doors", "Wall & Ceiling", "Office Racks")
    var selectedCategory by remember { mutableStateOf("All") }

    val products = listOf(
        FurnitureProduct(
            id = "pvc_kitchen",
            title = "Acrylic Finish PVC Modular Kitchen",
            category = "Modular Kitchen",
            priceEstimate = "₹1,200 - ₹1,600 / sq.ft",
            features = listOf("100% Waterproof", "Termite Proof", "Soft Close Telescopic", "25-Yr Durability"),
            description = "High gloss acrylic PVC boards with seamless edge banding. Completely immune to water splashes, turmeric stains, and moisture rot.",
            icon = Icons.Default.Kitchen
        ),
        FurnitureProduct(
            id = "steel_almirah_3door",
            title = "Heavy 18-Gauge 3-Door Steel Almirah",
            category = "Steel Almirahs",
            priceEstimate = "₹35,000 - ₹55,000 / unit",
            features = listOf("18-Gauge CRCA Steel", "Digital Safe Vault", "Dressing Mirror", "Powder Coated"),
            description = "High security steel wardrobe with computerized electronic safe, tie-hangers, secret drawer, and anti-scratch electrostatic powder finish.",
            icon = Icons.Default.DoorSliding
        ),
        FurnitureProduct(
            id = "pvc_bathroom_doors",
            title = "Solid Waterproof PVC Bathroom Doors",
            category = "PVC Doors",
            priceEstimate = "₹3,500 - ₹6,500 / door",
            features = listOf("Zero Swelling", "Chemical Resistant", "Designer 3D Emboss", "Included PVC Frame"),
            description = "Heavy solid core PVC doors engineered specially for wet areas and terrace entries. Never warps or bends in steam or damp conditions.",
            icon = Icons.Default.MeetingRoom
        ),
        FurnitureProduct(
            id = "pvc_wall_panels",
            title = "Fluted Charcoal & UV Marble Wall Panels",
            category = "Wall & Ceiling",
            priceEstimate = "₹95 - ₹140 / sq.ft",
            features = listOf("Zero Painting Needed", "Fire Retardant", "Interlocking Click", "Luxury Aesthetics"),
            description = "Designer fluted louvers and UV marble sheets for living room TV units, bedroom backdrops, and moisture-prone damp walls.",
            icon = Icons.Default.Wallpaper
        ),
        FurnitureProduct(
            id = "steel_office_racks",
            title = "Industrial Slotted Angle Steel Racks",
            category = "Office Racks",
            priceEstimate = "₹4,500 - ₹12,000 / rack",
            features = listOf("Heavy Load Capacity", "Adjustable Shelves", "Rust-Proof Primer", "Cold-Rolled Sheet"),
            description = "Heavy duty steel shelving for warehouses, commercial storerooms, showrooms, and pharmacy filing racks.",
            icon = Icons.Default.Inventory2
        )
    )

    val filteredProducts = if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8F2))
    ) {
        // Top Header
        Surface(
            color = BrandGreenPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Aravalli Product Catalog",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Premium Steel & 100% Waterproof PVC Furniture",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) }
                )
            }
        }

        // Product list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts, key = { it.id }) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_item_${product.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = product.icon,
                                        contentDescription = null,
                                        tint = BrandGreenPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = product.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = product.priceEstimate,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandGreenPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Features tags
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            product.features.take(3).forEach { feat ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = "✓ $feat",
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onRequestQuote(product.title) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_quote_${product.id}")
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Estimate for this Product")
                        }
                    }
                }
            }
        }
    }
}
