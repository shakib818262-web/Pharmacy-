package com.example

import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.SaleItem
import com.example.data.model.SaleRecord
import com.example.util.BengaliUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBengaliDigitConversion() {
        assertEquals("১২৩৪৫৬৭৮৯০", BengaliUtils.toBengaliDigits("1234567890"))
        assertEquals("৳ ১২০", BengaliUtils.formatMoney(120.0, true))
        assertEquals("৳ 120", BengaliUtils.formatMoney(120.0, false))
    }

    @Test
    fun testMedicineExpiryCheck() {
        val expiredMed = Medicine(
            name = "Old Medicine",
            expiryDate = "01/20"
        )
        assertTrue(expiredMed.isExpired())

        val futureMed = Medicine(
            name = "Fresh Medicine",
            expiryDate = "12/35"
        )
        assertFalse(futureMed.isExpired())
    }

    @Test
    fun testReceiptGeneration() {
        val pharmacy = PharmacyProfile(
            name = "জনসেবা ফার্মেসি",
            phone = "01712345678"
        )
        val sale = SaleRecord(
            id = "INV-100",
            customerName = "করিম মিয়া",
            items = listOf(
                SaleItem(
                    medicineId = "1",
                    medicineName = "Napa Extra",
                    medicineType = "ট্যাবলেট",
                    unitType = "পাতা",
                    quantity = 2,
                    totalPiecesSold = 20,
                    unitPrice = 30.0,
                    totalPrice = 60.0,
                    purchaseCost = 48.0
                )
            ),
            subtotal = 60.0,
            grandTotal = 60.0,
            paidAmount = 60.0,
            dueAmount = 0.0
        )
        val receipt = BengaliUtils.generateThermalReceiptText(pharmacy, sale, true)
        assertTrue(receipt.contains("জনসেবা ফার্মেসি"))
        assertTrue(receipt.contains("Napa Extra"))
        assertTrue(receipt.contains("INV-100"))
    }
}
