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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CashTransaction
import com.example.data.model.Expense
import com.example.data.model.SaleRecord
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils
import java.util.Calendar

enum class ReportPeriod(val label: String) {
    TODAY("আজ"),
    LAST_7_DAYS("গত ৭ দিন"),
    THIS_MONTH("এই মাস"),
    ALL_TIME("সর্বমোট")
}

@Composable
fun ReportsScreen(viewModel: PharmacyViewModel) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val cashTransactions by viewModel.cashTransactions.collectAsState()
    val useBengali = activePharmacy.useBengaliDigits

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Financial Report, 1: Shop Expenses, 2: Cash Drawer

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showCashInOutDialog by remember { mutableStateOf(false) }

    // Calculate time threshold
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val threshold = when (selectedPeriod) {
        ReportPeriod.TODAY -> calendar.timeInMillis
        ReportPeriod.LAST_7_DAYS -> now - (86400000L * 7)
        ReportPeriod.THIS_MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.timeInMillis
        }
        ReportPeriod.ALL_TIME -> 0L
    }

    val periodSales = sales.filter { it.date >= threshold }
    val periodExpenses = expenses.filter { it.date >= threshold }
    val periodCashTransactions = cashTransactions.filter { it.date >= threshold }

    val totalSale = periodSales.sumOf { it.grandTotal }
    val totalProfit = periodSales.sumOf { it.profit }
    val totalExpenseAmount = periodExpenses.sumOf { it.amount }
    val netProfit = totalProfit - totalExpenseAmount

    val totalCashIn = periodCashTransactions.filter { it.type == "CASH_IN" }.sumOf { it.amount }
    val totalCashOut = periodCashTransactions.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
    val netCashDrawer = totalCashIn - totalCashOut

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Filter Period
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "রিপোর্ট ও ক্যাশ ড্রয়ার (Reports & Cash)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Period Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReportPeriod.values().forEach { period ->
                            FilterChip(
                                selected = selectedPeriod == period,
                                onClick = { selectedPeriod = period },
                                label = { Text(period.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TealPrimary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("লাভ-ক্ষতি রিপোর্ট", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("দোকান খরচ", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("ক্যাশ বাক্স", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Financial Profit & Loss Overview
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (netProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "নিট লাভ (Net Profit) - ${selectedPeriod.label}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = BengaliUtils.formatMoney(netProfit, useBengali),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = IbmPlexMonoFontFamily,
                                        color = if (netProfit >= 0) Color(0xFF1B5E20) else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "= মোট বিক্রয় লাভ (${BengaliUtils.formatMoney(totalProfit, useBengali)}) - দোকান খরচ (${BengaliUtils.formatMoney(totalExpenseAmount, useBengali)})",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Core Metrics Row
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ReportMetricTile(
                                    title = "মোট বিক্রি",
                                    amount = BengaliUtils.formatMoney(totalSale, useBengali),
                                    subtitle = "${BengaliUtils.formatNumber(periodSales.size, useBengali)} টি ইনভয়েস",
                                    color = TealPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                ReportMetricTile(
                                    title = "মোট গ্রস লাভ",
                                    amount = BengaliUtils.formatMoney(totalProfit, useBengali),
                                    subtitle = "ওষুধ বিক্রয় লাভ",
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ReportMetricTile(
                                    title = "মোট দোকান খরচ",
                                    amount = BengaliUtils.formatMoney(totalExpenseAmount, useBengali),
                                    subtitle = "${BengaliUtils.formatNumber(periodExpenses.size, useBengali)} টি খরচ",
                                    color = Color(0xFFE65100),
                                    modifier = Modifier.weight(1f)
                                )
                                ReportMetricTile(
                                    title = "ক্যাশ ইন-হ্যান্ড",
                                    amount = BengaliUtils.formatMoney(netCashDrawer, useBengali),
                                    subtitle = "ক্যাশ ড্রয়ার হিসাব",
                                    color = Color(0xFF1976D2),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Breakdown of Sales in period
                        item {
                            Text(
                                text = "এই সময়ের বিক্রয় তালিকা (${BengaliUtils.formatNumber(periodSales.size, useBengali)})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(periodSales, key = { it.id }) { sale ->
                            RecentSaleItemRow(sale = sale, useBengali = useBengali)
                        }

                        item { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                }

                1 -> {
                    // Expenses Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "দোকান খরচের হিসাব (${BengaliUtils.formatMoney(totalExpenseAmount, useBengali)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showAddExpenseDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("খরচ যোগ", fontSize = 12.sp)
                                }
                            }
                        }

                        if (periodExpenses.isEmpty()) {
                            item {
                                EmptyKhataCard(text = "এই সময়ে কোনো দোকান খরচ রেকর্ড করা হয়নি।")
                            }
                        } else {
                            items(periodExpenses, key = { it.id }) { exp ->
                                ExpenseItemRow(
                                    expense = exp,
                                    useBengali = useBengali,
                                    onDelete = { viewModel.deleteExpense(exp.id) }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                }

                2 -> {
                    // Cash Drawer Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ক্যাশ বাক্সের ব্যালেন্স (Cash Drawer)",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = BengaliUtils.formatMoney(netCashDrawer, useBengali),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = IbmPlexMonoFontFamily,
                                        color = TealPrimary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "ক্যাশ জমা (In): +${BengaliUtils.formatMoney(totalCashIn, useBengali)}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "ক্যাশ খরচ (Out): -${BengaliUtils.formatMoney(totalCashOut, useBengali)}",
                                            fontSize = 12.sp,
                                            color = Color(0xFFD32F2F),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { showCashInOutDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ক্যাশ ইন / ক্যাশ আউট এন্ট্রি করুন")
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "ক্যাশ ট্রানজাকশন ইতিহাস",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(periodCashTransactions, key = { it.id }) { tx ->
                            CashTransactionRow(tx = tx, useBengali = useBengali)
                        }

                        item { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onSave = { title, cat, amt ->
                viewModel.addExpense(title, cat, amt)
                showAddExpenseDialog = false
            },
            onDismiss = { showAddExpenseDialog = false }
        )
    }

    if (showCashInOutDialog) {
        CashInOutDialog(
            onSave = { type, amt, reason ->
                viewModel.addCashTransaction(type, amt, reason)
                showCashInOutDialog = false
            },
            onDismiss = { showCashInOutDialog = false }
        )
    }
}

@Composable
fun ReportMetricTile(
    title: String,
    amount: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = IbmPlexMonoFontFamily,
                color = color
            )
            Text(subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ExpenseItemRow(
    expense: Expense,
    useBengali: Boolean,
    onDelete: () -> Unit
) {
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
                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "${expense.category} • ${BengaliUtils.formatDate(expense.date, useBengali)}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${BengaliUtils.formatMoney(expense.amount, useBengali)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = Color(0xFFD32F2F)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "মুছুন", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CashTransactionRow(tx: CashTransaction, useBengali: Boolean) {
    val isIn = tx.type == "CASH_IN"
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (isIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isIn) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tx.reason.ifBlank { if (isIn) "ক্যাশ জমা" else "ক্যাশ খরচ" },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = BengaliUtils.formatDate(tx.date, useBengali),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "${if (isIn) "+" else "-"}${BengaliUtils.formatMoney(tx.amount, useBengali)}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = IbmPlexMonoFontFamily,
                color = if (isIn) Color(0xFF2E7D32) else Color(0xFFD32F2F)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onSave: (String, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("দোকান ভাড়া") }
    var amount by remember { mutableStateOf("") }

    val categories = listOf("দোকান ভাড়া", "বিদ্যুৎ বিল", "কর্মচারীর বেতন", "চা-নাস্তা", "পরিবহন খরচ", "অন্যান্য")
    var catExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("দোকান খরচ যুক্ত করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("খরচের বিবরণ (যেমন: অক্টোবর মাসের বিদ্যুৎ বিল)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ক্যাটাগরি") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ (৳)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0) {
                                onSave(title.trim(), category, amt)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) { Text("সংরক্ষণ") }
                }
            }
        }
    }
}

@Composable
fun CashInOutDialog(
    onSave: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCashIn by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ক্যাশ বাক্সে এন্ট্রি", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isCashIn,
                        onClick = { isCashIn = true },
                        label = { Text("+ ক্যাশ জমা (Cash In)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF2E7D32), selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isCashIn,
                        onClick = { isCashIn = false },
                        label = { Text("- ক্যাশ খরচ (Cash Out)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD32F2F), selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("টাকার পরিমাণ (৳)*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("কারণ / বিবরণ (যেমন: মালিক ক্যাশ জমা দিলেন)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("বাতিল") }
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onSave(if (isCashIn) "CASH_IN" else "CASH_OUT", amt, reason.trim())
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) { Text("সংরক্ষণ") }
                }
            }
        }
    }
}
