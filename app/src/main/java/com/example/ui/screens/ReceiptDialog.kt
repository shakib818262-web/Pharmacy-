package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PharmacyProfile
import com.example.data.model.SaleRecord
import com.example.ui.theme.IbmPlexMonoFontFamily
import com.example.ui.theme.TealPrimary
import com.example.util.BengaliUtils

@Composable
fun ReceiptDialog(
    pharmacy: PharmacyProfile,
    sale: SaleRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val useBengali = pharmacy.useBengaliDigits

    val receiptText = BengaliUtils.generateThermalReceiptText(pharmacy, sale, useBengali)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বিক্রির রসিদ (POS Invoice)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Thermal receipt paper container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .border(1.dp, Color(0xFFD0D7DE), RoundedCornerShape(8.dp)),
                    color = Color(0xFFFCFDFD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = pharmacy.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = pharmacy.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "মোবাইল: ${pharmacy.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = IbmPlexMonoFontFamily,
                            textAlign = TextAlign.Center
                        )
                        if (pharmacy.drugLicense.isNotBlank()) {
                            Text(
                                text = "ড্রাগ লাইসেন্স: ${pharmacy.drugLicense}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Invoice details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "চালান: ${sale.id}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = BengaliUtils.formatDate(sale.date, useBengali),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = IbmPlexMonoFontFamily,
                                color = Color.Gray
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "গ্রাহক: ${sale.customerName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            if (sale.customerPhone.isNotBlank()) {
                                Text(
                                    text = sale.customerPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = IbmPlexMonoFontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Items header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F4))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ওষুধের নাম",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = "পরিমাণ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "মূল্য",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        // Items
                        sale.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(
                                        text = item.medicineName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${BengaliUtils.formatMoney(item.unitPrice, useBengali)}/${item.unitType}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontFamily = IbmPlexMonoFontFamily
                                    )
                                }
                                Text(
                                    text = "${BengaliUtils.formatNumber(item.quantity, useBengali)} ${item.unitType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = BengaliUtils.formatMoney(item.totalPrice, useBengali),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    fontFamily = IbmPlexMonoFontFamily,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Totals
                        ReceiptLine(label = "সাবটোটাল", value = BengaliUtils.formatMoney(sale.subtotal, useBengali))
                        if (sale.discount > 0) {
                            val discLabel = if (sale.discountType == "PERCENT") " (${sale.discount}%)" else ""
                            ReceiptLine(label = "ডিসকাউন্ট$discLabel", value = "- ${BengaliUtils.formatMoney(sale.discount, useBengali)}", color = Color(0xFFC62828))
                        }
                        ReceiptLine(
                            label = "সর্বমোট প্রদেয়",
                            value = BengaliUtils.formatMoney(sale.grandTotal, useBengali),
                            isBold = true,
                            fontSize = 15
                        )
                        ReceiptLine(
                            label = "পরিশোধ (${BengaliUtils.getPaymentMethodBangla(sale.paymentMethod)})",
                            value = BengaliUtils.formatMoney(sale.paidAmount, useBengali),
                            color = Color(0xFF2E7D32)
                        )
                        if (sale.dueAmount > 0) {
                            ReceiptLine(
                                label = "বকেয়া / বাকি",
                                value = BengaliUtils.formatMoney(sale.dueAmount, useBengali),
                                isBold = true,
                                color = Color(0xFFD32F2F)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "সুস্থ থাকুন, ভালো থাকুন। ধন্যবাদ!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "--- ফার্মেসি খাতা POS ---",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Copy, Share, Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(receiptText))
                            Toast.makeText(context, "রসিদের টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কপি")
                    }

                    Button(
                        onClick = {
                            BengaliUtils.shareText(context, receiptText, "রসিদ পাঠান / প্রিন্ট")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার / প্রিন্ট")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    label: String,
    value: String,
    isBold: Boolean = false,
    fontSize: Int = 13,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = fontSize.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = IbmPlexMonoFontFamily,
            color = color
        )
    }
}
