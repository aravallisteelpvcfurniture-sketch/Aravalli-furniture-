package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.rotate
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
import com.example.data.model.Carpenter
import com.example.data.model.CarpenterAssignment
import com.example.ui.MainViewModel
import com.example.ui.theme.BrandGreenPrimary
import com.example.ui.theme.IconCarpenterCyan
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
fun CarpentersScreen(
    viewModel: MainViewModel,
    carpenters: List<Carpenter>,
    assignments: List<CarpenterAssignment> = emptyList(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Carpenters, 1: Active Projects, 2: Rewards & Scheme
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    // Dialog states
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showAssignProjectDialog by remember { mutableStateOf(false) }
    var carpenterPreselectedForProject by remember { mutableStateOf<Carpenter?>(null) }
    var selectedCarpenterForDetail by remember { mutableStateOf<Carpenter?>(null) }
    var selectedCarpenterForReferral by remember { mutableStateOf<Carpenter?>(null) }
    var selectedCarpenterForPayout by remember { mutableStateOf<Carpenter?>(null) }
    var selectedAssignmentForUpdate by remember { mutableStateOf<CarpenterAssignment?>(null) }

    // Cloud auth state
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isSigningIn by remember { mutableStateOf(false) }
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    // Rotation animation for cloud sync icon
    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Aravalli Carpenter Club",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Firestore Cloud Directory & Active Projects",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("carpenters_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Sync All to Firestore Button
                    IconButton(
                        onClick = {
                            if (currentUser == null) {
                                viewModel.showMessage("Sign in with Google below to enable Cloud Sync.")
                            } else {
                                viewModel.syncAllCarpentersToFirestore()
                            }
                        },
                        enabled = !isCloudSyncing,
                        modifier = Modifier.testTag("btn_sync_all_carpenters")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sync with Firestore",
                            tint = Color.White,
                            modifier = if (isCloudSyncing) Modifier.rotate(rotationAngle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IconCarpenterCyan,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Secondary FAB: Assign Project
                FloatingActionButton(
                    onClick = {
                        carpenterPreselectedForProject = null
                        showAssignProjectDialog = true
                    },
                    containerColor = Color(0xFF00838F),
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp).testTag("fab_assign_project")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = "Assign Project", modifier = Modifier.size(22.dp))
                }

                // Primary Extended FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        if (selectedTab == 1) {
                            carpenterPreselectedForProject = null
                            showAssignProjectDialog = true
                        } else {
                            showRegisterDialog = true
                        }
                    },
                    icon = {
                        Icon(
                            if (selectedTab == 1) Icons.Default.PostAdd else Icons.Default.PersonAdd,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(if (selectedTab == 1) "Assign Project" else "Register Carpenter")
                    },
                    containerColor = IconCarpenterCyan,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_carpenter_action")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF0FDF4))
        ) {
            // Firestore Cloud Sync Status Banner
            Surface(
                color = if (currentUser != null) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth().testTag("firestore_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (currentUser != null) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (currentUser != null) "Firestore Cloud Connected" else "Cloud Sync Offline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (currentUser != null) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                            )
                            Text(
                                text = if (currentUser != null) {
                                    currentUser?.email ?: "Google Account Active"
                                } else {
                                    "Sign in with Google to sync carpenters & project assignments"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                    }

                    if (currentUser == null) {
                        Button(
                            onClick = {
                                isSigningIn = true
                                val credManager = CredentialManager.create(context)
                                val serverClientId = context.getString(R.string.default_web_client_id)
                                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                                scope.launch {
                                    try {
                                        val result = credManager.getCredential(context as Activity, request)
                                        val credential = result.credential
                                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                            Firebase.auth.signInWithCredential(authCredential).await()
                                            viewModel.showMessage("Google Cloud Sign-In successful! Carpenter Club synced.")
                                            viewModel.syncAllCarpentersToFirestore()
                                        }
                                    } catch (e: GetCredentialCancellationException) {
                                        Log.w("Auth", "Sign-in cancelled", e)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Sign-in notice: ${e.localizedMessage}")
                                    } finally {
                                        isSigningIn = false
                                    }
                                }
                            },
                            enabled = !isSigningIn,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("btn_google_signin_carpenters")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign In", fontSize = 11.sp)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.syncAllCarpentersToFirestore() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync All", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = IconCarpenterCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Carpenters (${carpenters.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_carpenters")
                )
                val activeProjectsCount = assignments.count { it.status != "Completed" }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Active Projects ($activeProjectsCount)", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_active_projects")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Rewards & Scheme", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_rewards_scheme")
                )
            }

            // Tab Contents
            when (selectedTab) {
                0 -> {
                    // TAB 0: Carpenters Directory
                    CarpentersDirectoryTab(
                        carpenters = carpenters,
                        assignments = assignments,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        tierFilter = selectedTierFilter,
                        onTierFilterChange = { selectedTierFilter = it },
                        onCall = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        onWhatsApp = { carp ->
                            openWhatsAppChat(context, carp.phone, "Namaste ${carp.name} ji! Greetings from Aravalli Steel & PVC Furniture.")
                        },
                        onViewDetails = { selectedCarpenterForDetail = it },
                        onAssignProject = { carp ->
                            carpenterPreselectedForProject = carp
                            showAssignProjectDialog = true
                        },
                        onAddReferral = { selectedCarpenterForReferral = it },
                        onPayout = { selectedCarpenterForPayout = it },
                        onSyncCloud = { carp -> viewModel.syncCarpenterToFirestore(carp) }
                    )
                }
                1 -> {
                    // TAB 1: Active Project Assignments
                    ActiveProjectsTab(
                        carpenters = carpenters,
                        assignments = assignments,
                        statusFilter = selectedStatusFilter,
                        onStatusFilterChange = { selectedStatusFilter = it },
                        onCallClient = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        onCallCarpenter = { carpId ->
                            val c = carpenters.find { it.id == carpId }
                            if (c != null) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onShareSlip = { assign, carpName ->
                            shareProjectSlipOnWhatsApp(context, assign, carpName)
                        },
                        onUpdateStatus = { selectedAssignmentForUpdate = it }
                    )
                }
                2 -> {
                    // TAB 2: Rewards, Schemes & Commission Ledger
                    RewardsAndSchemeTab(
                        carpenters = carpenters,
                        onPayout = { selectedCarpenterForPayout = it },
                        onAddReferral = { selectedCarpenterForReferral = it }
                    )
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Register Carpenter Partner
    if (showRegisterDialog) {
        RegisterCarpenterDialogFull(
            onDismiss = { showRegisterDialog = false },
            onSave = { name, phone, location, specialty, expYears, tier ->
                viewModel.addCarpenter(
                    name = name,
                    phone = phone,
                    location = location,
                    specialty = specialty,
                    experienceYears = expYears,
                    tier = tier
                )
                showRegisterDialog = false
            }
        )
    }

    // 2. Assign Project to Carpenter
    if (showAssignProjectDialog) {
        AssignProjectDialog(
            carpenters = carpenters,
            preselectedCarpenter = carpenterPreselectedForProject,
            onDismiss = { showAssignProjectDialog = false },
            onAssign = { carpId, clientName, clientPhone, siteAddress, workType, laborCharge, advancePaid, targetDate, notes ->
                viewModel.addAssignment(
                    carpenterId = carpId,
                    clientName = clientName,
                    clientPhone = clientPhone,
                    siteAddress = siteAddress,
                    workType = workType,
                    laborCharge = laborCharge,
                    advancePaid = advancePaid,
                    targetCompletionDate = targetDate,
                    notes = notes
                )
                showAssignProjectDialog = false
            }
        )
    }

    // 3. Update Project Status & Payment
    selectedAssignmentForUpdate?.let { assign ->
        val carp = carpenters.find { it.id == assign.carpenterId }
        UpdateProjectStatusDialog(
            assignment = assign,
            carpenterName = carp?.name ?: "Carpenter",
            onDismiss = { selectedAssignmentForUpdate = null },
            onConfirm = { newStatus, additionalPayment ->
                viewModel.updateAssignmentStatus(assign, newStatus, additionalPayment)
                selectedAssignmentForUpdate = null
            }
        )
    }

    // 4. Carpenter Details & Projects Modal
    selectedCarpenterForDetail?.let { carp ->
        val carpProjects = assignments.filter { it.carpenterId == carp.id }
        CarpenterDetailDialog(
            carpenter = carp,
            assignments = carpProjects,
            onDismiss = { selectedCarpenterForDetail = null },
            onCall = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${carp.phone}"))
                context.startActivity(intent)
            },
            onWhatsApp = {
                openWhatsAppChat(context, carp.phone, "Namaste ${carp.name} ji, this is from Aravalli Steel & PVC Furniture showroom.")
            },
            onAssignProject = {
                selectedCarpenterForDetail = null
                carpenterPreselectedForProject = carp
                showAssignProjectDialog = true
            },
            onSyncCloud = {
                viewModel.syncCarpenterToFirestore(carp)
            }
        )
    }

    // 5. Add Referral Dialog
    selectedCarpenterForReferral?.let { carp ->
        AddReferralDialog(
            carpenter = carp,
            onDismiss = { selectedCarpenterForReferral = null },
            onConfirm = { commission, pts ->
                viewModel.recordCarpenterReferral(carp, commission, pts)
                selectedCarpenterForReferral = null
            }
        )
    }

    // 6. Payout Dialog
    selectedCarpenterForPayout?.let { carp ->
        PayoutDialog(
            carpenter = carp,
            onDismiss = { selectedCarpenterForPayout = null },
            onConfirm = { amt ->
                viewModel.payoutCarpenter(carp, amt)
                selectedCarpenterForPayout = null
            }
        )
    }
}

// ==========================================
// TAB 0: CARPENTERS DIRECTORY VIEW
// ==========================================
@Composable
fun CarpentersDirectoryTab(
    carpenters: List<Carpenter>,
    assignments: List<CarpenterAssignment>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    tierFilter: String,
    onTierFilterChange: (String) -> Unit,
    onCall: (String) -> Unit,
    onWhatsApp: (Carpenter) -> Unit,
    onViewDetails: (Carpenter) -> Unit,
    onAssignProject: (Carpenter) -> Unit,
    onAddReferral: (Carpenter) -> Unit,
    onPayout: (Carpenter) -> Unit,
    onSyncCloud: (Carpenter) -> Unit
) {
    val filteredCarpenters = carpenters.filter { carp ->
        val matchesQuery = carp.name.contains(searchQuery, ignoreCase = true) ||
                carp.location.contains(searchQuery, ignoreCase = true) ||
                carp.specialty.contains(searchQuery, ignoreCase = true) ||
                carp.phone.contains(searchQuery)
        val matchesTier = tierFilter == "All" || carp.tier == tierFilter
        matchesQuery && matchesTier
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        Surface(color = Color.White, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search by name, location, or specialty...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("input_search_carpenters")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tier Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tiers = listOf("All", "Platinum Mitra", "Gold Mitra", "Silver Mitra")
                    items(tiers) { tier ->
                        FilterChip(
                            selected = tierFilter == tier,
                            onClick = { onTierFilterChange(tier) },
                            label = { Text(tier, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F7FA),
                                selectedLabelColor = Color(0xFF006064)
                            )
                        )
                    }
                }
            }
        }

        // List
        if (filteredCarpenters.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No carpenters found", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Text("Try adjusting your search or register a new partner.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCarpenters, key = { it.id }) { carpenter ->
                    val activeCount = assignments.count { it.carpenterId == carpenter.id && it.status != "Completed" }
                    CarpenterCardV2(
                        carpenter = carpenter,
                        activeProjectsCount = activeCount,
                        onCall = { onCall(carpenter.phone) },
                        onWhatsApp = { onWhatsApp(carpenter) },
                        onViewDetails = { onViewDetails(carpenter) },
                        onAssignProject = { onAssignProject(carpenter) },
                        onAddReferral = { onAddReferral(carpenter) },
                        onPayout = { onPayout(carpenter) },
                        onSyncCloud = { onSyncCloud(carpenter) }
                    )
                }
            }
        }
    }
}

