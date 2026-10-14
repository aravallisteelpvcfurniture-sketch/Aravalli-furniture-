package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.repository.FirestoreRepository
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandGreenPrimary
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val attendance by viewModel.attendance.collectAsState()
    val estimates by viewModel.estimates.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val visitors by viewModel.visitors.collectAsState()
    val carpenters by viewModel.carpenters.collectAsState()

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    val firestoreRepository = remember { FirestoreRepository(context) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

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
                    text = "Aravalli Showroom & Support",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Business Profile, Attendance & Cloud Sync",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Business Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_business_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(BrandGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            Column {
                                Text("Aravalli Steel & PVC Furniture", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Real Plast Authorized Distributor", color = BrandGreenPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("GSTIN: 08AABCA1234F1Z5", color = Color(0xFF64748B), fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "National Highway Junction, Rajsamand / Udaipur, Rajasthan",
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Showroom Timings: 9:00 AM - 8:30 PM (Mon - Sun)",
                                fontSize = 12.sp,
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }
            }

            // Firebase Cloud Sync & Google Sign-In Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("firebase_sync_card"),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = BrandGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text("Firebase Cloud Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Database: gen-lang-client-0439788859", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentUser != null) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (currentUser != null) "Cloud Connected" else "Offline Only",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser != null) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (currentUser != null) {
                            Text(
                                text = "Signed in as: ${currentUser?.displayName ?: currentUser?.email ?: "Authorized Staff"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isSyncing = true
                                        scope.launch {
                                            try {
                                                estimates.forEach { firestoreRepository.syncEstimate(it) }
                                                orders.forEach { firestoreRepository.syncOrder(it) }
                                                visitors.forEach { firestoreRepository.syncVisitor(it) }
                                                carpenters.forEach { firestoreRepository.syncCarpenter(it) }
                                                viewModel.showMessage("Successfully synced all data to Firestore Cloud!")
                                            } catch (e: Exception) {
                                                viewModel.showMessage("Sync error: ${e.localizedMessage}")
                                            } finally {
                                                isSyncing = false
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                                    modifier = Modifier.weight(1.3f).testTag("btn_sync_now")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sync to Cloud", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            Firebase.auth.signOut()
                                            val credMgr = CredentialManager.create(context)
                                            try {
                                                credMgr.clearCredentialState(ClearCredentialStateRequest())
                                            } catch (e: Exception) {
                                                Log.w("Auth", "Clear credentials failed", e)
                                            }
                                            viewModel.showMessage("Signed out from Cloud Sync.")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_sign_out")
                                ) {
                                    Text("Sign Out", fontSize = 12.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Sign in with Google to enable real-time cloud backup for all estimates, confirmed orders, and carpenter records.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    isSigningIn = true
                                    val credManager = CredentialManager.create(context)
                                    val clientId = try {
                                        context.getString(R.string.default_web_client_id)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Web client ID not configured in google-services.json")
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
                                                viewModel.showMessage("Signed in successfully with Google!")
                                            } else {
                                                viewModel.showMessage("Unexpected credential type received.")
                                            }
                                        } catch (e: GetCredentialCancellationException) {
                                            Log.w("Auth", "Google sign-in cancelled: ${e.message}", e)
                                        } catch (e: Exception) {
                                            Log.e("Auth", "Google Sign-In failed", e)
                                            viewModel.showMessage("Sign in error: ${e.localizedMessage}")
                                        } finally {
                                            isSigningIn = false
                                        }
                                    }
                                },
                                enabled = !isSigningIn,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("btn_google_signin")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign in with Google")
                                }
                            }
                        }
                    }
                }
            }

            // Attendance Status card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Staff & Showroom Attendance", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val isCheckedIn = attendance?.isCheckedIn == true
                                Text(
                                    text = if (isCheckedIn) "Status: Store Open / Checked In" else "Status: Store Closed / Checked Out",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isCheckedIn) BrandGreenPrimary else Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "Last Check-in: ${attendance?.checkInTime ?: "09:00 AM"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = { viewModel.toggleCheckIn() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (attendance?.isCheckedIn == true) Color(0xFFD32F2F) else BrandGreenPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (attendance?.isCheckedIn == true) "Check Out" else "Check In")
                            }
                        }
                    }
                }
            }

            // Quick Support & Helpline
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Dealer Helpdesk & Direct Contact", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919829012345"))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Showroom")
                            }

                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("https://wa.me/919829012345?text=Hello%20Aravalli%20Steel%20%26%20PVC%20Furniture,%20I%20have%20an%20enquiry.")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp")
                            }
                        }
                    }
                }
            }
        }
    }
}
