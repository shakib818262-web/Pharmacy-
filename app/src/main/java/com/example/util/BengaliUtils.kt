package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.SaleRecord
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BengaliUtils {

    private val englishToBengaliDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯', '.' to '.'
    )

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            sb.append(englishToBengaliDigits[ch] ?: ch)
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Number): String {
        return toBengaliDigits(number.toString())
    }

    fun formatNumber(number: Number, useBengali: Boolean): String {
        return if (useBengali) toBengaliDigits(number.toString()) else number.toString()
    }

    fun formatMoney(amount: Double, useBengali: Boolean): String {
        val formatted = if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", amount)
        } else {
            String.format(Locale.US, "%.2f", amount)
        }
        val prefix = "৳ "
        return if (useBengali) {
            prefix + toBengaliDigits(formatted)
        } else {
            prefix + formatted
        }
    }

    fun formatDate(timestamp: Long, useBengali: Boolean): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.US)
        val formatted = sdf.format(Date(timestamp))
        return if (useBengali) toBengaliDigits(formatted) else formatted
    }

    fun formatShortDate(timestamp: Long, useBengali: Boolean): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        val formatted = sdf.format(Date(timestamp))
        return if (useBengali) toBengaliDigits(formatted) else formatted
    }

    fun createWhatsAppUrl(phone: String, message: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val formattedPhone = when {
            cleanPhone.startsWith("+88") -> cleanPhone.removePrefix("+")
            cleanPhone.startsWith("88") -> cleanPhone
            cleanPhone.startsWith("01") -> "88$cleanPhone"
            else -> cleanPhone
        }
        val encodedMessage = try {
            URLEncoder.encode(message, "UTF-8")
        } catch (_: Exception) {
            message
        }
        return "https://wa.me/$formattedPhone?text=$encodedMessage"
    }

    fun openWhatsApp(context: Context, phone: String, message: String) {
        val url = createWhatsAppUrl(phone, message)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to browser or SMS
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        }
    }

    fun openDialer(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if dialer unavailable
        }
    }

    fun shareText(context: Context, text: String, title: String = "শেয়ার করুন") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun generateThermalReceiptText(
        pharmacy: PharmacyProfile,
        sale: SaleRecord,
        useBengali: Boolean
    ): String {
        val line = "================================"
        val dash = "--------------------------------"
        val sb = StringBuilder()

        sb.append(line).append("\n")
        sb.append("      ${pharmacy.name}\n")
        sb.append("   ${pharmacy.address}\n")
        sb.append("   মোবাইল: ${pharmacy.phone}\n")
        if (pharmacy.drugLicense.isNotBlank()) {
            sb.append("   ড্রাগ লাইসেন্স: ${pharmacy.drugLicense}\n")
        }
        sb.append(line).append("\n")
        sb.append("রসিদ নং: ${sale.id}\n")
        sb.append("তারিখ: ${formatDate(sale.date, useBengali)}\n")
        sb.append("কাস্টমার: ${sale.customerName}\n")
        if (sale.customerPhone.isNotBlank()) {
            sb.append("ফোন: ${sale.customerPhone}\n")
        }
        sb.append(dash).append("\n")
        sb.append(String.format(Locale.US, "%-16s %4s %8s\n", "ওষুধের নাম", "পরিমাণ", "মূল্য"))
        sb.append(dash).append("\n")

        for (item in sale.items) {
            val nameTrunc = if (item.medicineName.length > 15) {
                item.medicineName.substring(0, 13) + ".."
            } else {
                item.medicineName
            }
            val qtyStr = "${item.quantity} ${item.unitType}"
            val priceStr = formatMoney(item.totalPrice, useBengali)
            sb.append(String.format(Locale.US, "%-16s %4s %8s\n", nameTrunc, qtyStr, priceStr))
        }

        sb.append(dash).append("\n")
        sb.append("সাবটোটাল:     ${formatMoney(sale.subtotal, useBengali)}\n")
        if (sale.discount > 0) {
            val discLabel = if (sale.discountType == "PERCENT") "${sale.discount}%" else ""
            sb.append("ডিসকাউন্ট $discLabel: ${formatMoney(sale.discount, useBengali)}\n")
        }
        sb.append("মোট প্রদেয়:   ${formatMoney(sale.grandTotal, useBengali)}\n")
        sb.append("পরিশোধ:       ${formatMoney(sale.paidAmount, useBengali)} (${getPaymentMethodBangla(sale.paymentMethod)})\n")
        if (sale.dueAmount > 0) {
            sb.append("বকেয়া/বাকি:   ${formatMoney(sale.dueAmount, useBengali)}\n")
        }
        sb.append(line).append("\n")
        sb.append("  সুস্থ থাকুন, ভালো থাকুন। ধন্যবাদ!\n")
        sb.append("  ফার্মেসি খাতা অ্যাপ দ্বারা প্রস্তুত\n")
        sb.append(line)

        return sb.toString()
    }

    fun generateOrderListText(
        pharmacy: PharmacyProfile,
        lowStockMedicines: List<Medicine>,
        useBengali: Boolean
    ): String {
        val sb = StringBuilder()
        sb.append("📦 জরুরি ওষুধের অর্ডার লিস্ট\n")
        sb.append("দোকান: ${pharmacy.name}\n")
        sb.append("মোবাইল: ${pharmacy.phone}\n")
        sb.append("তারিখ: ${formatShortDate(System.currentTimeMillis(), useBengali)}\n")
        sb.append("================================\n")

        if (lowStockMedicines.isEmpty()) {
            sb.append("বর্তমানে কোনো ওষুধের সংকট নেই। সব স্টক পর্যাপ্ত।\n")
        } else {
            lowStockMedicines.forEachIndexed { index, med ->
                val num = if (useBengali) toBengaliDigits(index + 1) else (index + 1).toString()
                val stockPieces = if (useBengali) toBengaliDigits(med.currentStockPieces) else med.currentStockPieces.toString()
                val strips = if (med.piecesPerStrip > 0) med.currentStockPieces / med.piecesPerStrip else 0
                val stripsStr = if (useBengali) toBengaliDigits(strips) else strips.toString()
                sb.append("$num. ${med.name} (${med.company})\n")
                sb.append("   জেনেরিক: ${med.genericName}\n")
                sb.append("   বর্তমান স্টক: $stockPieces পিস ($stripsStr পাতা)\n")
                sb.append("   প্রয়োজন: ______ পাতা/বক্স\n\n")
            }
        }
        sb.append("================================\n")
        sb.append("শীঘ্রই ডেলিভারি করার জন্য অনুরোধ করা হলো।")
        return sb.toString()
    }

    fun getPaymentMethodBangla(method: String): String {
        return when (method.uppercase(Locale.US)) {
            "CASH" -> "নগদ ক্যাশ"
            "BKASH" -> "বিকাশ"
            "NAGAD" -> "নগদ (Nagad)"
            "CARD" -> "কার্ড"
            "DUE" -> "সম্পূর্ণ বাকি"
            else -> method
        }
    }
}
