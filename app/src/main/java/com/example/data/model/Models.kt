package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class PharmacyProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "জনসেবা ফার্মেসি",
    val address: String = "হাসপাতাল রোড, ঢাকা",
    val phone: String = "01712345678",
    val drugLicense: String = "DL-DHK-54892",
    val useBengaliDigits: Boolean = true,
    val isDefault: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("address", address)
        put("phone", phone)
        put("drugLicense", drugLicense)
        put("useBengaliDigits", useBengaliDigits)
        put("isDefault", isDefault)
    }

    companion object {
        fun fromJson(json: JSONObject): PharmacyProfile = PharmacyProfile(
            id = json.optString("id", UUID.randomUUID().toString()),
            name = json.optString("name", "জনসেবা ফার্মেসি"),
            address = json.optString("address", "হাসপাতাল রোড, ঢাকা"),
            phone = json.optString("phone", "01712345678"),
            drugLicense = json.optString("drugLicense", "DL-DHK-54892"),
            useBengaliDigits = json.optBoolean("useBengaliDigits", true),
            isDefault = json.optBoolean("isDefault", true)
        )
    }
}

data class Medicine(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val name: String,                    // e.g. "Napa Extra"
    val genericName: String = "",        // e.g. "Paracetamol + Caffeine"
    val company: String = "",            // e.g. "Beximco Pharmaceuticals"
    val type: String = "ট্যাবলেট",         // ট্যাবলেট, ক্যাপসুল, সিরাপ, ইনজেকশন, ড্রপ, মলম, স্যালাইন
    val piecesPerStrip: Int = 10,        // প্রতি পাতায় পিস
    val purchasePricePerStrip: Double = 20.0,
    val mrpPerStrip: Double = 25.0,
    val purchasePricePerPiece: Double = 2.0,
    val mrpPerPiece: Double = 2.5,
    val batchNo: String = "B-101",
    val expiryDate: String = "12/27",    // MM/YY format
    val currentStockPieces: Int = 100,   // Total pieces in stock
    val minStockAlert: Int = 20,         // Alert threshold
    val barcode: String = "",
    val rackLocation: String = "A-1"
) {
    fun isStockOut(): Boolean = currentStockPieces <= 0
    fun isLowStock(): Boolean = currentStockPieces in 1..minStockAlert

    fun isExpired(): Boolean {
        if (!expiryDate.contains("/")) return false
        val parts = expiryDate.split("/")
        if (parts.size != 2) return false
        val expMonth = parts[0].trim().toIntOrNull() ?: return false
        val expYearStr = parts[1].trim()
        val expYear = if (expYearStr.length == 2) 2000 + (expYearStr.toIntOrNull() ?: 0) else expYearStr.toIntOrNull() ?: 0

        val cal = java.util.Calendar.getInstance()
        val currentYear = cal.get(java.util.Calendar.YEAR)
        val currentMonth = cal.get(java.util.Calendar.MONTH) + 1 // 1-12

        return when {
            expYear < currentYear -> true
            expYear == currentYear && expMonth < currentMonth -> true
            else -> false
        }
    }

    fun isNearExpiry(): Boolean {
        if (isExpired()) return false
        if (!expiryDate.contains("/")) return false
        val parts = expiryDate.split("/")
        if (parts.size != 2) return false
        val expMonth = parts[0].trim().toIntOrNull() ?: return false
        val expYearStr = parts[1].trim()
        val expYear = if (expYearStr.length == 2) 2000 + (expYearStr.toIntOrNull() ?: 0) else expYearStr.toIntOrNull() ?: 0

        val cal = java.util.Calendar.getInstance()
        val currentYear = cal.get(java.util.Calendar.YEAR)
        val currentMonth = cal.get(java.util.Calendar.MONTH) + 1

        val monthsDiff = (expYear - currentYear) * 12 + (expMonth - currentMonth)
        return monthsDiff in 0..2 // Within next 2 months
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("name", name)
        put("genericName", genericName)
        put("company", company)
        put("type", type)
        put("piecesPerStrip", piecesPerStrip)
        put("purchasePricePerStrip", purchasePricePerStrip)
        put("mrpPerStrip", mrpPerStrip)
        put("purchasePricePerPiece", purchasePricePerPiece)
        put("mrpPerPiece", mrpPerPiece)
        put("batchNo", batchNo)
        put("expiryDate", expiryDate)
        put("currentStockPieces", currentStockPieces)
        put("minStockAlert", minStockAlert)
        put("barcode", barcode)
        put("rackLocation", rackLocation)
    }

    companion object {
        fun fromJson(json: JSONObject): Medicine = Medicine(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            name = json.optString("name", ""),
            genericName = json.optString("genericName", ""),
            company = json.optString("company", ""),
            type = json.optString("type", "ট্যাবলেট"),
            piecesPerStrip = json.optInt("piecesPerStrip", 10),
            purchasePricePerStrip = json.optDouble("purchasePricePerStrip", 0.0),
            mrpPerStrip = json.optDouble("mrpPerStrip", 0.0),
            purchasePricePerPiece = json.optDouble("purchasePricePerPiece", 0.0),
            mrpPerPiece = json.optDouble("mrpPerPiece", 0.0),
            batchNo = json.optString("batchNo", ""),
            expiryDate = json.optString("expiryDate", "12/26"),
            currentStockPieces = json.optInt("currentStockPieces", 0),
            minStockAlert = json.optInt("minStockAlert", 10),
            barcode = json.optString("barcode", ""),
            rackLocation = json.optString("rackLocation", "")
        )
    }
}

