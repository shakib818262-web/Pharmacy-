package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Medicine
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.StatusAlertBg
import com.example.ui.theme.StatusAlertText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils

enum class InventoryFilter(val label: String) {
    ALL("সব ওষুধ"),
    LOW_STOCK("কম স্টক"),
    EXPIRED("মেয়াদোত্তীর্ণ"),
    STOCK_OUT("স্টক শেষ")
}

@Composable
fun InventoryScreen(viewModel: PharmacyViewModel) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val useBengali = activePharmacy.useBengaliDigits

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(InventoryFilter.ALL) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<Medicine?>(null) }
    var adjustingStockMedicine by remember { mutableStateOf<Medicine?>(null) }

    // Filter list
    val filteredList = remember(medicines, searchQuery, selectedFilter) {
        medicines.filter { med ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase().trim()
                med.name.lowercase().contains(q) ||
                med.genericName.lowercase().contains(q) ||
                med.company.lowercase().contains(q) ||
                med.batchNo.lowercase().contains(q) ||
                med.rackLocation.lowercase().contains(q) ||
                med.barcode.contains(q)
            }
            val matchesFilter = when (selectedFilter) {
                InventoryFilter.ALL -> true
                InventoryFilter.LOW_STOCK -> med.isLowStock()
                InventoryFilter.EXPIRED -> med.isExpired()
                InventoryFilter.STOCK_OUT -> med.isStockOut()
            }
            matchesSearch && matchesFilter
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar with Search & Order list button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্টক ও ব্যাচ ইনভেন্টরি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { viewModel.setShowOrderListDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("অর্ডার লিস্ট তৈরি", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ওষুধ, জেনেরিক, কোম্পানি বা ব্যাচ খুঁজুন...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InventoryFilter.values().forEach { filter ->
                            val count = when (filter) {
                                InventoryFilter.ALL -> medicines.size
                                InventoryFilter.LOW_STOCK -> medicines.count { it.isLowStock() }
                                InventoryFilter.EXPIRED -> medicines.count { it.isExpired() }
                                InventoryFilter.STOCK_OUT -> medicines.count { it.isStockOut() }
                            }
                            val countStr = if (useBengali) BengaliUtils.toBengaliDigits(count) else count.toString()

                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text("${filter.label} ($countStr)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (filter == InventoryFilter.EXPIRED) Color(0xFFD32F2F) else TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Medicine Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো ওষুধ পাওয়া যায়নি। নতুন ওষুধ যোগ করতে নিচের '+' চাপুন।",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { med ->
                        InventoryMedicineCard(
                            medicine = med,
                            useBengali = useBengali,
                            onEdit = {
                                editingMedicine = med
                                showAddEditDialog = true
                            },
                            onAdjustStock = {
                                adjustingStockMedicine = med
                            },
                            onDelete = {
                                viewModel.deleteMedicine(med.id)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Action Button to add new medicine
        FloatingActionButton(
            onClick = {
                editingMedicine = null
                showAddEditDialog = true
            },
            containerColor = TealPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_medicine_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "নতুন ওষুধ যোগ")
        }
    }

    // Add or Edit Medicine Dialog
    if (showAddEditDialog) {
        AddEditMedicineDialog(
            initialMedicine = editingMedicine,
            onSave = { med ->
                viewModel.saveMedicine(med)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    // Quick Stock Adjustment Dialog
    if (adjustingStockMedicine != null) {
        StockAdjustmentDialog(
            medicine = adjustingStockMedicine!!,
            useBengali = useBengali,
            onAdjust = { newPieces ->
                viewModel.adjustStock(adjustingStockMedicine!!.id, newPieces)
                adjustingStockMedicine = null
            },
            onDismiss = { adjustingStockMedicine = null }
        )
    }
}

@Composable
fun InventoryMedicineCard(
    medicine: Medicine,
    useBengali: Boolean,
    onEdit: () -> Unit,
    onAdjustStock: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpired = medicine.isExpired()
    val isStockOut = medicine.isStockOut()
    val isLowStock = medicine.isLowStock()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExpired -> StatusAlertBg
                isStockOut -> Color(0xFFFBFBFB)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isExpired) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TealPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = medicine.type,
                            color = TealPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "এডিট", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = "${medicine.genericName} (${medicine.company})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Pricing & Batch Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MRP: ${BengaliUtils.formatMoney(medicine.mrpPerPiece, useBengali)}/পিস (পাতা ${BengaliUtils.formatMoney(medicine.mrpPerStrip, useBengali)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                    Text(
                        text = "ক্রয়মূল্য: ${BengaliUtils.formatMoney(medicine.purchasePricePerPiece, useBengali)}/পিস (পাতা ${BengaliUtils.formatMoney(medicine.purchasePricePerStrip, useBengali)})",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ব্যাচ: ${medicine.batchNo}",
                        fontSize = 11.sp,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = Color.DarkGray
                    )
                    if (medicine.rackLocation.isNotBlank()) {
                        Text(
                            text = "র‌্যাক: ${medicine.rackLocation}",
                            fontSize = 11.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Stock and Expiry Row with Stock Adjustment button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock details
                Column {
                    val strips = if (medicine.piecesPerStrip > 0) medicine.currentStockPieces / medicine.piecesPerStrip else 0
                    val loosePieces = if (medicine.piecesPerStrip > 0) medicine.currentStockPieces % medicine.piecesPerStrip else 0

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "বর্তমান স্টক: ",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${BengaliUtils.formatNumber(medicine.currentStockPieces, useBengali)} পিস (${BengaliUtils.formatNumber(strips, useBengali)} পাতা ${if (loosePieces > 0) "+ " + BengaliUtils.formatNumber(loosePieces, useBengali) + " পিস" else ""})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = if (isExpired || isStockOut) Color(0xFFD32F2F) else if (isLowStock) StatusWarningText else Color(0xFF2E7D32)
                        )
                    }

                    // Expiry status
                    Text(
                        text = when {
                            isExpired -> "⚠️ মেয়াদ উত্তীর্ণ হয়ে গেছে! (${medicine.expiryDate})"
                            medicine.isNearExpiry() -> "⚠️ শীঘ্রই মেয়াদ উত্তীর্ণ হবে (${medicine.expiryDate})"
                            else -> "মেয়াদ: ${medicine.expiryDate}"
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal,
                        color = if (isExpired) Color(0xFFD32F2F) else if (medicine.isNearExpiry()) Color(0xFFE65100) else Color.Gray,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                }

                // Quick stock adjust button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealPrimary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onAdjustStock() }
                ) {
                    Text(
                        text = "স্টক সংশোধন",
                        color = TealPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineDialog(
    initialMedicine: Medicine?,
    onSave: (Medicine) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialMedicine?.name ?: "") }
    var genericName by remember { mutableStateOf(initialMedicine?.genericName ?: "") }
    var company by remember { mutableStateOf(initialMedicine?.company ?: "") }
    var type by remember { mutableStateOf(initialMedicine?.type ?: "ট্যাবলেট") }
    var piecesPerStrip by remember { mutableStateOf((initialMedicine?.piecesPerStrip ?: 10).toString()) }
    var mrpPerStrip by remember { mutableStateOf((initialMedicine?.mrpPerStrip ?: 30.0).toString()) }
    var purchasePricePerStrip by remember { mutableStateOf((initialMedicine?.purchasePricePerStrip ?: 24.0).toString()) }
    var currentStockPieces by remember { mutableStateOf((initialMedicine?.currentStockPieces ?: 100).toString()) }
    var minStockAlert by remember { mutableStateOf((initialMedicine?.minStockAlert ?: 20).toString()) }
    var batchNo by remember { mutableStateOf(initialMedicine?.batchNo ?: "B-101") }
    var expiryDate by remember { mutableStateOf(initialMedicine?.expiryDate ?: "12/27") }
    var rackLocation by remember { mutableStateOf(initialMedicine?.rackLocation ?: "A-1") }
    var barcode by remember { mutableStateOf(initialMedicine?.barcode ?: "") }

    val medicineTypes = listOf("ট্যাবলেট", "ক্যাপসুল", "সিরাপ", "ইনজেকশন", "আই ড্রপ", "মলম", "স্যালাইন", "ইনহেলার")
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = if (initialMedicine == null) "নতুন ওষুধ যোগ করুন" else "ওষুধ তথ্য সংশোধন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ওষুধের ব্র্যান্ড নাম (যেমন: Napa Extra)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = genericName,
                    onValueChange = { genericName = it },
                    label = { Text("জেনেরিক নাম (যেমন: Paracetamol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("কোম্পানি / প্রস্তুতকারক (যেমন: Beximco, Square)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Type Dropdown & Pieces per strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ধরন") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            medicineTypes.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        type = t
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = piecesPerStrip,
                        onValueChange = { piecesPerStrip = it },
                        label = { Text("পাতায় পিস") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price Row (MRP strip & Purchase strip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePricePerStrip,
                        onValueChange = { purchasePricePerStrip = it },
                        label = { Text("পাতা ক্রয়মূল্য (৳)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = mrpPerStrip,
                        onValueChange = { mrpPerStrip = it },
                        label = { Text("পাতা MRP (৳)*") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Batch & Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchNo,
                        onValueChange = { batchNo = it },
                        label = { Text("ব্যাচ নম্বর") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("মেয়াদ (MM/YY)*") },
                        placeholder = { Text("12/27") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock & Alert
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentStockPieces,
                        onValueChange = { currentStockPieces = it },
                        label = { Text("বর্তমান স্টক (পিস)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = minStockAlert,
                        onValueChange = { minStockAlert = it },
                        label = { Text("সতর্কতা সীমা") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rack location & Barcode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = rackLocation,
                        onValueChange = { rackLocation = it },
                        label = { Text("র‌্যাক / সেলফ") },
                        placeholder = { Text("A-1") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("বারকোড") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) return@Button
                            val pPerStrip = piecesPerStrip.toIntOrNull() ?: 10
                            val purStrip = purchasePricePerStrip.toDoubleOrNull() ?: 0.0
                            val mStrip = mrpPerStrip.toDoubleOrNull() ?: 0.0
                            val purPiece = if (pPerStrip > 0) purStrip / pPerStrip else 0.0
                            val mPiece = if (pPerStrip > 0) mStrip / pPerStrip else 0.0
                            val stock = currentStockPieces.toIntOrNull() ?: 0
                            val alert = minStockAlert.toIntOrNull() ?: 10

                            val updated = (initialMedicine ?: Medicine(name = name)).copy(
                                name = name.trim(),
                                genericName = genericName.trim(),
                                company = company.trim(),
                                type = type,
                                piecesPerStrip = pPerStrip,
                                purchasePricePerStrip = purStrip,
                                mrpPerStrip = mStrip,
                                purchasePricePerPiece = purPiece,
                                mrpPerPiece = mPiece,
                                batchNo = batchNo.trim(),
                                expiryDate = expiryDate.trim(),
                                currentStockPieces = stock,
                                minStockAlert = alert,
                                rackLocation = rackLocation.trim(),
                                barcode = barcode.trim()
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("সংরক্ষণ করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    medicine: Medicine,
    useBengali: Boolean,
    onAdjust: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var stockInput by remember { mutableStateOf(medicine.currentStockPieces.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "স্টক সংখ্যা সংশোধন করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${medicine.name} (বর্তমান: ${BengaliUtils.formatNumber(medicine.currentStockPieces, useBengali)} পিস)",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = stockInput,
                    onValueChange = { stockInput = it },
                    label = { Text("নতুন মোট স্টক (পিস)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("বাতিল")
                    }

                    Button(
                        onClick = {
                            val newStock = stockInput.toIntOrNull() ?: medicine.currentStockPieces
                            onAdjust(maxOf(0, newStock))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("আপডেট করুন")
                    }
                }
            }
        }
    }
}
