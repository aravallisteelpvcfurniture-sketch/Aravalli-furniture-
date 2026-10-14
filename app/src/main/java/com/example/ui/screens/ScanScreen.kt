package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.SubScreen
import com.example.ui.theme.BrandGreenPrimary

@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            viewModel.showMessage("Camera permission is required to scan QR codes.")
        }
    }

    var manualCodeInput by remember { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Scanner Laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserOffset"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Bar
        Surface(
            color = BrandGreenPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aravalli QR Scanner",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier.testTag("btn_toggle_flash")
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) Color(0xFFFFD54F) else Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Align Order QR or Carpenter Card",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Scanner Viewfinder Box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .border(2.dp, BrandGreenPrimary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Corner markers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(36.dp).align(Alignment.TopStart))
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(36.dp).align(Alignment.TopEnd))
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(36.dp).align(Alignment.BottomStart))
                    Icon(Icons.Default.CropFree, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(36.dp).align(Alignment.BottomEnd))
                }

                // Laser scan bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = (laserOffset - 110).dp)
                        .background(Color(0xFF00E676))
                )

                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!hasCameraPermission) {
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("request_camera_btn")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Camera Permission")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Simulate / Manual Entry
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Or enter Order / Member ID manually:",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = manualCodeInput,
                            onValueChange = { manualCodeInput = it },
                            placeholder = { Text("e.g. ORD-ARV-2026-101", color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandGreenPrimary,
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("manual_scan_input")
                        )

                        Button(
                            onClick = {
                                if (manualCodeInput.isNotBlank()) {
                                    scanResult = manualCodeInput
                                    viewModel.showMessage("Found record for: $manualCodeInput")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                            modifier = Modifier.testTag("btn_lookup_code")
                        ) {
                            Text("Find")
                        }
                    }

                    if (scanResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF0F766E),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Verified: $scanResult", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                TextButton(onClick = { viewModel.setSubScreen(SubScreen.CONFIRM_ORDER_LIST) }) {
                                    Text("Open Order", color = Color(0xFF67E8F9), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
