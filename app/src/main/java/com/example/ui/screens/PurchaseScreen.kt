package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Medicine
import com.example.data.model.PurchaseRecord
import com.example.data.model.Supplier
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils

@Composable
fun PurchaseScreen(viewModel: PharmacyViewModel) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val useBengali = activePharmacy.useBengaliDigits

    var showAddPurchaseDialog by remember { mutableStateOf(false) }

    val totalPurchased = purchases.sumOf { it.totalAmount }
    val totalPaid = purchases.sumOf { it.paidAmount }
    val totalDue = purchases.sumOf { it.dueAmount }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Summary Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ক্রয় খাতা (Purchase / Supplier Bills)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "নতুন চালানের ওষুধ স্টক সরাসরি যুক্ত হয়",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMiniCard(
                            title = "মোট ক্রয়",
                            value = BengaliUtils.formatMoney(totalPurchased, useBengali),
                            color = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            title = "পরিশোধ",
                            value = BengaliUtils.formatMoney(totalPaid, useBengali),
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            title = "মহাজন বাকি",
                            value = BengaliUtils.formatMoney(totalDue, useBengali),
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Purchase Records List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "ক্রয় চালানের তালিকা (${BengaliUtils.formatNumber(purchases.size, useBengali)})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                if (purchases.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                    text = "এখনো কোনো ক্রয়ের চালান রেকর্ড করা হয়নি।",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(purchases, key = { it.id }) { pur ->
                        PurchaseItemCard(purchase = pur, useBengali = useBengali)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // FAB to add purchase
        FloatingActionButton(
            onClick = { showAddPurchaseDialog = true },
            containerColor = TealPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_purchase_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "নতুন ক্রয় চালান")
        }
    }

    if (showAddPurchaseDialog) {
        AddPurchaseDialog(
            suppliers = suppliers,
            medicines = medicines,
            onSave = { supp, inv, med, medCustom, qty, total, paid, batch, exp ->
                viewModel.recordPurchase(supp, inv, med, medCustom, qty, total, paid, batch, exp)
                showAddPurchaseDialog = false
            },
            onAddSupplier = { name, company, phone ->
                viewModel.saveSupplier(Supplier(name = name, companyName = company, phone = phone))
            },
            onDismiss = { showAddPurchaseDialog = false }
        )
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IbmPlexMonoFontFamily,
                color = color
            )
        }
    }
}

