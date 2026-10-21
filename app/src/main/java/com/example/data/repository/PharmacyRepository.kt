package com.example.data.repository

import android.content.Context
import com.example.data.model.CashTransaction
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.LedgerEntry
import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.PurchaseRecord
import com.example.data.model.SaleItem
import com.example.data.model.SaleRecord
import com.example.data.model.Supplier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class PharmacyRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataFile = File(context.filesDir, "pharmacy_khata_db.json")

    private val _pharmacies = MutableStateFlow<List<PharmacyProfile>>(emptyList())
    val pharmacies: StateFlow<List<PharmacyProfile>> = _pharmacies.asStateFlow()

    private val _activePharmacy = MutableStateFlow(PharmacyProfile())
    val activePharmacy: StateFlow<PharmacyProfile> = _activePharmacy.asStateFlow()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleRecord>>(emptyList())
    val sales: StateFlow<List<SaleRecord>> = _sales.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseRecord>>(emptyList())
    val purchases: StateFlow<List<PurchaseRecord>> = _purchases.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _suppliers = MutableStateFlow<List<Supplier>>(emptyList())
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _cashTransactions = MutableStateFlow<List<CashTransaction>>(emptyList())
    val cashTransactions: StateFlow<List<CashTransaction>> = _cashTransactions.asStateFlow()

    private val _ledgerEntries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val ledgerEntries: StateFlow<List<LedgerEntry>> = _ledgerEntries.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (!dataFile.exists()) {
            // First time launch: Initialize default pharmacy profile and demo data
            val defaultPharmacy = PharmacyProfile(
                id = "pharmacy-main",
                name = "জনসেবা ফার্মেসি ও মেডিসিন কর্নার",
                address = "হাসপাতাল গেট রোড, ঢাকা",
                phone = "01712345678",
                drugLicense = "DL-DHK-48291",
                useBengaliDigits = true,
                isDefault = true
            )
            _pharmacies.value = listOf(defaultPharmacy)
            _activePharmacy.value = defaultPharmacy
            seedInitialDemoData(defaultPharmacy.id)
            saveData()
            return
        }

        try {
            val jsonStr = dataFile.readText()
            val root = JSONObject(jsonStr)

            val pharmList = mutableListOf<PharmacyProfile>()
            val pharmArr = root.optJSONArray("pharmacies")
            if (pharmArr != null) {
                for (i in 0 until pharmArr.length()) {
                    pharmList.add(PharmacyProfile.fromJson(pharmArr.getJSONObject(i)))
                }
            }
            if (pharmList.isEmpty()) {
                val def = PharmacyProfile()
                pharmList.add(def)
            }
            _pharmacies.value = pharmList

            val activeId = root.optString("activePharmacyId", pharmList.first().id)
            _activePharmacy.value = pharmList.find { it.id == activeId } ?: pharmList.first()

            val medList = mutableListOf<Medicine>()
            val medArr = root.optJSONArray("medicines")
            if (medArr != null) {
                for (i in 0 until medArr.length()) {
                    medList.add(Medicine.fromJson(medArr.getJSONObject(i)))
                }
            }
            _medicines.value = medList

            val saleList = mutableListOf<SaleRecord>()
            val saleArr = root.optJSONArray("sales")
            if (saleArr != null) {
                for (i in 0 until saleArr.length()) {
                    saleList.add(SaleRecord.fromJson(saleArr.getJSONObject(i)))
                }
            }
            _sales.value = saleList

            val purList = mutableListOf<PurchaseRecord>()
            val purArr = root.optJSONArray("purchases")
            if (purArr != null) {
                for (i in 0 until purArr.length()) {
                    purList.add(PurchaseRecord.fromJson(purArr.getJSONObject(i)))
                }
            }
            _purchases.value = purList

            val custList = mutableListOf<Customer>()
            val custArr = root.optJSONArray("customers")
            if (custArr != null) {
                for (i in 0 until custArr.length()) {
                    custList.add(Customer.fromJson(custArr.getJSONObject(i)))
                }
            }
            _customers.value = custList

            val suppList = mutableListOf<Supplier>()
            val suppArr = root.optJSONArray("suppliers")
            if (suppArr != null) {
                for (i in 0 until suppArr.length()) {
                    suppList.add(Supplier.fromJson(suppArr.getJSONObject(i)))
                }
            }
            _suppliers.value = suppList

            val expList = mutableListOf<Expense>()
            val expArr = root.optJSONArray("expenses")
            if (expArr != null) {
                for (i in 0 until expArr.length()) {
                    expList.add(Expense.fromJson(expArr.getJSONObject(i)))
                }
            }
            _expenses.value = expList

            val cashList = mutableListOf<CashTransaction>()
            val cashArr = root.optJSONArray("cashTransactions")
            if (cashArr != null) {
                for (i in 0 until cashArr.length()) {
                    cashList.add(CashTransaction.fromJson(cashArr.getJSONObject(i)))
                }
            }
            _cashTransactions.value = cashList

            val ledgerList = mutableListOf<LedgerEntry>()
            val ledgerArr = root.optJSONArray("ledgerEntries")
            if (ledgerArr != null) {
                for (i in 0 until ledgerArr.length()) {
                    ledgerList.add(LedgerEntry.fromJson(ledgerArr.getJSONObject(i)))
                }
            }
            _ledgerEntries.value = ledgerList

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    private fun saveData() {
        scope.launch {
            try {
                val root = JSONObject()
                root.put("activePharmacyId", _activePharmacy.value.id)

                val pharmArr = JSONArray()
                _pharmacies.value.forEach { pharmArr.put(it.toJson()) }
                root.put("pharmacies", pharmArr)

                val medArr = JSONArray()
                _medicines.value.forEach { medArr.put(it.toJson()) }
                root.put("medicines", medArr)

                val saleArr = JSONArray()
                _sales.value.forEach { saleArr.put(it.toJson()) }
                root.put("sales", saleArr)

                val purArr = JSONArray()
                _purchases.value.forEach { purArr.put(it.toJson()) }
                root.put("purchases", purArr)

                val custArr = JSONArray()
                _customers.value.forEach { custArr.put(it.toJson()) }
                root.put("customers", custArr)

                val suppArr = JSONArray()
                _suppliers.value.forEach { suppArr.put(it.toJson()) }
                root.put("suppliers", suppArr)

                val expArr = JSONArray()
                _expenses.value.forEach { expArr.put(it.toJson()) }
                root.put("expenses", expArr)

                val cashArr = JSONArray()
                _cashTransactions.value.forEach { cashArr.put(it.toJson()) }
                root.put("cashTransactions", cashArr)

                val ledgerArr = JSONArray()
                _ledgerEntries.value.forEach { ledgerArr.put(it.toJson()) }
                root.put("ledgerEntries", ledgerArr)

                dataFile.writeText(root.toString(2))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Pharmacy Profile Management
    fun setActivePharmacy(pharmacyId: String) {
        val found = _pharmacies.value.find { it.id == pharmacyId }
        if (found != null) {
            _activePharmacy.value = found
            saveData()
        }
    }

    fun savePharmacyProfile(profile: PharmacyProfile) {
        val existing = _pharmacies.value.toMutableList()
        val index = existing.indexOfFirst { it.id == profile.id }
        if (index != -1) {
            existing[index] = profile
        } else {
            existing.add(profile)
        }
        _pharmacies.value = existing
        if (_activePharmacy.value.id == profile.id) {
            _activePharmacy.value = profile
        }
        saveData()
    }

    fun deletePharmacy(pharmacyId: String) {
        if (_pharmacies.value.size <= 1) return // Must keep at least one
        val updated = _pharmacies.value.filter { it.id != pharmacyId }
        _pharmacies.value = updated
        if (_activePharmacy.value.id == pharmacyId) {
            _activePharmacy.value = updated.first()
        }
        saveData()
    }

    // Medicine Stock Management
    fun addMedicine(medicine: Medicine) {
        val current = _medicines.value.toMutableList()
        current.add(0, medicine)
        _medicines.value = current
        saveData()
    }

    fun updateMedicine(medicine: Medicine) {
        val current = _medicines.value.toMutableList()
        val index = current.indexOfFirst { it.id == medicine.id }
        if (index != -1) {
            current[index] = medicine
            _medicines.value = current
            saveData()
        }
    }

    fun deleteMedicine(id: String) {
        _medicines.value = _medicines.value.filter { it.id != id }
        saveData()
    }

    fun adjustStock(medicineId: String, newPieces: Int) {
        val current = _medicines.value.toMutableList()
        val index = current.indexOfFirst { it.id == medicineId }
        if (index != -1) {
            val med = current[index]
            current[index] = med.copy(currentStockPieces = maxOf(0, newPieces))
            _medicines.value = current
            saveData()
        }
    }

    // POS & Sale Processing
    fun recordSale(sale: SaleRecord) {
        // 1. Add to sales list
        val salesList = _sales.value.toMutableList()
        salesList.add(0, sale)
        _sales.value = salesList

        // 2. Decrement medicine stocks
        val medMap = _medicines.value.associateBy { it.id }.toMutableMap()
        for (item in sale.items) {
            val med = medMap[item.medicineId]
            if (med != null) {
                val newStock = maxOf(0, med.currentStockPieces - item.totalPiecesSold)
                medMap[item.medicineId] = med.copy(currentStockPieces = newStock)
            }
        }
        _medicines.value = medMap.values.toList()

        // 3. Customer Ledger and Due handling
        if (sale.dueAmount > 0 && !sale.customerId.isNullOrBlank()) {
            val custList = _customers.value.toMutableList()
            val cIdx = custList.indexOfFirst { it.id == sale.customerId }
            if (cIdx != -1) {
                val cust = custList[cIdx]
                custList[cIdx] = cust.copy(totalDue = cust.totalDue + sale.dueAmount)
                _customers.value = custList
            }

            val ledgers = _ledgerEntries.value.toMutableList()
            ledgers.add(0, LedgerEntry(
                pharmacyId = sale.pharmacyId,
                partyType = "CUSTOMER",
                partyId = sale.customerId,
                partyName = sale.customerName,
                date = sale.date,
                type = "DUE_SALE",
                amount = sale.dueAmount,
                note = "বিক্রি ইনভয়েস #${sale.id}-এ বকেয়া"
            ))
            _ledgerEntries.value = ledgers
        }

        // 4. Record Cash Drawer transaction for paid amount
        if (sale.paidAmount > 0 && sale.paymentMethod == "CASH") {
            val cashList = _cashTransactions.value.toMutableList()
            cashList.add(0, CashTransaction(
                pharmacyId = sale.pharmacyId,
                type = "CASH_IN",
                amount = sale.paidAmount,
                reason = "ওষুধ বিক্রি (${sale.id})",
                date = sale.date
            ))
            _cashTransactions.value = cashList
        }

        saveData()
    }

    // Purchase Processing
    fun recordPurchase(purchase: PurchaseRecord) {
        val purList = _purchases.value.toMutableList()
        purList.add(0, purchase)
        _purchases.value = purList

        // Auto stock increment
        if (!purchase.medicineId.isNullOrBlank()) {
            val medList = _medicines.value.toMutableList()
            val mIdx = medList.indexOfFirst { it.id == purchase.medicineId }
            if (mIdx != -1) {
                val med = medList[mIdx]
                medList[mIdx] = med.copy(
                    currentStockPieces = med.currentStockPieces + purchase.quantityPieces,
                    batchNo = if (purchase.batchNo.isNotBlank()) purchase.batchNo else med.batchNo,
                    expiryDate = if (purchase.expiryDate.isNotBlank()) purchase.expiryDate else med.expiryDate
                )
                _medicines.value = medList
            }
        }

        // Supplier due handling
        if (purchase.dueAmount > 0 && purchase.supplierId.isNotBlank()) {
            val suppList = _suppliers.value.toMutableList()
            val sIdx = suppList.indexOfFirst { it.id == purchase.supplierId }
            if (sIdx != -1) {
                val supp = suppList[sIdx]
                suppList[sIdx] = supp.copy(totalPayable = supp.totalPayable + purchase.dueAmount)
                _suppliers.value = suppList
            }

            val ledgers = _ledgerEntries.value.toMutableList()
            ledgers.add(0, LedgerEntry(
                pharmacyId = purchase.pharmacyId,
                partyType = "SUPPLIER",
                partyId = purchase.supplierId,
                partyName = purchase.supplierName,
                date = purchase.date,
                type = "PURCHASE_DUE",
                amount = purchase.dueAmount,
                note = "ক্রয় চালান #${purchase.invoiceNo}-এ বাকি"
            ))
            _ledgerEntries.value = ledgers
        }

        // Cash out for paid amount
        if (purchase.paidAmount > 0) {
            val cashList = _cashTransactions.value.toMutableList()
            cashList.add(0, CashTransaction(
                pharmacyId = purchase.pharmacyId,
                type = "CASH_OUT",
                amount = purchase.paidAmount,
                reason = "ওষুধ ক্রয় (${purchase.supplierName})",
                date = purchase.date
            ))
            _cashTransactions.value = cashList
        }

        saveData()
    }

    // Customer & Supplier Management
    fun addCustomer(customer: Customer) {
        val list = _customers.value.toMutableList()
        list.add(0, customer)
        _customers.value = list
        saveData()
    }

    fun updateCustomer(customer: Customer) {
        val list = _customers.value.toMutableList()
        val idx = list.indexOfFirst { it.id == customer.id }
        if (idx != -1) {
            list[idx] = customer
            _customers.value = list
            saveData()
        }
    }

    fun recordCustomerDuePayment(customerId: String, amount: Double, note: String) {
        val custList = _customers.value.toMutableList()
        val cIdx = custList.indexOfFirst { it.id == customerId }
        if (cIdx != -1) {
            val cust = custList[cIdx]
            val newDue = maxOf(0.0, cust.totalDue - amount)
            custList[cIdx] = cust.copy(totalDue = newDue)
            _customers.value = custList

            val ledgers = _ledgerEntries.value.toMutableList()
            ledgers.add(0, LedgerEntry(
                pharmacyId = cust.pharmacyId,
                partyType = "CUSTOMER",
                partyId = cust.id,
                partyName = cust.name,
                date = System.currentTimeMillis(),
                type = "PAYMENT_RECEIVED",
                amount = amount,
                note = note.ifBlank { "বকেয়া আদায় পরিশোধ" }
            ))
            _ledgerEntries.value = ledgers

            val cashList = _cashTransactions.value.toMutableList()
            cashList.add(0, CashTransaction(
                pharmacyId = cust.pharmacyId,
                type = "CASH_IN",
                amount = amount,
                reason = "বকেয়া আদায় (${cust.name})",
                date = System.currentTimeMillis()
            ))
            _cashTransactions.value = cashList

            saveData()
        }
    }

    fun addSupplier(supplier: Supplier) {
        val list = _suppliers.value.toMutableList()
        list.add(0, supplier)
        _suppliers.value = list
        saveData()
    }

    fun updateSupplier(supplier: Supplier) {
        val list = _suppliers.value.toMutableList()
        val idx = list.indexOfFirst { it.id == supplier.id }
        if (idx != -1) {
            list[idx] = supplier
            _suppliers.value = list
            saveData()
        }
    }

    fun recordSupplierPayment(supplierId: String, amount: Double, note: String) {
        val suppList = _suppliers.value.toMutableList()
        val sIdx = suppList.indexOfFirst { it.id == supplierId }
        if (sIdx != -1) {
            val supp = suppList[sIdx]
            val newPayable = maxOf(0.0, supp.totalPayable - amount)
            suppList[sIdx] = supp.copy(totalPayable = newPayable)
            _suppliers.value = suppList

            val ledgers = _ledgerEntries.value.toMutableList()
            ledgers.add(0, LedgerEntry(
                pharmacyId = supp.pharmacyId,
                partyType = "SUPPLIER",
                partyId = supp.id,
                partyName = supp.name,
                date = System.currentTimeMillis(),
                type = "PURCHASE_PAID",
                amount = amount,
                note = note.ifBlank { "মহাজন বকেয়া পরিশোধ" }
            ))
            _ledgerEntries.value = ledgers

            val cashList = _cashTransactions.value.toMutableList()
            cashList.add(0, CashTransaction(
                pharmacyId = supp.pharmacyId,
                type = "CASH_OUT",
                amount = amount,
                reason = "মহাজন বকেয়া পরিশোধ (${supp.name})",
                date = System.currentTimeMillis()
            ))
            _cashTransactions.value = cashList

            saveData()
        }
    }

    // Expense Management
    fun addExpense(expense: Expense) {
        val list = _expenses.value.toMutableList()
        list.add(0, expense)
        _expenses.value = list

        val cashList = _cashTransactions.value.toMutableList()
        cashList.add(0, CashTransaction(
            pharmacyId = expense.pharmacyId,
            type = "CASH_OUT",
            amount = expense.amount,
            reason = "দোকান খরচ: ${expense.title} (${expense.category})",
            date = expense.date
        ))
        _cashTransactions.value = cashList

        saveData()
    }

    fun deleteExpense(id: String) {
        _expenses.value = _expenses.value.filter { it.id != id }
        saveData()
    }

    // Cash Drawer
    fun addCashTransaction(cashTransaction: CashTransaction) {
        val list = _cashTransactions.value.toMutableList()
        list.add(0, cashTransaction)
        _cashTransactions.value = list
        saveData()
    }

    // Backup & Restore
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        if (dataFile.exists()) dataFile.readText() else "{}"
    }

    suspend fun importJson(jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        try {
            JSONObject(jsonContent) // validate
            dataFile.writeText(jsonContent)
            loadData()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resetAndSeedDemo() {
        seedInitialDemoData(_activePharmacy.value.id)
        saveData()
    }

    private fun seedInitialDemoData(pharmacyId: String) {
        val demoMedicines = listOf(
            Medicine(
                id = "med-1",
                pharmacyId = pharmacyId,
                name = "Napa Extra 500mg",
                genericName = "Paracetamol + Caffeine",
                company = "Beximco Pharmaceuticals",
                type = "ট্যাবলেট",
                piecesPerStrip = 12,
                purchasePricePerStrip = 24.0,
                mrpPerStrip = 30.0,
                purchasePricePerPiece = 2.0,
                mrpPerPiece = 2.5,
                batchNo = "B-9402",
                expiryDate = "10/27",
                currentStockPieces = 240,
                minStockAlert = 36,
                barcode = "89411001",
                rackLocation = "A-1"
            ),
            Medicine(
                id = "med-2",
                pharmacyId = pharmacyId,
                name = "Seclo 20mg",
                genericName = "Omeprazole",
                company = "Square Pharmaceuticals",
                type = "ক্যাপসুল",
                piecesPerStrip = 10,
                purchasePricePerStrip = 48.0,
                mrpPerStrip = 60.0,
                purchasePricePerPiece = 4.8,
                mrpPerPiece = 6.0,
                batchNo = "SQ-8812",
                expiryDate = "08/27",
                currentStockPieces = 150,
                minStockAlert = 30,
                barcode = "89411002",
                rackLocation = "A-2"
            ),
            Medicine(
                id = "med-3",
                pharmacyId = pharmacyId,
                name = "Maxpro 20mg",
                genericName = "Esomeprazole",
                company = "Renata Limited",
                type = "ট্যাবলেট",
                piecesPerStrip = 10,
                purchasePricePerStrip = 56.0,
                mrpPerStrip = 70.0,
                purchasePricePerPiece = 5.6,
                mrpPerPiece = 7.0,
                batchNo = "RN-3310",
                expiryDate = "11/27",
                currentStockPieces = 80,
                minStockAlert = 20,
                barcode = "89411003",
                rackLocation = "A-3"
            ),
            Medicine(
                id = "med-4",
                pharmacyId = pharmacyId,
                name = "Ace Plus",
                genericName = "Paracetamol + Caffeine",
                company = "Square Pharmaceuticals",
                type = "ট্যাবলেট",
                piecesPerStrip = 10,
                purchasePricePerStrip = 22.0,
                mrpPerStrip = 27.5,
                purchasePricePerPiece = 2.2,
                mrpPerPiece = 2.75,
                batchNo = "SQ-4401",
                expiryDate = "05/27",
                currentStockPieces = 8, // Low Stock for testing alert
                minStockAlert = 20,
                barcode = "89411004",
                rackLocation = "B-1"
            ),
            Medicine(
                id = "med-5",
                pharmacyId = pharmacyId,
                name = "Ceevit 250mg",
                genericName = "Vitamin C (Ascorbic Acid)",
                company = "Square Pharmaceuticals",
                type = "ট্যাবলেট",
                piecesPerStrip = 10,
                purchasePricePerStrip = 16.0,
                mrpPerStrip = 20.0,
                purchasePricePerPiece = 1.6,
                mrpPerPiece = 2.0,
                batchNo = "SQ-1200",
                expiryDate = "12/28",
                currentStockPieces = 180,
                minStockAlert = 30,
                barcode = "89411005",
                rackLocation = "B-2"
            ),
            Medicine(
                id = "med-6",
                pharmacyId = pharmacyId,
                name = "Fexo 120mg",
                genericName = "Fexofenadine HCl",
                company = "Square Pharmaceuticals",
                type = "ট্যাবলেট",
                piecesPerStrip = 10,
                purchasePricePerStrip = 72.0,
                mrpPerStrip = 90.0,
                purchasePricePerPiece = 7.2,
                mrpPerPiece = 9.0,
                batchNo = "SQ-9901",
                expiryDate = "09/27",
                currentStockPieces = 0, // Stock Out for testing POS block
                minStockAlert = 20,
                barcode = "89411006",
                rackLocation = "B-3"
            ),
            Medicine(
                id = "med-7",
                pharmacyId = pharmacyId,
                name = "Monas 10mg",
                genericName = "Montelukast",
                company = "Acme Laboratories",
                type = "ট্যাবলেট",
                piecesPerStrip = 10,
                purchasePricePerStrip = 135.0,
                mrpPerStrip = 160.0,
                purchasePricePerPiece = 13.5,
                mrpPerPiece = 16.0,
                batchNo = "AC-7721",
                expiryDate = "04/24", // Expired for testing STRICT BLOCK
                currentStockPieces = 40,
                minStockAlert = 15,
                barcode = "89411007",
                rackLocation = "C-1"
            ),
            Medicine(
                id = "med-8",
                pharmacyId = pharmacyId,
                name = "Azithrocin 500mg",
                genericName = "Azithromycin",
                company = "Beximco Pharmaceuticals",
                type = "ক্যাপসুল",
                piecesPerStrip = 6,
                purchasePricePerStrip = 180.0,
                mrpPerStrip = 210.0,
                purchasePricePerPiece = 30.0,
                mrpPerPiece = 35.0,
                batchNo = "BX-6019",
                expiryDate = "12/27",
                currentStockPieces = 60,
                minStockAlert = 12,
                barcode = "89411008",
                rackLocation = "C-2"
            ),
            Medicine(
                id = "med-9",
                pharmacyId = pharmacyId,
                name = "Tofen Syrup 100ml",
                genericName = "Ketotifen",
                company = "Beximco Pharmaceuticals",
                type = "সিরাপ",
                piecesPerStrip = 1,
                purchasePricePerStrip = 68.0,
                mrpPerStrip = 85.0,
                purchasePricePerPiece = 68.0,
                mrpPerPiece = 85.0,
                batchNo = "BX-9941",
                expiryDate = "03/27",
                currentStockPieces = 15,
                minStockAlert = 5,
                barcode = "89411009",
                rackLocation = "D-1"
            ),
            Medicine(
                id = "med-10",
                pharmacyId = pharmacyId,
                name = "Oral Saline-N",
                genericName = "ORS (Oral Rehydration Salt)",
                company = "SMC Enterprise",
                type = "স্যালাইন",
                piecesPerStrip = 1,
                purchasePricePerStrip = 5.0,
                mrpPerStrip = 6.0,
                purchasePricePerPiece = 5.0,
                mrpPerPiece = 6.0,
                batchNo = "SMC-551",
                expiryDate = "01/28",
                currentStockPieces = 300,
                minStockAlert = 50,
                barcode = "89411010",
                rackLocation = "D-2"
            )
        )
        _medicines.value = demoMedicines

        val demoCustomers = listOf(
            Customer(
                id = "cust-1",
                pharmacyId = pharmacyId,
                name = "মোঃ রফিকুল ইসলাম",
                phone = "01711223344",
                address = "বাসা #১২, রোড #৪, ধানমন্ডি",
                totalDue = 450.0
            ),
            Customer(
                id = "cust-2",
                pharmacyId = pharmacyId,
                name = "আব্দুর রহিম মিয়া",
                phone = "01819876543",
                address = "গ্রাম: চাঁদপুর, থানা সদর",
                totalDue = 1200.0
            ),
            Customer(
                id = "cust-3",
                pharmacyId = pharmacyId,
                name = "ডাঃ সাজ্জাদ হোসেন",
                phone = "01912345678",
                address = "গ্রিনভিউ ক্লিনিক",
                totalDue = 0.0
            )
        )
        _customers.value = demoCustomers

        val demoSuppliers = listOf(
            Supplier(
                id = "supp-1",
                pharmacyId = pharmacyId,
                name = "স্কয়ার ডিস্ট্রিবিউশন",
                companyName = "Square Pharma Depot",
                phone = "01722334455",
                address = "তেজগাঁও শিল্প এলাকা, ঢাকা",
                totalPayable = 8500.0
            ),
            Supplier(
                id = "supp-2",
                pharmacyId = pharmacyId,
                name = "বেক্সিমকো ড্রাগ এজেন্সি",
                companyName = "Beximco Sales Depot",
                phone = "01833445566",
                address = "টঙ্গী, গাজীপুর",
                totalPayable = 3200.0
            )
        )
        _suppliers.value = demoSuppliers

        val demoExpenses = listOf(
            Expense(
                id = "exp-1",
                pharmacyId = pharmacyId,
                title = "দোকানের বিদ্যুৎ বিল",
                category = "বিদ্যুৎ বিল",
                amount = 1450.0,
                date = System.currentTimeMillis() - 86400000L * 2
            ),
            Expense(
                id = "exp-2",
                pharmacyId = pharmacyId,
                title = "স্টাফ দুপুরের খাবার ও নাস্তা",
                category = "চা-নাস্তা",
                amount = 260.0,
                date = System.currentTimeMillis() - 3600000L * 4
            )
        )
        _expenses.value = demoExpenses

        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        val demoSales = listOf(
            SaleRecord(
                id = "INV-78491",
                pharmacyId = pharmacyId,
                date = now - 3600000L * 2,
                customerId = "cust-1",
                customerName = "মোঃ রফিকুল ইসলাম",
                customerPhone = "01711223344",
                items = listOf(
                    SaleItem(
                        medicineId = "med-1",
                        medicineName = "Napa Extra 500mg",
                        medicineType = "ট্যাবলেট",
                        unitType = "পাতা/প্যাক",
                        quantity = 2,
                        totalPiecesSold = 24,
                        unitPrice = 30.0,
                        totalPrice = 60.0,
                        purchaseCost = 48.0
                    ),
                    SaleItem(
                        medicineId = "med-2",
                        medicineName = "Seclo 20mg",
                        medicineType = "ক্যাপসুল",
                        unitType = "পাতা/প্যাক",
                        quantity = 2,
                        totalPiecesSold = 20,
                        unitPrice = 60.0,
                        totalPrice = 120.0,
                        purchaseCost = 96.0
                    )
                ),
                subtotal = 180.0,
                discount = 10.0,
                discountType = "FLAT",
                grandTotal = 170.0,
                paidAmount = 100.0,
                dueAmount = 70.0,
                paymentMethod = "CASH",
                profit = 26.0
            ),
            SaleRecord(
                id = "INV-78490",
                pharmacyId = pharmacyId,
                date = now - oneDay,
                customerId = null,
                customerName = "সাধারণ কাস্টমার",
                customerPhone = "",
                items = listOf(
                    SaleItem(
                        medicineId = "med-8",
                        medicineName = "Azithrocin 500mg",
                        medicineType = "ক্যাপসুল",
                        unitType = "পাতা/প্যাক",
                        quantity = 1,
                        totalPiecesSold = 6,
                        unitPrice = 210.0,
                        totalPrice = 210.0,
                        purchaseCost = 180.0
                    ),
                    SaleItem(
                        medicineId = "med-5",
                        medicineName = "Ceevit 250mg",
                        medicineType = "ট্যাবলেট",
                        unitType = "পিস",
                        quantity = 5,
                        totalPiecesSold = 5,
                        unitPrice = 2.0,
                        totalPrice = 10.0,
                        purchaseCost = 8.0
                    )
                ),
                subtotal = 220.0,
                discount = 0.0,
                discountType = "FLAT",
                grandTotal = 220.0,
                paidAmount = 220.0,
                dueAmount = 0.0,
                paymentMethod = "BKASH",
                profit = 32.0
            ),
            SaleRecord(
                id = "INV-78489",
                pharmacyId = pharmacyId,
                date = now - oneDay * 2,
                customerName = "সাধারণ কাস্টমার",
                items = listOf(
                    SaleItem("med-3", "Maxpro 20mg", "ট্যাবলেট", "পাতা/প্যাক", 3, 30, 70.0, 210.0, 168.0)
                ),
                subtotal = 210.0,
                discount = 0.0,
                grandTotal = 210.0,
                paidAmount = 210.0,
                dueAmount = 0.0,
                paymentMethod = "CASH",
                profit = 42.0
            ),
            SaleRecord(
                id = "INV-78488",
                pharmacyId = pharmacyId,
                date = now - oneDay * 3,
                customerName = "সাধারণ কাস্টমার",
                items = listOf(
                    SaleItem("med-1", "Napa Extra 500mg", "ট্যাবলেট", "পাতা/প্যাক", 5, 60, 30.0, 150.0, 120.0)
                ),
                subtotal = 150.0,
                discount = 0.0,
                grandTotal = 150.0,
                paidAmount = 150.0,
                dueAmount = 0.0,
                paymentMethod = "CASH",
                profit = 30.0
            ),
            SaleRecord(
                id = "INV-78487",
                pharmacyId = pharmacyId,
                date = now - oneDay * 4,
                customerName = "সাধারণ কাস্টমার",
                items = listOf(
                    SaleItem("med-9", "Tofen Syrup 100ml", "সিরাপ", "পিস", 2, 2, 85.0, 170.0, 136.0)
                ),
                subtotal = 170.0,
                grandTotal = 170.0,
                paidAmount = 170.0,
                paymentMethod = "NAGAD",
                profit = 34.0
            ),
            SaleRecord(
                id = "INV-78486",
                pharmacyId = pharmacyId,
                date = now - oneDay * 5,
                customerName = "সাধারণ কাস্টমার",
                items = listOf(
                    SaleItem("med-10", "Oral Saline-N", "স্যালাইন", "পিস", 10, 10, 6.0, 60.0, 50.0)
                ),
                subtotal = 60.0,
                grandTotal = 60.0,
                paidAmount = 60.0,
                paymentMethod = "CASH",
                profit = 10.0
            )
        )
        _sales.value = demoSales

        val demoCash = listOf(
            CashTransaction(
                id = "csh-1",
                pharmacyId = pharmacyId,
                type = "CASH_IN",
                amount = 2500.0,
                reason = "দিনের প্রারম্ভিক ক্যাশ ব্যালেন্স",
                date = now - 3600000L * 8
            ),
            CashTransaction(
                id = "csh-2",
                pharmacyId = pharmacyId,
                type = "CASH_IN",
                amount = 100.0,
                reason = "ওষুধ বিক্রি (INV-78491)",
                date = now - 3600000L * 2
            ),
            CashTransaction(
                id = "csh-3",
                pharmacyId = pharmacyId,
                type = "CASH_OUT",
                amount = 260.0,
                reason = "স্টাফ দুপুরের খাবার ও নাস্তা",
                date = now - 3600000L * 4
            )
        )
        _cashTransactions.value = demoCash

        val demoLedgers = listOf(
            LedgerEntry(
                id = "led-1",
                pharmacyId = pharmacyId,
                partyType = "CUSTOMER",
                partyId = "cust-1",
                partyName = "মোঃ রফিকুল ইসলাম",
                date = now - 3600000L * 2,
                type = "DUE_SALE",
                amount = 70.0,
                note = "বিক্রি ইনভয়েস #INV-78491-এ বকেয়া"
            )
        )
        _ledgerEntries.value = demoLedgers
    }
}
