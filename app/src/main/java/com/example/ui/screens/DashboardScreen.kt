package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AssignmentLate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleRecord
import com.example.ui.AppScreen
import com.example.ui.PharmacyViewModel
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.StatusAlertBg
import com.example.ui.theme.StatusAlertText
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningText
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.util.BengaliUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PharmacyViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val activePharmacy by viewModel.activePharmacy.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cashTransactions by viewModel.cashTransactions.collectAsState()

    val useBengali = activePharmacy.useBengaliDigits

    // Filter calculations
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val todaySalesList = sales.filter { it.date >= startOfToday }
    val todayTotalSale = todaySalesList.sumOf { it.grandTotal }
    val todayTotalProfit = todaySalesList.sumOf { it.profit }

    val totalCustomerDue = customers.sumOf { it.totalDue }
    val totalCashIn = cashTransactions.filter { it.type == "CASH_IN" }.sumOf { it.amount }
    val totalCashOut = cashTransactions.filter { it.type == "CASH_OUT" }.sumOf { it.amount }
    val netCashInHand = totalCashIn - totalCashOut

    // Alert metrics
    val expiredMeds = medicines.filter { it.isExpired() }
    val stockOutMeds = medicines.filter { it.isStockOut() }
    val lowStockMeds = medicines.filter { it.isLowStock() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pharmacy Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TealPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(TealPrimary, TealSecondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activePharmacy.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activePharmacy.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                if (activePharmacy.drugLicense.isNotBlank()) {
                                    Text(
                                        text = "লাইসেন্স: ${activePharmacy.drugLicense}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontFamily = IbmPlexMonoFontFamily
                                    )
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPharmacy,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick action row inside header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(AppScreen.POS) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PointOfSale,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "নতুন বিক্রি",
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setShowOrderListDialog(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "অর্ডার লিস্ট",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4 Core Financial Summary Cards (আজকের বিক্রি, লাভ, ক্যাশ ড্রয়ার, বাকি)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "আজকের হিসাব ও মূল ব্যালেন্স",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "আজকের বিক্রি",
                        value = BengaliUtils.formatMoney(todayTotalSale, useBengali),
                        subtitle = "${BengaliUtils.formatNumber(todaySalesList.size, useBengali)} টি বিক্রি",
                        icon = Icons.Default.PointOfSale,
                        iconBg = Color(0xFFE0F2F1),
                        iconTint = Color(0xFF00796B),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.POS) }
                    )

                    MetricCard(
                        title = "আজকের লাভ",
                        value = BengaliUtils.formatMoney(todayTotalProfit, useBengali),
                        subtitle = "আনুমানিক গ্রস লাভ",
                        icon = Icons.Default.TrendingUp,
                        iconBg = Color(0xFFE8F5E9),
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.REPORTS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "মোট ক্যাশ ব্যালেন্স",
                        value = BengaliUtils.formatMoney(netCashInHand, useBengali),
                        subtitle = "ক্যাশ ড্রয়ারে জমা",
                        icon = Icons.Default.AttachMoney,
                        iconBg = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.REPORTS) }
                    )

                    MetricCard(
                        title = "মোট বাকি পাওনা",
                        value = BengaliUtils.formatMoney(totalCustomerDue, useBengali),
                        subtitle = "গ্রাহকদের কাছে পাওনা",
                        icon = Icons.Default.CreditCard,
                        iconBg = Color(0xFFFFEBEE),
                        iconTint = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppScreen.KHATA_LEDGER) }
                    )
                }
            }
        }

        // Warning Alert Banners (মেয়াদোত্তীর্ণ, স্টক শেষ, কম স্টক)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Expired Alert Banner
                if (expiredMeds.isNotEmpty()) {
                    AlertBanner(
                        title = "মেয়াদোত্তীর্ণ ওষুধ সতর্কবার্তা!",
                        description = "${BengaliUtils.formatNumber(expiredMeds.size, useBengali)} টি ওষুধের মেয়াদ শেষ হয়ে গেছে। বিক্রি বন্ধ করুন!",
                        containerColor = StatusAlertBg,
                        contentColor = StatusAlertText,
                        icon = Icons.Default.Error,
                        actionLabel = "দেখুন",
                        onClick = { onNavigate(AppScreen.INVENTORY) }
                    )
                }

                // Out of stock banner
                if (stockOutMeds.isNotEmpty()) {
                    AlertBanner(
                        title = "ওষুধের স্টক শেষ (Stock Out)",
                        description = "${BengaliUtils.formatNumber(stockOutMeds.size, useBengali)} টি ওষুধের স্টক ০ পিস। দ্রুত অর্ডার করুন।",
                        containerColor = Color(0xFFFBE9E7),
                        contentColor = Color(0xFFD84315),
                        icon = Icons.Default.AssignmentLate,
                        actionLabel = "অর্ডার করুন",
                        onClick = { viewModel.setShowOrderListDialog(true) }
                    )
                }

                // Low stock banner
                if (lowStockMeds.isNotEmpty()) {
                    AlertBanner(
                        title = "কম স্টক সতর্কবার্তা (Low Stock)",
                        description = "${BengaliUtils.formatNumber(lowStockMeds.size, useBengali)} টি ওষুধের স্টক সংকটজনক অবস্থায় রয়েছে।",
                        containerColor = StatusWarningBg,
                        contentColor = StatusWarningText,
                        icon = Icons.Default.Warning,
                        actionLabel = "তালিকা",
                        onClick = { onNavigate(AppScreen.INVENTORY) }
                    )
                }
            }
        }

        // 7-day Sales Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "গত ৭ দিনের বিক্রয় প্রবাহ (Sales Trend)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SevenDayBarChart(sales = sales, useBengali = useBengali)
                }
            }
        }

        // Recent Sales List Preview
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক বিক্রয় রশিদসমূহ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "সব দেখুন",
                        color = TealPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(AppScreen.REPORTS) }
                    )
                }

                val recentSales = sales.take(5)
                if (recentSales.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "আজ এখনো কোনো বিক্রি রেকর্ড করা হয়নি।",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    recentSales.forEach { sale ->
                        RecentSaleItemRow(sale = sale, useBengali = useBengali)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = IbmPlexMonoFontFamily
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AlertBanner(
    title: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
    actionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 14.sp
                )
                Text(
                    text = description,
                    color = contentColor.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = contentColor,
                modifier = Modifier.clickable { onClick() }
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SevenDayBarChart(sales: List<SaleRecord>, useBengali: Boolean) {
    // Generate data for past 7 days
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val dayNamesBangla = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
    val data = mutableListOf<Pair<String, Double>>()

    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 86400000L

        val dayTotal = sales.filter { it.date in dayStart until dayEnd }.sumOf { it.grandTotal }
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
        val label = dayNamesBangla[dayOfWeekIndex]
        data.add(label to dayTotal)
    }

    val maxSale = maxOf(100.0, data.maxOf { it.second })

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (dayLabel, amount) ->
            val ratio = (amount / maxSale).toFloat().coerceIn(0.08f, 1f)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (amount > 0) BengaliUtils.formatNumber(amount.toInt(), useBengali) else "-",
                    fontSize = 10.sp,
                    fontFamily = IbmPlexMonoFontFamily,
                    color = if (amount > 0) TealPrimary else Color.Gray,
                    fontWeight = if (amount > 0) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height((ratio * 85).dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (amount > 0) {
                                Brush.verticalGradient(listOf(TealPrimary, TealSecondary))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFFEEEEEE)))
                            }
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecentSaleItemRow(sale: SaleRecord, useBengali: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    color = Color(0xFFE0F2F1),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${sale.customerName} (${sale.id})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${BengaliUtils.formatDate(sale.date, useBengali)} • ${sale.items.size} টি আইটেম",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = IbmPlexMonoFontFamily
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = BengaliUtils.formatMoney(sale.grandTotal, useBengali),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = IbmPlexMonoFontFamily
                )
                if (sale.dueAmount > 0) {
                    Text(
                        text = "বাকি: ${BengaliUtils.formatMoney(sale.dueAmount, useBengali)}",
                        color = Color(0xFFD32F2F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "পরিশোধ (${BengaliUtils.getPaymentMethodBangla(sale.paymentMethod)})",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
