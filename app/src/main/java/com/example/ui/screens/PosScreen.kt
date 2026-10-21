package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.Medicine
import com.example.data.model.UnitType
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.StatusAlertBg
import com.example.ui.theme.StatusAlertText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.BengaliUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PharmacyViewModel) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsState()
    val customerNameInput by viewModel.customerNameInput.collectAsState()
    val customerPhoneInput by viewModel.customerPhoneInput.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val discountValue by viewModel.discountValue.collectAsState()
    val discountIsPercent by viewModel.discountIsPercent.collectAsState()
    val paidAmountInput by viewModel.paidAmountInput.collectAsState()

    val useBengali = activePharmacy.useBengaliDigits

    var showDiscountDialog by remember { mutableStateOf(false) }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showCustomPaidDialog by remember { mutableStateOf(false) }

    // Filter medicines
    val filteredMedicines = remember(medicines, searchQuery) {
        if (searchQuery.isBlank()) {
            medicines
        } else {
            val q = searchQuery.lowercase().trim()
            medicines.filter {
                it.name.lowercase().contains(q) ||
                it.genericName.lowercase().contains(q) ||
                it.company.lowercase().contains(q) ||
                it.barcode.contains(q) ||
                it.rackLocation.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setPosSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_search_input"),
                    placeholder = { Text("ওষুধ খুঁজুন (নাম, জেনেরিক বা বারকোড)...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TealPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setPosSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "মুছুন")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Two Section View: Medicines list on top/scrollable, Cart at bottom or side
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ওষুধের তালিকা (${BengaliUtils.formatNumber(filteredMedicines.size, useBengali)})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        if (cartItems.isNotEmpty()) {
                            Text(
                                text = "কার্টে ${BengaliUtils.formatNumber(cartItems.size, useBengali)} টি আইটেম",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(filteredMedicines, key = { it.id }) { medicine ->
                    MedicinePosCard(
                        medicine = medicine,
                        useBengali = useBengali,
                        onAddToCart = { viewModel.addToCart(medicine) }
                    )
                }

                // Bottom padding so items are not hidden by bottom cart sheet
                item {
                    Spacer(modifier = Modifier.height(180.dp))
                }
            }
        }

        // Bottom POS Cart & Checkout Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Cart Items Expandable Preview / Controls
                if (cartItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কার্টের আইটেমসমূহ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("কার্ট খালি করুন", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                    }

                    // Cart items horizontally or compact list
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cartItems.forEach { item ->
                            CartItemRow(
                                item = item,
                                useBengali = useBengali,
                                onIncrement = { viewModel.updateCartItemQuantity(item.medicine.id, 1) },
                                onDecrement = { viewModel.updateCartItemQuantity(item.medicine.id, -1) },
                                onToggleUnit = { viewModel.toggleCartItemUnit(item.medicine.id) },
                                onRemove = { viewModel.removeFromCart(item.medicine.id) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                }

                // Customer Selector and Discount Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customer button
                    Surface(
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable { showCustomerDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = customerNameInput,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (selectedCustomerId != null) "নিয়মিত গ্রাহক" else "ট্যাপ করে পরিবর্তন",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Discount Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDiscountDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Discount, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (discountValue > 0) {
                                        if (discountIsPercent) "${BengaliUtils.formatNumber(discountValue, useBengali)}%" else BengaliUtils.formatMoney(discountValue, useBengali)
                                    } else "ডিসকাউন্ট",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = if (discountValue > 0) "ছাড় সক্রিয়" else "যোগ করুন",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Method Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("CASH" to "ক্যাশ", "BKASH" to "বিকাশ", "NAGAD" to "নগদ", "CARD" to "কার্ড", "DUE" to "সম্পূর্ণ বাকি").forEach { (code, label) ->
                        val selected = paymentMethod == code
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setPaymentMethod(code) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Financial Total and Checkout Button
                val grandTotal = viewModel.calculateGrandTotal()
                val paidAmount = viewModel.calculatePaidAmount()
                val dueAmount = viewModel.calculateDueAmount()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "সর্বমোট: ${BengaliUtils.formatMoney(grandTotal, useBengali)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = IbmPlexMonoFontFamily,
                            color = TealPrimary
                        )
                        if (dueAmount > 0) {
                            Text(
                                text = "বাকি: ${BengaliUtils.formatMoney(dueAmount, useBengali)} (পরিশোধ: ${BengaliUtils.formatMoney(paidAmount, useBengali)})",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.completeSale() },
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("complete_sale_button")
                    ) {
                        Icon(imageVector = Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("বিক্রি সম্পন্ন করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Customer Selection / Add Dialog
    if (showCustomerDialog) {
        CustomerSelectDialog(
            customers = customers,
            currentCustomerName = customerNameInput,
            currentPhone = customerPhoneInput,
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerDialog = false
            },
            onManualCustomer = { name, phone ->
                viewModel.setCustomerManual(name, phone)
                showCustomerDialog = false
            },
            onDismiss = { showCustomerDialog = false }
        )
    }

    // Discount Input Dialog
    if (showDiscountDialog) {
        DiscountDialog(
            currentValue = discountValue,
            isPercent = discountIsPercent,
            onApply = { amt, percent ->
                viewModel.setDiscount(amt, percent)
                showDiscountDialog = false
            },
            onDismiss = { showDiscountDialog = false }
        )
    }
}

@Composable
fun MedicinePosCard(
    medicine: Medicine,
    useBengali: Boolean,
    onAddToCart: () -> Unit
) {
    val isExpired = medicine.isExpired()
    val isStockOut = medicine.isStockOut()
    val isLowStock = medicine.isLowStock()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isExpired && !isStockOut) { onAddToCart() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExpired -> StatusAlertBg
                isStockOut -> Color(0xFFF9F9F9)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isExpired) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)) else null,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Text(
                    text = "${medicine.genericName} • ${medicine.company}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price and stock info row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "দর: ${BengaliUtils.formatMoney(medicine.mrpPerPiece, useBengali)}/পিস (পাতা ${BengaliUtils.formatMoney(medicine.mrpPerStrip, useBengali)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                    if (medicine.rackLocation.isNotBlank()) {
                        Text(
                            text = "র‌্যাক: ${medicine.rackLocation}",
                            fontSize = 11.sp,
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status badges: Expired, Stock out, or Low Stock
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when {
                        isExpired -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD32F2F)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "মেয়াদোত্তীর্ণ! বিক্রি সম্পূর্ণ নিষিদ্ধ (${medicine.expiryDate})",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        isStockOut -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFD84315)
                            ) {
                                Text(
                                    text = "স্টক শেষ (০ পিস)",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        isLowStock -> {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusWarningBg
                            ) {
                                Text(
                                    text = "কম স্টক: ${BengaliUtils.formatNumber(medicine.currentStockPieces, useBengali)} পিস বাকি",
                                    color = StatusWarningText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "স্টক: ${BengaliUtils.formatNumber(medicine.currentStockPieces, useBengali)} পিস (মেয়াদ: ${medicine.expiryDate})",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontFamily = IbmPlexMonoFontFamily
                            )
                        }
                    }
                }
            }

            // Add button
            Surface(
                shape = CircleShape,
                color = if (isExpired || isStockOut) Color.LightGray else TealPrimary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(enabled = !isExpired && !isStockOut) { onAddToCart() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Block else Icons.Default.Add,
                        contentDescription = "কার্টে যোগ",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    useBengali: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onToggleUnit: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF5F7F6),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.medicine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "${BengaliUtils.formatMoney(item.unitPrice, useBengali)} x ${BengaliUtils.formatNumber(item.quantity, useBengali)} ${item.unitType.banglaLabel} = ${BengaliUtils.formatMoney(item.totalPrice, useBengali)}",
                    fontSize = 11.sp,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Strip vs Piece unit switcher toggle
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, TealPrimary),
                modifier = Modifier.clickable { onToggleUnit() }
            ) {
                Text(
                    text = item.unitType.banglaLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity stepper (- qty +)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDecrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "কমান", modifier = Modifier.size(14.dp))
                    }
                }

                Text(
                    text = BengaliUtils.formatNumber(item.quantity, useBengali),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = IbmPlexMonoFontFamily,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = TealPrimary,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onIncrement() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "বাড়ান", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CustomerSelectDialog(
    customers: List<Customer>,
    currentCustomerName: String,
    currentPhone: String,
    onSelect: (Customer?) -> Unit,
    onManualCustomer: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameInput by remember { mutableStateOf(if (currentCustomerName == "সাধারণ কাস্টমার") "" else currentCustomerName) }
    var phoneInput by remember { mutableStateOf(currentPhone) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "কাস্টমার নির্বাচন বা যোগ করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Walk-in Quick Button
                OutlinedButton(
                    onClick = { onSelect(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সাধারণ কাস্টমার (Walk-in Customer)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Entry or Quick Add
                Text("নতুন কাস্টমার তথ্য লিখুন:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("কাস্টমার নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onManualCustomer(nameInput.trim(), phoneInput.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("এই কাস্টমার সেট করুন")
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("পূর্ববর্তী কাস্টমার তালিকা:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("খুঁজুন...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(cust) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F7F6)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(cust.phone, fontSize = 11.sp, color = Color.Gray)
                                }
                                if (cust.totalDue > 0) {
                                    Text("বাকি: ৳ ${cust.totalDue}", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscountDialog(
    currentValue: Double,
    isPercent: Boolean,
    onApply: (Double, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var discountInput by remember { mutableStateOf(if (currentValue > 0) currentValue.toString() else "") }
    var isPercentageSelected by remember { mutableStateOf(isPercent) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ডিসকাউন্ট বা ছাড় দিন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isPercentageSelected,
                        onClick = { isPercentageSelected = false },
                        label = { Text("টাকা (৳ Flat)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isPercentageSelected,
                        onClick = { isPercentageSelected = true },
                        label = { Text("শতকরা (% Percent)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = discountInput,
                    onValueChange = { discountInput = it },
                    label = { Text(if (isPercentageSelected) "ডিসকাউন্ট শতকরা (%)" else "ডিসকাউন্ট টাকা (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onApply(0.0, false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("মুছুন")
                    }

                    Button(
                        onClick = {
                            val amt = discountInput.toDoubleOrNull() ?: 0.0
                            onApply(amt, isPercentageSelected)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text("প্রয়োগ করুন")
                    }
                }
            }
        }
    }
}