data class CartItem(
    val medicine: Medicine,
    val unitType: UnitType = UnitType.PIECE, // PIECE or STRIP
    val quantity: Int = 1
) {
    val totalPieces: Int
        get() = if (unitType == UnitType.STRIP) quantity * medicine.piecesPerStrip else quantity

    val unitPrice: Double
        get() = if (unitType == UnitType.STRIP) medicine.mrpPerStrip else medicine.mrpPerPiece

    val purchaseCost: Double
        get() = if (unitType == UnitType.STRIP) {
            quantity * medicine.purchasePricePerStrip
        } else {
            quantity * medicine.purchasePricePerPiece
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}

enum class UnitType(val banglaLabel: String) {
    PIECE("পিস"),
    STRIP("পাতা/প্যাক")
}

data class SaleItem(
    val medicineId: String,
    val medicineName: String,
    val medicineType: String,
    val unitType: String,
    val quantity: Int,
    val totalPiecesSold: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val purchaseCost: Double
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("medicineId", medicineId)
        put("medicineName", medicineName)
        put("medicineType", medicineType)
        put("unitType", unitType)
        put("quantity", quantity)
        put("totalPiecesSold", totalPiecesSold)
        put("unitPrice", unitPrice)
        put("totalPrice", totalPrice)
        put("purchaseCost", purchaseCost)
    }

    companion object {
        fun fromJson(json: JSONObject): SaleItem = SaleItem(
            medicineId = json.optString("medicineId", ""),
            medicineName = json.optString("medicineName", ""),
            medicineType = json.optString("medicineType", "ট্যাবলেট"),
            unitType = json.optString("unitType", "পিস"),
            quantity = json.optInt("quantity", 1),
            totalPiecesSold = json.optInt("totalPiecesSold", 1),
            unitPrice = json.optDouble("unitPrice", 0.0),
            totalPrice = json.optDouble("totalPrice", 0.0),
            purchaseCost = json.optDouble("purchaseCost", 0.0)
        )
    }
}

data class SaleRecord(
    val id: String = "INV-${System.currentTimeMillis() % 1000000}",
    val pharmacyId: String = "",
    val date: Long = System.currentTimeMillis(),
    val customerId: String? = null,
    val customerName: String = "সাধারণ কাস্টমার",
    val customerPhone: String = "",
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val discountType: String = "FLAT", // "FLAT" or "PERCENT"
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = "CASH", // CASH, BKASH, NAGAD, CARD, DUE
    val profit: Double = 0.0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("date", date)
        put("customerId", customerId ?: "")
        put("customerName", customerName)
        put("customerPhone", customerPhone)
        val itemsArr = JSONArray()
        items.forEach { itemsArr.put(it.toJson()) }
        put("items", itemsArr)
        put("subtotal", subtotal)
        put("discount", discount)
        put("discountType", discountType)
        put("grandTotal", grandTotal)
        put("paidAmount", paidAmount)
        put("dueAmount", dueAmount)
        put("paymentMethod", paymentMethod)
        put("profit", profit)
    }

    companion object {
        fun fromJson(json: JSONObject): SaleRecord {
            val itemsList = mutableListOf<SaleItem>()
            val itemsArr = json.optJSONArray("items")
            if (itemsArr != null) {
                for (i in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.optJSONObject(i)
                    if (itemObj != null) itemsList.add(SaleItem.fromJson(itemObj))
                }
            }
            return SaleRecord(
                id = json.optString("id", ""),
                pharmacyId = json.optString("pharmacyId", ""),
                date = json.optLong("date", System.currentTimeMillis()),
                customerId = json.optString("customerId").takeIf { it.isNotBlank() },
                customerName = json.optString("customerName", "সাধারণ কাস্টমার"),
                customerPhone = json.optString("customerPhone", ""),
                items = itemsList,
                subtotal = json.optDouble("subtotal", 0.0),
                discount = json.optDouble("discount", 0.0),
                discountType = json.optString("discountType", "FLAT"),
                grandTotal = json.optDouble("grandTotal", 0.0),
                paidAmount = json.optDouble("paidAmount", 0.0),
                dueAmount = json.optDouble("dueAmount", 0.0),
                paymentMethod = json.optString("paymentMethod", "CASH"),
                profit = json.optDouble("profit", 0.0)
            )
        }
    }
}

data class Customer(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val name: String,
    val phone: String = "",
    val address: String = "",
    val totalDue: Double = 0.0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("name", name)
        put("phone", phone)
        put("address", address)
        put("totalDue", totalDue)
    }

    companion object {
        fun fromJson(json: JSONObject): Customer = Customer(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            name = json.optString("name", ""),
            phone = json.optString("phone", ""),
            address = json.optString("address", ""),
            totalDue = json.optDouble("totalDue", 0.0)
        )
    }
}