@Composable
fun PurchaseItemCard(purchase: PurchaseRecord, useBengali: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0F2F1),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = purchase.supplierName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "চালান #${purchase.invoiceNo} • ${BengaliUtils.formatDate(purchase.date, useBengali)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontFamily = IbmPlexMonoFontFamily
                        )
                    }
                }

                Text(
                    text = BengaliUtils.formatMoney(purchase.totalAmount, useBengali),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = TealPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Medicine & Stock increment details
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF7FAF9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ওষুধ: ${purchase.medicineName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "ব্যাচ: ${purchase.batchNo} • মেয়াদ: ${purchase.expiryDate}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "+${BengaliUtils.formatNumber(purchase.quantityPieces, useBengali)} পিস স্টক",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Payment info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "পরিশোধ: ${BengaliUtils.formatMoney(purchase.paidAmount, useBengali)}",
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )
                if (purchase.dueAmount > 0) {
                    Text(
                        text = "বাকি: ${BengaliUtils.formatMoney(purchase.dueAmount, useBengali)}",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseDialog(
    suppliers: List<Supplier>,
    medicines: List<Medicine>,
    onSave: (Supplier, String, Medicine?, String, Int, Double, Double, String, String) -> Unit,
    onAddSupplier: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var invoiceNo by remember { mutableStateOf("INV-${System.currentTimeMillis() % 100000}") }
    var selectedMedicine by remember { mutableStateOf<Medicine?>(null) }
    var customMedicineName by remember { mutableStateOf("") }
    var quantityPieces by remember { mutableStateOf("100") }
    var totalAmount by remember { mutableStateOf("2000") }
    var paidAmount by remember { mutableStateOf("2000") }
    var batchNo by remember { mutableStateOf("B-${(100..999).random()}") }
    var expiryDate by remember { mutableStateOf("12/28") }

    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var medicineDropdownExpanded by remember { mutableStateOf(false) }

    // Quick add supplier dialog flag
    var showQuickAddSupplier by remember { mutableStateOf(false) }
    var newSuppName by remember { mutableStateOf("") }
    var newSuppCompany by remember { mutableStateOf("") }
    var newSuppPhone by remember { mutableStateOf("") }

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
                    text = "নতুন ক্রয় চালান এন্ট্রি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "ক্রয় সংরক্ষণ করলে ওষুধের স্টক স্বয়ংক্রিয়ভাবে বাড়বে",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Supplier Selector with Quick Add button
                Text("সাপ্লায়ার নির্বাচন করুন:*", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = supplierDropdownExpanded,
                        onExpandedChange = { supplierDropdownExpanded = !supplierDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedSupplier?.name ?: "সাপ্লায়ার সিলেক্ট করুন",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierDropdownExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = supplierDropdownExpanded,
                            onDismissRequest = { supplierDropdownExpanded = false }
                        ) {
                            suppliers.forEach { supp ->
                                DropdownMenuItem(
                                    text = { Text("${supp.name} (${supp.companyName})") },
                                    onClick = {
                                        selectedSupplier = supp
                                        supplierDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedButton(onClick = { showQuickAddSupplier = true }) {
                        Text("+ নতুন")
                    }
                }

                if (showQuickAddSupplier) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("নতুন সাপ্লায়ার যোগ করুন:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            OutlinedTextField(
                                value = newSuppName,
                                onValueChange = { newSuppName = it },
                                label = { Text("সাপ্লায়ার নাম") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newSuppCompany,
                                onValueChange = { newSuppCompany = it },
                                label = { Text("কোম্পানি / ডিপো") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = newSuppPhone,
                                onValueChange = { newSuppPhone = it },
                                label = { Text("মোবাইল") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (newSuppName.isNotBlank()) {
                                        onAddSupplier(newSuppName, newSuppCompany, newSuppPhone)
                                        showQuickAddSupplier = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Text("যুক্ত করুন")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = invoiceNo,
                    onValueChange = { invoiceNo = it },
                    label = { Text("চালান / ইনভয়েস নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select Existing Medicine to auto-increment stock
                Text("কোন ওষুধের স্টক বৃদ্ধি পাবে?*", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                ExposedDropdownMenuBox(
                    expanded = medicineDropdownExpanded,
                    onExpandedChange = { medicineDropdownExpanded = !medicineDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedMedicine?.name ?: if (customMedicineName.isNotBlank()) customMedicineName else "ওষুধ নির্বাচন করুন",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = medicineDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = medicineDropdownExpanded,
                        onDismissRequest = { medicineDropdownExpanded = false }
                    ) {
                        medicines.forEach { med ->
                            DropdownMenuItem(
                                text = { Text("${med.name} (বর্তমান স্টক: ${med.currentStockPieces} পিস)") },
                                onClick = {
                                    selectedMedicine = med
                                    customMedicineName = med.name
                                    medicineDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity and Total Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityPieces,
                        onValueChange = { quantityPieces = it },
                        label = { Text("ক্রয় পরিমাণ (পিস)*") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("মোট ক্রয় বিল (৳)*") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Paid amount
                OutlinedTextField(
                    value = paidAmount,
                    onValueChange = { paidAmount = it },
                    label = { Text("পরিশোধিত টাকা (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Batch and Expiry
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
                        label = { Text("মেয়াদ (MM/YY)") },
                        placeholder = { Text("12/28") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

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
                            val supp = selectedSupplier ?: Supplier(name = "সাধারণ সাপ্লায়ার")
                            val qty = quantityPieces.toIntOrNull() ?: 0
                            val total = totalAmount.toDoubleOrNull() ?: 0.0
                            val paid = paidAmount.toDoubleOrNull() ?: total

                            onSave(
                                supp,
                                invoiceNo.trim(),
                                selectedMedicine,
                                customMedicineName.ifBlank { "ওষুধ চালান" },
                                qty,
                                total,
                                paid,
                                batchNo.trim(),
                                expiryDate.trim()
                            )
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
