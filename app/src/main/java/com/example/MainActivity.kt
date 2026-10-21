package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.PharmacyViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.KhataLedgerScreen
import com.example.ui.screens.OrderListDialog
import com.example.ui.screens.PosScreen
import com.example.ui.screens.PurchaseScreen
import com.example.ui.screens.ReceiptDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.BengaliUtils
import kotlinx.coroutines.flow.collectLatest

import androidx.compose.runtime.DisposableEffect
import com.example.ui.screens.AuthScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: PharmacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val auth = Firebase.auth
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { fAuth ->
                        currentUser = fAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                if (currentUser == null) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = Firebase.auth.currentUser
                        }
                    )
                } else {
                    PharmacyKhataApp(
                        viewModel = viewModel,
                        userEmail = currentUser?.email ?: ""
                    )
                }
            }
        }
    }
}

data class NavItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyKhataApp(
    viewModel: PharmacyViewModel,
    userEmail: String = ""
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val pharmacies by viewModel.pharmacies.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val lastCompletedSale by viewModel.lastCompletedSale.collectAsState()
    val showReceiptDialog by viewModel.showReceiptDialog.collectAsState()
    val showOrderListDialog by viewModel.showOrderListDialog.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var pharmacyBranchMenuExpanded by remember { mutableStateOf(false) }

    // Collect alert notifications
    LaunchedEffect(Unit) {
        viewModel.alertMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(message = msg)
        }
    }

    // Android Hardware / Gesture Back button handling
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val navItems = listOf(
        NavItem(AppScreen.DASHBOARD, Icons.Default.Dashboard, "nav_dashboard"),
        NavItem(AppScreen.POS, Icons.Default.PointOfSale, "nav_pos"),
        NavItem(AppScreen.INVENTORY, Icons.Default.Inventory2, "nav_inventory"),
        NavItem(AppScreen.PURCHASE, Icons.Default.LocalShipping, "nav_purchase"),
        NavItem(AppScreen.KHATA_LEDGER, Icons.Default.Book, "nav_khata"),
        NavItem(AppScreen.REPORTS, Icons.Default.BarChart, "nav_reports"),
        NavItem(AppScreen.SETTINGS, Icons.Default.Settings, "nav_settings")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ফার্মেসি খাতা",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = activePharmacy.name,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    actions = {
                        // Quick Pharmacy Branch Switcher
                        Box {
                            IconButton(onClick = { pharmacyBranchMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "ফার্মেসি শাখা পরিবর্তন",
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = pharmacyBranchMenuExpanded,
                                onDismissRequest = { pharmacyBranchMenuExpanded = false }
                            ) {
                                pharmacies.forEach { p ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (p.id == activePharmacy.id) {
                                                    Text("✓ ", color = TealPrimary, fontWeight = FontWeight.Bold)
                                                }
                                                Text(p.name, fontWeight = if (p.id == activePharmacy.id) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        },
                                        onClick = {
                                            viewModel.switchPharmacy(p.id)
                                            pharmacyBranchMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TealPrimary
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentScreen == item.screen
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    if (item.screen == AppScreen.POS && cartItems.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Color(0xFFD32F2F)) {
                                                    Text(BengaliUtils.formatNumber(cartItems.size, activePharmacy.useBengaliDigits))
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = item.icon, contentDescription = item.screen.banglaTitle)
                                        }
                                    } else {
                                        Icon(imageVector = item.icon, contentDescription = item.screen.banglaTitle)
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.screen.banglaTitle,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = TealPrimary,
                                    selectedTextColor = TealPrimary,
                                    indicatorColor = TealPrimary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Side Navigation Rail (Desktop / Tablet view)
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        navItems.forEach { item ->
                            val selected = currentScreen == item.screen
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    if (item.screen == AppScreen.POS && cartItems.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = Color(0xFFD32F2F)) {
                                                    Text(BengaliUtils.formatNumber(cartItems.size, activePharmacy.useBengaliDigits))
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = item.icon, contentDescription = item.screen.banglaTitle)
                                        }
                                    } else {
                                        Icon(imageVector = item.icon, contentDescription = item.screen.banglaTitle)
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.screen.banglaTitle,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = TealPrimary,
                                    selectedTextColor = TealPrimary,
                                    indicatorColor = TealPrimary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                // Main Screen Body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (currentScreen) {
                        AppScreen.DASHBOARD -> DashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                        AppScreen.POS -> PosScreen(viewModel = viewModel)
                        AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                        AppScreen.PURCHASE -> PurchaseScreen(viewModel = viewModel)
                        AppScreen.KHATA_LEDGER -> KhataLedgerScreen(viewModel = viewModel)
                        AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                        AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Receipt Dialog Popup (Thermal POS Slip)
    if (showReceiptDialog && lastCompletedSale != null) {
        ReceiptDialog(
            pharmacy = activePharmacy,
            sale = lastCompletedSale!!,
            onDismiss = { viewModel.dismissReceiptDialog() }
        )
    }

    // Emergency Order List Dialog
    if (showOrderListDialog) {
        OrderListDialog(
            pharmacy = activePharmacy,
            medicines = medicines,
            onDismiss = { viewModel.setShowOrderListDialog(false) }
        )
    }
}
