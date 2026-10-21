package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CartItem
import com.example.data.model.CashTransaction
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.PurchaseRecord
import com.example.data.model.SaleItem
import com.example.data.model.SaleRecord
import com.example.data.model.Supplier
import com.example.data.model.UnitType
import com.example.data.repository.PharmacyRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

import android.app.Activity
import com.example.data.firebase.FirestoreSyncService
import com.example.data.firebase.SyncStatus
import com.google.firebase.auth.FirebaseUser

enum class AppScreen(val banglaTitle: String) {
    DASHBOARD("ড্যাশবোর্ড"),
    POS("পিওএস বিক্রি"),
    INVENTORY("স্টক খাতা"),
    PURCHASE("ক্রয় খাতা"),
    KHATA_LEDGER("বাকির খাতা"),
    REPORTS("রিপোর্ট ও ক্যাশ"),
    SETTINGS("সেটিংস")
}

class PharmacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PharmacyRepository(application)
    val syncService = FirestoreSyncService(application)

    val currentUser: StateFlow<FirebaseUser?> = syncService.currentUser
    val syncStatus: StateFlow<SyncStatus> = syncService.syncStatus

    // Current navigation tab
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Pharmacy Profile
    val activePharmacy: StateFlow<PharmacyProfile> = repository.activePharmacy
    val pharmacies: StateFlow<List<PharmacyProfile>> = repository.pharmacies

    // Repository flows filtered for active pharmacy
    val medicines: StateFlow<List<Medicine>> = combine(repository.medicines, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleRecord>> = combine(repository.sales, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<PurchaseRecord>> = combine(repository.purchases, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = combine(repository.customers, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = combine(repository.suppliers, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = combine(repository.expenses, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashTransactions: StateFlow<List<CashTransaction>> = combine(repository.cashTransactions, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerEntries: StateFlow<List<com.example.data.model.LedgerEntry>> = combine(repository.ledgerEntries, activePharmacy) { list, pharm ->
        list.filter { it.pharmacyId.isEmpty() || it.pharmacyId == pharm.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback Toast / Warning messages
    private val _alertMessage = MutableSharedFlow<String>()
    val alertMessage: SharedFlow<String> = _alertMessage.asSharedFlow()

    // POS Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _selectedCustomerId = MutableStateFlow<String?>(null)
    val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

    private val _customerNameInput = MutableStateFlow("সাধারণ কাস্টমার")
    val customerNameInput: StateFlow<String> = _customerNameInput.asStateFlow()

    private val _customerPhoneInput = MutableStateFlow("")
    val customerPhoneInput: StateFlow<String> = _customerPhoneInput.asStateFlow()

    private val _discountValue = MutableStateFlow(0.0)
    val discountValue: StateFlow<Double> = _discountValue.asStateFlow()

    private val _discountIsPercent = MutableStateFlow(false)
    val discountIsPercent: StateFlow<Boolean> = _discountIsPercent.asStateFlow()

    private val _paymentMethod = MutableStateFlow("CASH") // CASH, BKASH, NAGAD, CARD, DUE
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _paidAmountInput = MutableStateFlow<Double?>(null)
    val paidAmountInput: StateFlow<Double?> = _paidAmountInput.asStateFlow()

    // Completed Sale Receipt Dialog
    private val _lastCompletedSale = MutableStateFlow<SaleRecord?>(null)
    val lastCompletedSale: StateFlow<SaleRecord?> = _lastCompletedSale.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    private val _showOrderListDialog = MutableStateFlow(false)
    val showOrderListDialog: StateFlow<Boolean> = _showOrderListDialog.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // --- POS Cart Operations ---

    fun setPosSearchQuery(query: String) {
        _posSearchQuery.value = query
    }

    /**
     * Strict Check: Prevent adding expired or out-of-stock medicines to cart
     */
    fun addToCart(medicine: Medicine) {
        if (medicine.isExpired()) {
            emitAlert("⚠️ মেয়াদোত্তীর্ণ ওষুধ বিক্রি সম্পূর্ণ নিষিদ্ধ ও আইনত দণ্ডনীয়! (Exp: ${medicine.expiryDate})")
            return
        }
        if (medicine.isStockOut()) {
            emitAlert("⚠️ '${medicine.name}'-এর স্টক শূন্য (০)! বিক্রি করা যাবে না।")
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.medicine.id == medicine.id }

        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            val addedPieces = if (existing.unitType == UnitType.STRIP) medicine.piecesPerStrip else 1
            if (existing.totalPieces + addedPieces > medicine.currentStockPieces) {
                emitAlert("⚠️ পর্যাপ্ত স্টক নেই! দোকানে মাত্র ${medicine.currentStockPieces} পিস অবশিষ্ট রয়েছে।")
                return
            }
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            // Default to 1 piece
            currentList.add(CartItem(medicine = medicine, unitType = UnitType.PIECE, quantity = 1))
        }

        _cartItems.value = currentList
    }

    fun updateCartItemQuantity(medicineId: String, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.medicine.id == medicineId }
        if (index == -1) return

        val item = currentList[index]
        val newQty = item.quantity + delta

        if (newQty <= 0) {
            currentList.removeAt(index)
        } else {
            val totalPiecesNeeded = if (item.unitType == UnitType.STRIP) newQty * item.medicine.piecesPerStrip else newQty
            if (totalPiecesNeeded > item.medicine.currentStockPieces) {
                emitAlert("⚠️ পর্যাপ্ত স্টক নেই! মাত্র ${item.medicine.currentStockPieces} পিস রয়েছে।")
                return
            }
            currentList[index] = item.copy(quantity = newQty)
        }
        _cartItems.value = currentList
    }

    fun toggleCartItemUnit(medicineId: String) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.medicine.id == medicineId }
        if (index == -1) return

        val item = currentList[index]
        val newUnit = if (item.unitType == UnitType.PIECE) UnitType.STRIP else UnitType.PIECE
        val totalPiecesNeeded = if (newUnit == UnitType.STRIP) item.quantity * item.medicine.piecesPerStrip else item.quantity

        if (totalPiecesNeeded > item.medicine.currentStockPieces) {
            emitAlert("⚠️ পাতার জন্য পর্যাপ্ত স্টক নেই! মাত্র ${item.medicine.currentStockPieces} পিস রয়েছে।")
            return
        }

        currentList[index] = item.copy(unitType = newUnit)
        _cartItems.value = currentList
    }

    fun removeFromCart(medicineId: String) {
        _cartItems.value = _cartItems.value.filter { it.medicine.id != medicineId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomerId.value = null
        _customerNameInput.value = "সাধারণ কাস্টমার"
        _customerPhoneInput.value = ""
        _discountValue.value = 0.0
        _discountIsPercent.value = false
        _paidAmountInput.value = null
        _paymentMethod.value = "CASH"
    }

    fun selectCustomer(customer: Customer?) {
        if (customer != null) {
            _selectedCustomerId.value = customer.id
            _customerNameInput.value = customer.name
            _customerPhoneInput.value = customer.phone
        } else {
            _selectedCustomerId.value = null
            _customerNameInput.value = "সাধারণ কাস্টমার"
            _customerPhoneInput.value = ""
        }
    }

    fun setCustomerManual(name: String, phone: String) {
        _selectedCustomerId.value = null
        _customerNameInput.value = name.ifBlank { "সাধারণ কাস্টমার" }
        _customerPhoneInput.value = phone
    }

    fun setDiscount(amount: Double, isPercent: Boolean) {
        _discountValue.value = maxOf(0.0, amount)
        _discountIsPercent.value = isPercent
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    fun setPaidAmount(amount: Double?) {
        _paidAmountInput.value = amount
    }

    fun calculateCartSubtotal(): Double {
        return _cartItems.value.sumOf { it.totalPrice }
    }

    fun calculateDiscountAmount(): Double {
        val sub = calculateCartSubtotal()
        return if (_discountIsPercent.value) {
            (sub * _discountValue.value) / 100.0
        } else {
            minOf(sub, _discountValue.value)
        }
    }

    fun calculateGrandTotal(): Double {
        return maxOf(0.0, calculateCartSubtotal() - calculateDiscountAmount())
    }

    fun calculatePaidAmount(): Double {
        val grand = calculateGrandTotal()
        return when {
            _paymentMethod.value == "DUE" -> 0.0
            _paidAmountInput.value != null -> minOf(grand, maxOf(0.0, _paidAmountInput.value!!))
            else -> grand
        }
    }

    fun calculateDueAmount(): Double {
        return maxOf(0.0, calculateGrandTotal() - calculatePaidAmount())
    }

    /**
     * Complete POS sale with strict validation
     */
    fun completeSale() {
        val items = _cartItems.value
        if (items.isEmpty()) {
            emitAlert("⚠️ কার্ট খালি! অনুগ্রহ করে ওষুধ যোগ করুন।")
            return
        }

        // Re-verify no expired items slipped through
        val expiredItem = items.find { it.medicine.isExpired() }
        if (expiredItem != null) {
            emitAlert("⚠️ ত্রুটি: '${expiredItem.medicine.name}' ওষুধটির মেয়াদ শেষ হয়ে গেছে! বিক্রি বাতিল করা হলো।")
            return
        }

        val grandTotal = calculateGrandTotal()
        val paidAmount = calculatePaidAmount()
        val dueAmount = calculateDueAmount()

        // Strict Requirement: If due > 0, customer MUST be selected or have name/phone!
        if (dueAmount > 0) {
            if (_selectedCustomerId.value == null && (_customerNameInput.value == "সাধারণ কাস্টমার" || _customerNameInput.value.isBlank())) {
                emitAlert("⚠️ বাকি বিক্রির ক্ষেত্রে কাস্টমার নির্বাচন করা বা নাম প্রদান করা বাধ্যতামূলক!")
                return
            }
        }

        // Ensure customer exists in database if due
        var finalCustId = _selectedCustomerId.value
        if (dueAmount > 0 && finalCustId == null && _customerNameInput.value.isNotBlank() && _customerNameInput.value != "সাধারণ কাস্টমার") {
            // Auto create customer record so due can be tracked
            val newCust = Customer(
                pharmacyId = activePharmacy.value.id,
                name = _customerNameInput.value.trim(),
                phone = _customerPhoneInput.value.trim(),
                totalDue = 0.0
            )
            repository.addCustomer(newCust)
            finalCustId = newCust.id
        }

        var totalProfit = 0.0
        val saleItems = items.map { item ->
            val cost = item.purchaseCost
            val price = item.totalPrice
            totalProfit += (price - cost)
            SaleItem(
                medicineId = item.medicine.id,
                medicineName = item.medicine.name,
                medicineType = item.medicine.type,
                unitType = item.unitType.banglaLabel,
                quantity = item.quantity,
                totalPiecesSold = item.totalPieces,
                unitPrice = item.unitPrice,
                totalPrice = price,
                purchaseCost = cost
            )
        }

        // Subtract discount from profit
        totalProfit -= calculateDiscountAmount()

        val saleRecord = SaleRecord(
            pharmacyId = activePharmacy.value.id,
            customerId = finalCustId,
            customerName = _customerNameInput.value,
            customerPhone = _customerPhoneInput.value,
            items = saleItems,
            subtotal = calculateCartSubtotal(),
            discount = calculateDiscountAmount(),
            discountType = if (_discountIsPercent.value) "PERCENT" else "FLAT",
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentMethod = _paymentMethod.value,
            profit = maxOf(0.0, totalProfit)
        )

        repository.recordSale(saleRecord)
        syncService.uploadSaleToCloud(activePharmacy.value.id, saleRecord)

        // Show thermal receipt
        _lastCompletedSale.value = saleRecord
        _showReceiptDialog.value = true

        clearCart()
        emitAlert("✅ বিক্রি সফলভাবে সম্পন্ন হয়েছে!")
    }

    fun dismissReceiptDialog() {
        _showReceiptDialog.value = false
    }

    fun setShowOrderListDialog(show: Boolean) {
        _showOrderListDialog.value = show
    }

    // Medicine Inventory Actions
    fun saveMedicine(medicine: Medicine) {
        val target = medicine.copy(pharmacyId = activePharmacy.value.id)
        if (medicines.value.any { it.id == medicine.id }) {
            repository.updateMedicine(target)
            emitAlert("✅ ওষুধ তথ্য আপডেট করা হয়েছে")
        } else {
            repository.addMedicine(target)
            emitAlert("✅ নতুন ওষুধ যুক্ত করা হয়েছে")
        }
        syncService.uploadMedicineToCloud(activePharmacy.value.id, target)
    }

    fun deleteMedicine(id: String) {
        repository.deleteMedicine(id)
        emitAlert("🗑️ ওষুধ মুছে ফেলা হয়েছে")
    }

    fun adjustStock(medicineId: String, newPieces: Int) {
        repository.adjustStock(medicineId, newPieces)
        emitAlert("✅ স্টক সংখ্যা সংশোধন করা হয়েছে")
    }

    // Customer Actions
    fun saveCustomer(customer: Customer) {
        if (customers.value.any { it.id == customer.id }) {
            repository.updateCustomer(customer.copy(pharmacyId = activePharmacy.value.id))
        } else {
            repository.addCustomer(customer.copy(pharmacyId = activePharmacy.value.id))
        }
        emitAlert("✅ কাস্টমার তথ্য সংরক্ষিত")
    }

    fun collectCustomerDue(customerId: String, amount: Double, note: String) {
        if (amount <= 0) return
        repository.recordCustomerDuePayment(customerId, amount, note)
        emitAlert("✅ বকেয়া আদায় সফলভাবে রেকর্ড করা হয়েছে")
    }

    // Supplier Actions
    fun saveSupplier(supplier: Supplier) {
        if (suppliers.value.any { it.id == supplier.id }) {
            repository.updateSupplier(supplier.copy(pharmacyId = activePharmacy.value.id))
        } else {
            repository.addSupplier(supplier.copy(pharmacyId = activePharmacy.value.id))
        }
        emitAlert("✅ সাপ্লায়ার তথ্য সংরক্ষিত")
    }

    fun paySupplierDue(supplierId: String, amount: Double, note: String) {
        if (amount <= 0) return
        repository.recordSupplierPayment(supplierId, amount, note)
        emitAlert("✅ সাপ্লায়ারকে পাওনা পরিশোধ রেকর্ড হয়েছে")
    }

    // Purchase Actions
    fun recordPurchase(
        supplier: Supplier,
        invoiceNo: String,
        medicine: Medicine?,
        medicineNameCustom: String,
        quantityPieces: Int,
        totalAmount: Double,
        paidAmount: Double,
        batchNo: String,
        expiryDate: String
    ) {
        val dueAmount = maxOf(0.0, totalAmount - paidAmount)
        val record = PurchaseRecord(
            pharmacyId = activePharmacy.value.id,
            supplierId = supplier.id,
            supplierName = supplier.name,
            invoiceNo = invoiceNo,
            date = System.currentTimeMillis(),
            medicineName = medicine?.name ?: medicineNameCustom,
            medicineId = medicine?.id,
            quantityPieces = quantityPieces,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            batchNo = batchNo,
            expiryDate = expiryDate
        )
        repository.recordPurchase(record)
        emitAlert("✅ নতুন ক্রয় চালানের স্টক জমা হয়েছে!")
    }

    // Expense Actions
    fun addExpense(title: String, category: String, amount: Double) {
        if (title.isBlank() || amount <= 0) return
        val expense = Expense(
            pharmacyId = activePharmacy.value.id,
            title = title,
            category = category,
            amount = amount,
            date = System.currentTimeMillis()
        )
        repository.addExpense(expense)
        emitAlert("✅ দোকান খরচ রেকর্ড হয়েছে")
    }

    fun deleteExpense(id: String) {
        repository.deleteExpense(id)
        emitAlert("🗑️ খরচ এন্ট্রি মোছা হয়েছে")
    }

    // Cash Drawer
    fun addCashTransaction(type: String, amount: Double, reason: String) {
        if (amount <= 0) return
        val tx = CashTransaction(
            pharmacyId = activePharmacy.value.id,
            type = type,
            amount = amount,
            reason = reason,
            date = System.currentTimeMillis()
        )
        repository.addCashTransaction(tx)
        val typeLabel = if (type == "CASH_IN") "ক্যাশ জমা" else "ক্যাশ খরচ"
        emitAlert("✅ $typeLabel সম্পন্ন হয়েছে")
    }

    // Pharmacy Profile & Settings
    fun switchPharmacy(pharmacyId: String) {
        repository.setActivePharmacy(pharmacyId)
        clearCart()
        emitAlert("🏬 ফার্মেসি পরিবর্তন করা হয়েছে")
    }

    fun savePharmacyProfile(profile: PharmacyProfile) {
        repository.savePharmacyProfile(profile)
        emitAlert("✅ ফার্মেসির প্রোফাইল ও সেটিংস সংরক্ষিত")
    }

    fun deletePharmacy(pharmacyId: String) {
        repository.deletePharmacy(pharmacyId)
        emitAlert("🗑️ ফার্মেসি শাখা মুছে ফেলা হয়েছে")
    }

    fun toggleBengaliDigits(enabled: Boolean) {
        val current = activePharmacy.value
        savePharmacyProfile(current.copy(useBengaliDigits = enabled))
    }

    fun loadDemoData() {
        repository.resetAndSeedDemo()
        emitAlert("✨ নমুনা ডেটা (Demo Data) সফলভাবে লোড করা হয়েছে!")
    }

    suspend fun exportJson(): String = repository.exportJson()

    suspend fun importJson(content: String): Boolean {
        val success = repository.importJson(content)
        if (success) {
            emitAlert("✅ ব্যাকআপ ডাটা সফলভাবে রিস্টোর হয়েছে!")
        } else {
            emitAlert("❌ ভুল JSON ফরম্যাট! রিস্টোর ব্যর্থ হয়েছে।")
        }
        return success
    }

    fun handleScannedBarcode(barcode: String) {
        val trimmed = barcode.trim()
        val med = medicines.value.find {
            it.barcode.equals(trimmed, ignoreCase = true) ||
            it.barcode.contains(trimmed) ||
            it.id == trimmed
        }
        if (med != null) {
            if (med.isExpired()) {
                emitAlert("⚠️ স্ক্যানকৃত ওষুধ '${med.name}'-এর মেয়াদ শেষ (${med.expiryDate})! বিক্রি সম্পূর্ণ নিষিদ্ধ।")
            } else if (med.isStockOut()) {
                emitAlert("⚠️ স্ক্যানকৃত ওষুধ '${med.name}'-এর স্টক ০ পিস! কার্টে যোগ করা যাবে না।")
            } else {
                addToCart(med)
                emitAlert("✅ স্ক্যান সফল: '${med.name}' কার্টে যুক্ত হয়েছে!")
            }
        } else {
            setPosSearchQuery(trimmed)
            emitAlert("🔍 বারকোড '$trimmed' মিল পাওয়া যায়নি। সার্চ বক্সে ফিল্টার করা হলো।")
        }
    }

    fun signInWithGoogle(activity: Activity) {
        syncService.signInWithGoogle(
            activity = activity,
            onSuccess = {
                emitAlert("✅ গুগল ক্লাউড সিঙ্ক সক্রিয় হয়েছে!")
                syncService.startListeningToPharmacyCloud(activePharmacy.value.id) { cloudMeds ->
                    cloudMeds.forEach { repository.updateMedicine(it) }
                }
            },
            onError = { err ->
                emitAlert("❌ সাইন-ইন ত্রুটি: $err")
            }
        )
    }

    fun signOut() {
        syncService.signOut {
            emitAlert("গুগল অ্যাকাউন্ট থেকে সাইন-আউট করা হয়েছে")
        }
    }

    private fun emitAlert(message: String) {
        viewModelScope.launch {
            _alertMessage.emit(message)
        }
    }
}