data class Supplier(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val name: String,
    val companyName: String = "",
    val phone: String = "",
    val address: String = "",
    val totalPayable: Double = 0.0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("name", name)
        put("companyName", companyName)
        put("phone", phone)
        put("address", address)
        put("totalPayable", totalPayable)
    }

    companion object {
        fun fromJson(json: JSONObject): Supplier = Supplier(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            name = json.optString("name", ""),
            companyName = json.optString("companyName", ""),
            phone = json.optString("phone", ""),
            address = json.optString("address", ""),
            totalPayable = json.optDouble("totalPayable", 0.0)
        )
    }
}

data class PurchaseRecord(
    val id: String = "PUR-${System.currentTimeMillis() % 1000000}",
    val pharmacyId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val invoiceNo: String = "",
    val date: Long = System.currentTimeMillis(),
    val medicineName: String = "",
    val medicineId: String? = null,
    val quantityPieces: Int = 0,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val batchNo: String = "",
    val expiryDate: String = "12/27"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("supplierId", supplierId)
        put("supplierName", supplierName)
        put("invoiceNo", invoiceNo)
        put("date", date)
        put("medicineName", medicineName)
        put("medicineId", medicineId ?: "")
        put("quantityPieces", quantityPieces)
        put("totalAmount", totalAmount)
        put("paidAmount", paidAmount)
        put("dueAmount", dueAmount)
        put("batchNo", batchNo)
        put("expiryDate", expiryDate)
    }

    companion object {
        fun fromJson(json: JSONObject): PurchaseRecord = PurchaseRecord(
            id = json.optString("id", ""),
            pharmacyId = json.optString("pharmacyId", ""),
            supplierId = json.optString("supplierId", ""),
            supplierName = json.optString("supplierName", ""),
            invoiceNo = json.optString("invoiceNo", ""),
            date = json.optLong("date", System.currentTimeMillis()),
            medicineName = json.optString("medicineName", ""),
            medicineId = json.optString("medicineId").takeIf { it.isNotBlank() },
            quantityPieces = json.optInt("quantityPieces", 0),
            totalAmount = json.optDouble("totalAmount", 0.0),
            paidAmount = json.optDouble("paidAmount", 0.0),
            dueAmount = json.optDouble("dueAmount", 0.0),
            batchNo = json.optString("batchNo", ""),
            expiryDate = json.optString("expiryDate", "12/27")
        )
    }
}

data class LedgerEntry(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val partyType: String = "CUSTOMER", // "CUSTOMER" or "SUPPLIER"
    val partyId: String = "",
    val partyName: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: String = "DUE_SALE", // "DUE_SALE", "PAYMENT_RECEIVED", "PURCHASE_DUE", "PURCHASE_PAID"
    val amount: Double = 0.0,
    val note: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("partyType", partyType)
        put("partyId", partyId)
        put("partyName", partyName)
        put("date", date)
        put("type", type)
        put("amount", amount)
        put("note", note)
    }

    companion object {
        fun fromJson(json: JSONObject): LedgerEntry = LedgerEntry(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            partyType = json.optString("partyType", "CUSTOMER"),
            partyId = json.optString("partyId", ""),
            partyName = json.optString("partyName", ""),
            date = json.optLong("date", System.currentTimeMillis()),
            type = json.optString("type", "DUE_SALE"),
            amount = json.optDouble("amount", 0.0),
            note = json.optString("note", "")
        )
    }
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val title: String,
    val category: String = "দোকান খরচ", // দোকান ভাড়া, বিদ্যুৎ বিল, কর্মচারীর বেতন, চা-নাস্তা, ইত্যাদি
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("title", title)
        put("category", category)
        put("amount", amount)
        put("date", date)
    }

    companion object {
        fun fromJson(json: JSONObject): Expense = Expense(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            title = json.optString("title", ""),
            category = json.optString("category", "দোকান খরচ"),
            amount = json.optDouble("amount", 0.0),
            date = json.optLong("date", System.currentTimeMillis())
        )
    }
}

data class CashTransaction(
    val id: String = UUID.randomUUID().toString(),
    val pharmacyId: String = "",
    val type: String = "CASH_IN", // "CASH_IN" or "CASH_OUT"
    val amount: Double = 0.0,
    val reason: String = "",
    val date: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("pharmacyId", pharmacyId)
        put("type", type)
        put("amount", amount)
        put("reason", reason)
        put("date", date)
    }

    companion object {
        fun fromJson(json: JSONObject): CashTransaction = CashTransaction(
            id = json.optString("id", UUID.randomUUID().toString()),
            pharmacyId = json.optString("pharmacyId", ""),
            type = json.optString("type", "CASH_IN"),
            amount = json.optDouble("amount", 0.0),
            reason = json.optString("reason", ""),
            date = json.optLong("date", System.currentTimeMillis())
        )
    }
}
