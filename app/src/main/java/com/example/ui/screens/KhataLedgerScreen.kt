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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Customer
import com.example.data.model.LedgerEntry
import com.example.data.model.Supplier
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils

@Composable
fun KhataLedgerScreen(viewModel: PharmacyViewModel) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val ledgerEntries by viewModel.ledgerEntries.collectAsState()
    val useBengali = activePharmacy.useBengaliDigits

    var selectedTab by remember { mutableStateOf(0) } // 0: Customers, 1: Suppliers, 2: All History
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }

    var collectingDueCustomer by remember { mutableStateOf<Customer?>(null) }
    var payingSupplier by remember { mutableStateOf<Supplier?>(null) }

    val totalCustomerDue = customers.sumOf { it.totalDue }
    val totalSupplierPayable = suppliers.sumOf { it.totalPayable }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "বাকির খাতা ও লেজার (Khata Ledger)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedTab == 0) "মোট কাস্টমার বাকি: ${BengaliUtils.formatMoney(totalCustomerDue, useBengali)}"
                                       else if (selectedTab == 1) "মোট মহাজন বাকি: ${BengaliUtils.formatMoney(totalSupplierPayable, useBengali)}"
                                       else "সর্বশেষ লেনদেন ইতিহাস",
                                fontSize = 12.sp,
                                color = if (selectedTab == 0) Color(0xFFD32F2F) else if (selectedTab == 1) Color(0xFFE65100) else Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = IbmPlexMonoFontFamily
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TealPrimary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("কাস্টমার বাকি (${BengaliUtils.formatNumber(customers.count { it.totalDue > 0 }, useBengali)})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("মহাজন বাকি (${BengaliUtils.formatNumber(suppliers.count { it.totalPayable > 0 }, useBengali)})", fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("লেজার ইতিহাস", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Customers List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (customers.isEmpty()) {
                            item {
                                EmptyKhataCard(text = "কোনো কাস্টমার পাওয়া যায়নি। নতুন কাস্টমার যোগ করতে নিচের '+' চাপুন।")
                            }
                        } else {
                            // Sort by due desc
                            val sorted = customers.sortedByDescending { it.totalDue }
                            items(sorted, key = { it.id }) { cust ->
                                CustomerKhataCard(
                                    customer = cust,
                                    pharmacyName = activePharmacy.name,
                                    useBengali = useBengali,
                                    onCollectDue = { collectingDueCustomer = cust }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                1 -> {
                    // Suppliers List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (suppliers.isEmpty()) {
                            item {
                                EmptyKhataCard(text = "কোনো মহাজন/সাপ্লায়ার পাওয়া যায়নি।")
                            }
                        } else {
                            val sorted = suppliers.sortedByDescending { it.totalPayable }
                            items(sorted, key = { it.id }) { supp ->
                                SupplierKhataCard(
                                    supplier = supp,
                                    useBengali = useBengali,
                                    onPayDue = { payingSupplier = supp }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
                2 -> {
                    // Ledger History
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (ledgerEntries.isEmpty()) {
                            item {
                                EmptyKhataCard(text = "এখনো কোনো লেজার লেনদেন রেকর্ড হয়নি।")
                            }
                        } else {
                            items(ledgerEntries, key = { it.id }) { entry ->
                                LedgerItemRow(entry = entry, useBengali = useBengali)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // FAB to add Customer or Supplier
        if (selectedTab == 0 || selectedTab == 1) {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddCustomerDialog = true else showAddSupplierDialog = true
                },
                containerColor = TealPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_party_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "যোগ করুন")
            }
        }
    }

    // Dialogs
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onSave = { name, phone, address ->
                viewModel.saveCustomer(Customer(name = name, phone = phone, address = address))
                showAddCustomerDialog = false
            },
            onDismiss = { showAddCustomerDialog = false }
        )
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onSave = { name, company, phone, address ->
                viewModel.saveSupplier(Supplier(name = name, companyName = company, phone = phone, address = address))
                showAddSupplierDialog = false
            },
            onDismiss = { showAddSupplierDialog = false }
        )
    }

    if (collectingDueCustomer != null) {
        CollectDueDialog(
            customer = collectingDueCustomer!!,
            useBengali = useBengali,
            onCollect = { amt, note ->
                viewModel.collectCustomerDue(collectingDueCustomer!!.id, amt, note)
                collectingDueCustomer = null
            },
            onDismiss = { collectingDueCustomer = null }
        )
    }

    if (payingSupplier != null) {
        PaySupplierDialog(
            supplier = payingSupplier!!,
            useBengali = useBengali,
            onPay = { amt, note ->
                viewModel.paySupplierDue(payingSupplier!!.id, amt, note)
                payingSupplier = null
            },
            onDismiss = { payingSupplier = null }
        )
    }
}

@Composable
fun CustomerKhataCard(
    customer: Customer,
    pharmacyName: String,
    useBengali: Boolean,
    onCollectDue: () -> Unit
) {
    val context = LocalContext.current
    val dueAmountStr = BengaliUtils.formatMoney(customer.totalDue, useBengali)

    // Pre-written WhatsApp reminder message
    val reminderMessage = "সম্মানিত ${customer.name}, $pharmacyName থেকে আপনার পূর্বের বকেয়া বিল $dueAmountStr পরিশোধ করার জন্য বিনীত অনুরোধ করা হচ্ছে। ধন্যবাদ।"

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
                        color = if (customer.totalDue > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (customer.totalDue > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "মোট বাকি:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = dueAmountStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = if (customer.totalDue > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ঠিকানা: ${customer.address}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Action Buttons: Call, WhatsApp Reminder, Collect Due Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Call button
                    if (customer.phone.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { BengaliUtils.openDialer(context, customer.phone) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "কল", tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                            }
                        }

                        // WhatsApp Reminder Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.clickable {
                                BengaliUtils.openWhatsApp(context, customer.phone, reminderMessage)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp তাগাদা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            }
                        }
                    }
                }

                // Collect payment button
                if (customer.totalDue > 0) {
                    Button(
                        onClick = onCollectDue,
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("বকেয়া আদায়", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierKhataCard(
    supplier: Supplier,
    useBengali: Boolean,
    onPayDue: () -> Unit
) {
    val context = LocalContext.current
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
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Store, contentDescription = null, tint = Color(0xFFE65100))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = supplier.companyName,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "মহাজন দেনা:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = BengaliUtils.formatMoney(supplier.totalPayable, useBengali),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = IbmPlexMonoFontFamily,
                        color = Color(0xFFE65100)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (supplier.phone.isNotBlank()) {
                    Row(
                        modifier = Modifier.clickable { BengaliUtils.openDialer(context, supplier.phone) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(supplier.phone, fontSize = 12.sp, fontFamily = IbmPlexMonoFontFamily, color = TealPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (supplier.totalPayable > 0) {
                    Button(
                        onClick = onPayDue,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("পাওনা পরিশোধ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerItemRow(entry: LedgerEntry, useBengali: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.partyName} (${entry.note})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = BengaliUtils.formatDate(entry.date, useBengali),
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontFamily = IbmPlexMonoFontFamily
                )
            }

            val isCredit = entry.type in listOf("PAYMENT_RECEIVED", "PURCHASE_PAID")
            Text(
                text = "${if (isCredit) "-" else "+"}${BengaliUtils.formatMoney(entry.amount, useBengali)}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = IbmPlexMonoFontFamily,
                color = if (isCredit) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun CollectDueDialog(
    customer: Customer,
    useBengali: Boolean,
    onCollect: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf(customer.totalDue.toString()) }
    var noteInput by remember { mutableStateOf("নগদ পরিশোধ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "বকেয়া টাকা আদায় গ্রহণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "কাস্টমার: ${customer.name} (বাকি: ${BengaliUtils.formatMoney(customer.totalDue, useBengali)})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("আদায়কৃত টাকা (৳)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("নোট / বিবরণ") },
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
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onCollect(amt, noteInput.trim())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("আদায় জমা করুন")
                    }
                }
            }
        }
    }
}

@Composable
fun PaySupplierDialog(
    supplier: Supplier,
    useBengali: Boolean,
    onPay: (Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf(supplier.totalPayable.toString()) }
    var noteInput by remember { mutableStateOf("পাওনা পরিশোধ") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "মহাজন/সাপ্লায়ারকে বকেয়া পরিশোধ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "সাপ্লায়ার: ${supplier.name} (${supplier.companyName})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("পরিশোধিত টাকা (৳)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("নোট / রেফারেন্স") },
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
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onPay(amt, noteInput.trim())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Text("পরিশোধ সম্পন্ন")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomerDialog(
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("নতুন কাস্টমার যোগ করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("নাম*") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name.trim(), phone.trim(), address.trim()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) { Text("সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun AddSupplierDialog(
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("নতুন মহাজন/সাপ্লায়ার যোগ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("সাপ্লায়ার নাম*") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("ফার্মা কোম্পানি / ডিপো") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name.trim(), company.trim(), phone.trim(), address.trim()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) { Text("সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun EmptyKhataCard(text: String) {
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
            Text(text = text, color = Color.Gray, fontSize = 14.sp)
        }
    }
}