@Composable
fun CarpenterCardV2(
    carpenter: Carpenter,
    activeProjectsCount: Int,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onViewDetails: () -> Unit,
    onAssignProject: () -> Unit,
    onAddReferral: () -> Unit,
    onPayout: () -> Unit,
    onSyncCloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("carpenter_card_${carpenter.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Tier badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            when (carpenter.tier) {
                                "Platinum Mitra" -> Color(0xFFE2E8F0)
                                "Gold Mitra" -> Color(0xFFFFF8E1)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .border(
                            1.5.dp,
                            when (carpenter.tier) {
                                "Platinum Mitra" -> Color(0xFF94A3B8)
                                "Gold Mitra" -> Color(0xFFFFB300)
                                else -> Color(0xFFCBD5E1)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = carpenter.name.firstOrNull()?.toString()?.uppercase() ?: "C",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = carpenter.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        // Cloud sync icon badge
                        IconButton(
                            onClick = onSyncCloud,
                            modifier = Modifier.size(24.dp).testTag("btn_sync_carpenter_${carpenter.id}")
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Sync to Firestore",
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${carpenter.specialty} • ${carpenter.experienceYears} yrs exp",
                        fontSize = 11.sp,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "${carpenter.location} • ${carpenter.phone}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Tier Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (carpenter.tier) {
                        "Platinum Mitra" -> Color(0xFFEDE7F6)
                        "Gold Mitra" -> Color(0xFFFFF8E1)
                        else -> Color(0xFFE0F7FA)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (carpenter.tier) {
                                "Platinum Mitra" -> Icons.Default.WorkspacePremium
                                "Gold Mitra" -> Icons.Default.Stars
                                else -> Icons.Default.Grade
                            },
                            contentDescription = null,
                            tint = when (carpenter.tier) {
                                "Platinum Mitra" -> Color(0xFF6A1B9A)
                                "Gold Mitra" -> Color(0xFFFF8F00)
                                else -> Color(0xFF00838F)
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = carpenter.tier.replace(" Mitra", ""),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (carpenter.tier) {
                                "Platinum Mitra" -> Color(0xFF4A148C)
                                "Gold Mitra" -> Color(0xFFE65100)
                                else -> Color(0xFF006064)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Active Sites", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = "$activeProjectsCount Active",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (activeProjectsCount > 0) Color(0xFF0284C7) else Color(0xFF94A3B8)
                    )
                }
                Column {
                    Text("Points Earned", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("${carpenter.pointsEarned} Pts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00838F))
                }
                Column {
                    Text("Total Referrals", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("${carpenter.totalReferrals} Orders", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending Payout", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        "₹${String.format(java.util.Locale.getDefault(), "%,.0f", carpenter.pendingPayout)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (carpenter.pendingPayout > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(0.9f).height(36.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 11.sp, color = Color(0xFF15803D))
                }

                Button(
                    onClick = onAssignProject,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.3f).height(36.dp).testTag("btn_assign_to_${carpenter.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Assign Site", fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(0.9f).height(36.dp)
                ) {
                    Text("Projects", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// TAB 1: ACTIVE PROJECT ASSIGNMENTS VIEW
// ==========================================
@Composable
fun ActiveProjectsTab(
    carpenters: List<Carpenter>,
    assignments: List<CarpenterAssignment>,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onCallClient: (String) -> Unit,
    onCallCarpenter: (Long) -> Unit,
    onShareSlip: (CarpenterAssignment, String) -> Unit,
    onUpdateStatus: (CarpenterAssignment) -> Unit
) {
    val filteredAssignments = assignments.filter { assign ->
        if (statusFilter == "All") true
        else assign.status == statusFilter
    }

    val totalLaborValue = assignments.sumOf { it.laborCharge }
    val totalAdvancePaid = assignments.sumOf { it.advancePaid }
    val totalBalanceDue = (totalLaborValue - totalAdvancePaid).coerceAtLeast(0.0)

    Column(modifier = Modifier.fillMaxSize()) {
        // Summary Card
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Project Assignments Overview", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Text("${assignments.size} Total Sites", fontSize = 11.sp, color = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Projects", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${assignments.count { it.status != "Completed" }} Sites", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0284C7))
                    }
                    Column {
                        Text("Labor Contract", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalLaborValue)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Advance Paid", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalAdvancePaid)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Balance Due", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalBalanceDue)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                    }
                }
            }
        }

        // Status Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val statuses = listOf("All", "In Progress", "Assigned", "Inspection Pending", "Completed")
            items(statuses) { status ->
                FilterChip(
                    selected = statusFilter == status,
                    onClick = { onStatusFilterChange(status) },
                    label = { Text(status, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (status) {
                            "Completed" -> Color(0xFFE8F5E9)
                            "In Progress" -> Color(0xFFE0F2FE)
                            "Inspection Pending" -> Color(0xFFEDE7F6)
                            else -> Color(0xFFFFF3E0)
                        },
                        selectedLabelColor = when (status) {
                            "Completed" -> Color(0xFF1B5E20)
                            "In Progress" -> Color(0xFF0369A1)
                            "Inspection Pending" -> Color(0xFF6A1B9A)
                            else -> Color(0xFFB45309)
                        }
                    )
                )
            }
        }

        // Project Cards List
        if (filteredAssignments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No project assignments found", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                    Text("Tap 'Assign Project' below to allocate a site to a carpenter.", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAssignments, key = { it.id }) { assignment ->
                    val carpenter = carpenters.find { it.id == assignment.carpenterId }
                    val carpenterName = carpenter?.name ?: "Assigned Carpenter #${assignment.carpenterId}"

                    ProjectAssignmentCard(
                        assignment = assignment,
                        carpenter = carpenter,
                        carpenterName = carpenterName,
                        onCallClient = { onCallClient(assignment.clientPhone) },
                        onCallCarpenter = { onCallCarpenter(assignment.carpenterId) },
                        onShareSlip = { onShareSlip(assignment, carpenterName) },
                        onUpdateStatus = { onUpdateStatus(assignment) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectAssignmentCard(
    assignment: CarpenterAssignment,
    carpenter: Carpenter?,
    carpenterName: String,
    onCallClient: () -> Unit,
    onCallCarpenter: () -> Unit,
    onShareSlip: () -> Unit,
    onUpdateStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balanceDue = (assignment.laborCharge - assignment.advancePaid).coerceAtLeast(0.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("assignment_card_${assignment.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Work Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Handyman,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = assignment.workType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (assignment.status) {
                        "Completed" -> Color(0xFFE8F5E9)
                        "In Progress" -> Color(0xFFE0F2FE)
                        "Inspection Pending" -> Color(0xFFEDE7F6)
                        else -> Color(0xFFFFF3E0)
                    }
                ) {
                    Text(
                        text = assignment.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (assignment.status) {
                            "Completed" -> Color(0xFF1B5E20)
                            "In Progress" -> Color(0xFF0369A1)
                            "Inspection Pending" -> Color(0xFF6A1B9A)
                            else -> Color(0xFFB45309)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Carpenter Banner
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF00838F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Carpenter: $carpenterName ${carpenter?.let { "(${it.tier})" } ?: ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                    IconButton(onClick = onCallCarpenter, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = "Call Carpenter", tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Client & Site Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Client: ${assignment.clientName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Site: ${assignment.siteAddress}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    if (assignment.notes.isNotBlank()) {
                        Text(
                            text = "Note: ${assignment.notes}",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
                if (assignment.clientPhone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCallClient,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Client", fontSize = 10.sp)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            // Financial & Dates Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Labor Charge", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.laborCharge)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Advance Paid", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.advancePaid)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
                Column {
                    Text("Balance Due", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        "₹${String.format(java.util.Locale.getDefault(), "%,.0f", balanceDue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (balanceDue > 0) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target Date", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(assignment.targetCompletionDate, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF0369A1))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareSlip,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(34.dp).testTag("btn_share_slip_${assignment.id}")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp Slip", fontSize = 11.sp, color = Color(0xFF15803D))
                }

                Button(
                    onClick = onUpdateStatus,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IconCarpenterCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.2f).height(34.dp).testTag("btn_update_status_${assignment.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Status & Pay", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// TAB 2: REWARDS, SCHEMES & PAYOUTS VIEW
// ==========================================
@Composable
fun RewardsAndSchemeTab(
    carpenters: List<Carpenter>,
    onPayout: (Carpenter) -> Unit,
    onAddReferral: (Carpenter) -> Unit
) {
    val pendingPayoutCarpenters = carpenters.filter { it.pendingPayout > 0 }
    val totalPendingPayouts = carpenters.sumOf { it.pendingPayout }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF006064)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Aravalli Mitra Partner Program", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Direct Commission & Gift Rewards for Fabricators", fontSize = 11.sp, color = Color.White.copy(0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("PVC Kitchens", fontSize = 10.sp, color = Color.White.copy(0.8f))
                                Text("3% Commission", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD54F))
                                Text("₹3,000 to ₹10,000/site", fontSize = 9.sp, color = Color.White.copy(0.7f))
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Steel Almirahs", fontSize = 10.sp, color = Color.White.copy(0.8f))
                                Text("₹1,500 Fix Bonus", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD54F))
                                Text("Per 3-door wardrobe", fontSize = 9.sp, color = Color.White.copy(0.7f))
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Reward Points", fontSize = 10.sp, color = Color.White.copy(0.8f))
                                Text("10 pts / ₹1K", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFD54F))
                                Text("Diwali tool gift box", fontSize = 9.sp, color = Color.White.copy(0.7f))
                            }
                        }
                    }
                }
            }
        }

        // Pending Commission Settlements Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Commission Settlements",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Total: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalPendingPayouts)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFE65100)
                )
            }
        }

        if (pendingPayoutCarpenters.isEmpty()) {
            item {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("All Carpenter Commissions Settled!", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("No pending payouts. New commissions can be credited via referral sites.", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }
        } else {
            items(pendingPayoutCarpenters) { carp ->
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(carp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${carp.location} • ${carp.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                "Pending: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", carp.pendingPayout)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                        }

                        Button(
                            onClick = { onPayout(carp) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settle UPI", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Tier Milestone Gift Rewards
        item {
            Text(
                text = "Carpenter Mitra Milestone Gifts",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GiftMilestoneCard("500 Points", "Bosch Professional Measuring Tape & Level", "Unlocked for all Gold Mitras", true)
                GiftMilestoneCard("1,000 Points", "Heavy Duty 750W Electric Drill & Bit Set", "Eligible: Ramesh Sharma, Dinesh Mistri", true)
                GiftMilestoneCard("2,500 Points", "Cordless Circular Saw & Diwali Cash Bonus of ₹5,000", "Platinum Mitra Exclusive", false)
            }
        }
    }
}

@Composable
fun GiftMilestoneCard(
    points: String,
    title: String,
    subtitle: String,
    isUnlocked: Boolean
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.CardGiftcard else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color(0xFF2E7D32) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(points, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color(0xFF00838F) else Color(0xFF64748B))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(subtitle, fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }
    }
}

// ==========================================
// DIALOGS IMPLEMENTATION
// ==========================================

// 1. Full Register Carpenter Partner Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterCarpenterDialogFull(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, location: String, specialty: String, expYears: Int, tier: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("PVC Modular Kitchens") }
    var experienceYearsText by remember { mutableStateOf("8") }
    var tier by remember { mutableStateOf("Gold Mitra") }

    val specialties = listOf(
        "PVC Modular Kitchens",
        "Steel Wardrobe & Almirah Fitter",
        "PVC Waterproof Door Master",
        "PVC Fluted Ceiling & Paneling",
        "Aluminium & Glass Fabrication",
        "General Modular Furniture"
    )
    val tiers = listOf("Silver Mitra", "Gold Mitra", "Platinum Mitra")
    var specialtyExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = IconCarpenterCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Carpenter Partner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Carpenter details will be stored locally and synced to Firestore Cloud.", fontSize = 11.sp, color = Color(0xFF64748B))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Carpenter / Mistri Name *") },
                    placeholder = { Text("e.g. Ramesh Sharma") },
                    modifier = Modifier.fillMaxWidth().testTag("input_carpenter_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile / WhatsApp Number *") },
                    placeholder = { Text("+91 98290 12345") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_carpenter_phone")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Workshop City / Area") },
                    placeholder = { Text("e.g. Nathdwara / Rajsamand") },
                    modifier = Modifier.fillMaxWidth().testTag("input_carpenter_location")
                )

                // Specialty Dropdown
                ExposedDropdownMenuBox(
                    expanded = specialtyExpanded,
                    onExpandedChange = { specialtyExpanded = !specialtyExpanded }
                ) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Furniture Specialty") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = specialtyExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = specialtyExpanded,
                        onDismissRequest = { specialtyExpanded = false }
                    ) {
                        specialties.forEach { spec ->
                            DropdownMenuItem(
                                text = { Text(spec, fontSize = 13.sp) },
                                onClick = {
                                    specialty = spec
                                    specialtyExpanded = false
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
                        value = experienceYearsText,
                        onValueChange = { experienceYearsText = it },
                        label = { Text("Experience (Yrs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    // Tier selection
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Partner Tier", fontSize = 11.sp, color = Color(0xFF64748B))
                        Row {
                            tiers.forEach { t ->
                                FilterChip(
                                    selected = tier == t,
                                    onClick = { tier = t },
                                    label = { Text(t.replace(" Mitra", ""), fontSize = 10.sp) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        val exp = experienceYearsText.toIntOrNull() ?: 5
                        onSave(name, phone, location.ifBlank { "Rajsamand" }, specialty, exp, tier)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IconCarpenterCyan),
                modifier = Modifier.testTag("btn_confirm_register_carpenter")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Register & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 2. Assign Project to Carpenter Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignProjectDialog(
    carpenters: List<Carpenter>,
    preselectedCarpenter: Carpenter?,
    onDismiss: () -> Unit,
    onAssign: (
        carpenterId: Long,
        clientName: String,
        clientPhone: String,
        siteAddress: String,
        workType: String,
        laborCharge: Double,
        advancePaid: Double,
        targetDate: String,
        notes: String
    ) -> Unit
) {
    var selectedCarpenterId by remember { mutableLongStateOf(preselectedCarpenter?.id ?: (carpenters.firstOrNull()?.id ?: 1L)) }
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var siteAddress by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("L-Shape PVC Kitchen Installation (12x9 ft)") }
    var laborChargeText by remember { mutableStateOf("15000") }
    var advancePaidText by remember { mutableStateOf("5000") }
    var targetDate by remember { mutableStateOf("2026-10-25") }
    var notes by remember { mutableStateOf("Handle with care: Acrylic high-gloss finish shutters") }

    var carpenterDropdownExpanded by remember { mutableStateOf(false) }

    val commonWorkTypes = listOf(
        "L-Shape PVC Kitchen Installation (12x9 ft)",
        "Straight PVC Modular Kitchen",
        "Steel Almirah Wardrobe & Mirror Fitting",
        "4 Waterproof PVC Bathroom Doors with Frames",
        "PVC Bedroom Wardrobe with Loft Cladding",
        "PVC Fluted Wall Panel Cladding"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color(0xFF0284C7))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assign Site Project", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Assign a furniture installation or fabrication project to a carpenter.", fontSize = 11.sp, color = Color(0xFF64748B))

                // Carpenter Selector
                ExposedDropdownMenuBox(
                    expanded = carpenterDropdownExpanded,
                    onExpandedChange = { carpenterDropdownExpanded = !carpenterDropdownExpanded }
                ) {
                    val currentCarp = carpenters.find { it.id == selectedCarpenterId }
                    OutlinedTextField(
                        value = currentCarp?.let { "${it.name} (${it.location} • ${it.tier})" } ?: "Select Carpenter",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to Carpenter *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carpenterDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = carpenterDropdownExpanded,
                        onDismissRequest = { carpenterDropdownExpanded = false }
                    ) {
                        carpenters.forEach { carp ->
                            DropdownMenuItem(
                                text = { Text("${carp.name} - ${carp.specialty} (${carp.tier})", fontSize = 12.sp) },
                                onClick = {
                                    selectedCarpenterId = carp.id
                                    carpenterDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Client Details
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client / Site Owner Name *") },
                    placeholder = { Text("e.g. Mukesh Sharma") },
                    modifier = Modifier.fillMaxWidth().testTag("input_project_client_name")
                )

                OutlinedTextField(
                    value = clientPhone,
                    onValueChange = { clientPhone = it },
                    label = { Text("Client Mobile Number") },
                    placeholder = { Text("+91 94140 12345") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = siteAddress,
                    onValueChange = { siteAddress = it },
                    label = { Text("Site Address / Location *") },
                    placeholder = { Text("e.g. Plot 15, Near Krishna Mandir, Rajsamand") },
                    modifier = Modifier.fillMaxWidth().testTag("input_project_site_address")
                )

                OutlinedTextField(
                    value = workType,
                    onValueChange = { workType = it },
                    label = { Text("Work Scope / Furniture Description *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_project_work_type")
                )

                // Quick Work Type chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(commonWorkTypes) { preset ->
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { workType = preset }
                        ) {
                            Text(preset, fontSize = 9.sp, color = Color(0xFF0369A1), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = laborChargeText,
                        onValueChange = { laborChargeText = it },
                        label = { Text("Labor Charge (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = advancePaidText,
                        onValueChange = { advancePaidText = it },
                        label = { Text("Advance Paid (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Completion Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Site Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientName.isNotBlank() && workType.isNotBlank()) {
                        val labor = laborChargeText.toDoubleOrNull() ?: 0.0
                        val advance = advancePaidText.toDoubleOrNull() ?: 0.0
                        onAssign(
                            selectedCarpenterId,
                            clientName,
                            clientPhone,
                            siteAddress.ifBlank { "Showroom site" },
                            workType,
                            labor,
                            advance,
                            targetDate.ifBlank { "2026-10-30" },
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("btn_confirm_assign_project")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Assign & Save to Cloud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 3. Update Project Status & Payment Dialog
@Composable
fun UpdateProjectStatusDialog(
    assignment: CarpenterAssignment,
    carpenterName: String,
    onDismiss: () -> Unit,
    onConfirm: (newStatus: String, additionalPayment: Double) -> Unit
) {
    var status by remember { mutableStateOf(assignment.status) }
    var additionalPaymentText by remember { mutableStateOf("0") }
    val statuses = listOf("Assigned", "In Progress", "Inspection Pending", "Completed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Project Assignment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${assignment.workType} for ${assignment.clientName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                Text("Carpenter: $carpenterName", fontSize = 12.sp, color = Color(0xFF00838F))

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Current Labor: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.laborCharge)}", fontSize = 11.sp)
                        Text("Already Paid: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.advancePaid)}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        Text(
                            "Remaining Balance: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", (assignment.laborCharge - assignment.advancePaid).coerceAtLeast(0.0))}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Text("Select Stage Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                statuses.forEach { st ->
                    Surface(
                        color = if (status == st) Color(0xFFE0F7FA) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { status = st }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = status == st,
                                onClick = { status = st }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(st, fontSize = 13.sp, fontWeight = if (status == st) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                OutlinedTextField(
                    value = additionalPaymentText,
                    onValueChange = { additionalPaymentText = it },
                    label = { Text("Add Labor Payment / Final Settlement (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addPay = additionalPaymentText.toDoubleOrNull() ?: 0.0
                    onConfirm(status, addPay)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IconCarpenterCyan)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 4. Carpenter Details & Assigned Projects Dialog
@Composable
fun CarpenterDetailDialog(
    carpenter: Carpenter,
    assignments: List<CarpenterAssignment>,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onAssignProject: () -> Unit,
    onSyncCloud: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(carpenter.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(carpenter.tier, fontSize = 11.sp, color = Color(0xFF00838F), fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = onSyncCloud) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync Carpenter", tint = Color(0xFF00897B))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Contact: ${carpenter.phone}", fontSize = 12.sp)
                Text("Workshop Location: ${carpenter.location}", fontSize = 12.sp)
                Text("Specialty: ${carpenter.specialty}", fontSize = 12.sp)
                Text("Experience: ${carpenter.experienceYears} Years", fontSize = 12.sp)
                Text("Member Since: ${carpenter.joinDate}", fontSize = 11.sp, color = Color(0xFF64748B))

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Assigned Projects (${assignments.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                if (assignments.isEmpty()) {
                    Text("No active or completed projects assigned yet.", fontSize = 11.sp, color = Color(0xFF64748B))
                } else {
                    assignments.take(5).forEach { a ->
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(a.workType, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(a.status, fontSize = 10.sp, color = Color(0xFF0284C7))
                                }
                                Text("Client: ${a.clientName} • ${a.siteAddress}", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("Labor: ₹${a.laborCharge.toInt()} | Paid: ₹${a.advancePaid.toInt()}", fontSize = 10.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAssignProject,
                colors = ButtonDefaults.buttonColors(containerColor = IconCarpenterCyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assign Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// 5. Add Referral Dialog
@Composable
fun AddReferralDialog(
    carpenter: Carpenter,
    onDismiss: () -> Unit,
    onConfirm: (commission: Double, points: Int) -> Unit
) {
    var siteName by remember { mutableStateOf("") }
    var orderValueText by remember { mutableStateOf("100000") }
    val orderVal = orderValueText.toDoubleOrNull() ?: 0.0
    val commission = orderVal * 0.03 // 3% referral commission
    val points = (orderVal / 100).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Referral Site Commission") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Carpenter Partner: ${carpenter.name} (${carpenter.location})")
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text("Client / Site Name") },
                    placeholder = { Text("e.g. Purohit Residence Kitchen") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderValueText,
                    onValueChange = { orderValueText = it },
                    label = { Text("Order Value (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Surface(
                    color = Color(0xFFE0F7FA),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Calculated Commission (3%): ₹${String.format(java.util.Locale.getDefault(), "%,.0f", commission)}", fontWeight = FontWeight.Bold, color = Color(0xFF006064))
                        Text("Reward Points: +$points pts (10 pts per ₹1,000)", fontSize = 11.sp, color = Color(0xFF00838F))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(commission, points) },
                colors = ButtonDefaults.buttonColors(containerColor = IconCarpenterCyan)
            ) {
                Text("Credit Commission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 6. Payout Dialog
@Composable
fun PayoutDialog(
    carpenter: Carpenter,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("${carpenter.pendingPayout.toInt()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payout Carpenter Commission") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pay to: ${carpenter.name} (${carpenter.phone})")
                Text("Pending Balance: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", carpenter.pendingPayout)}", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Confirm Payout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Helper: Open WhatsApp with prefilled message
fun openWhatsAppChat(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (!cleanPhone.startsWith("91") && cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Share via"))
    }
}

// Helper: Share project slip on WhatsApp
fun shareProjectSlipOnWhatsApp(context: Context, assignment: CarpenterAssignment, carpenterName: String) {
    val message = """
        *ARAVALLI STEEL & PVC FURNITURE*
        _Carpenter Club - Project Work Assignment_
        ------------------------------------
        *Assigned To:* $carpenterName
        *Client Name:* ${assignment.clientName}
        *Client Phone:* ${assignment.clientPhone}
        *Site Location:* ${assignment.siteAddress}
        
        *Work Scope:*
        ${assignment.workType}
        
        *Contract Financials:*
        • Labor Charge: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.laborCharge)}
        • Advance Paid: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", assignment.advancePaid)}
        • Balance Due: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", (assignment.laborCharge - assignment.advancePaid).coerceAtLeast(0.0))}
        
        *Schedule:*
        Target Completion: ${assignment.targetCompletionDate}
        Current Status: ${assignment.status}
        
        *Instructions:* ${assignment.notes}
        ------------------------------------
        Aravalli Steel & PVC Showroom
        Highway Junction, Rajsamand / Udaipur
        Contact: +91 98290 12345
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share Project Work Slip via"))
}
