package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CashTransaction
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.PurchaseRecord
import com.example.data.model.SaleRecord
import com.example.data.model.Supplier
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestorePharmacyRepository(
    val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- Medicines Stream & Ops ---

    fun observeMedicines(): Flow<List<Medicine>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/medicines"
        emitAll(
            db.collection("users").document(uid).collection("medicines")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            Medicine(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                name = doc.getString("name") ?: "",
                                genericName = doc.getString("genericName") ?: "",
                                company = doc.getString("company") ?: "",
                                type = doc.getString("category") ?: "ট্যাবলেট",
                                rackLocation = doc.getString("rackLocation") ?: "",
                                mrpPerStrip = doc.getDouble("stripPrice") ?: 0.0,
                                mrpPerPiece = doc.getDouble("piecePrice") ?: 0.0,
                                piecesPerStrip = doc.getLong("piecesPerStrip")?.toInt() ?: 10,
                                purchasePricePerStrip = doc.getDouble("buyPricePerStrip") ?: 0.0,
                                purchasePricePerPiece = (doc.getDouble("buyPricePerStrip") ?: 0.0) / (doc.getLong("piecesPerStrip")?.toInt() ?: 10),
                                currentStockPieces = doc.getLong("currentStockPieces")?.toInt() ?: 0,
                                minStockAlert = doc.getLong("minStockAlertPieces")?.toInt() ?: 20,
                                batchNo = doc.getString("batchNumber") ?: "",
                                expiryDate = doc.getString("expiryDate") ?: "",
                                barcode = doc.getString("barcode") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveMedicine(medicine: Medicine): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (medicine.id.isBlank()) UUID.randomUUID().toString() else medicine.id
            val docRef = db.collection("users").document(uid).collection("medicines").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "pharmacyId" to medicine.pharmacyId,
                "name" to medicine.name,
                "genericName" to medicine.genericName,
                "company" to medicine.company,
                "category" to medicine.type,
                "rackLocation" to medicine.rackLocation,
                "stripPrice" to medicine.mrpPerStrip,
                "piecePrice" to medicine.mrpPerPiece,
                "piecesPerStrip" to medicine.piecesPerStrip,
                "buyPricePerStrip" to medicine.purchasePricePerStrip,
                "currentStockPieces" to medicine.currentStockPieces,
                "minStockAlertPieces" to medicine.minStockAlert,
                "batchNumber" to medicine.batchNo,
                "expiryDate" to medicine.expiryDate,
                "barcode" to medicine.barcode,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/medicines/${medicine.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteMedicine(medicineId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val path = "users/$uid/medicines/$medicineId"
            db.collection("users").document(uid).collection("medicines").document(medicineId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/medicines/$medicineId")
            Result.failure(e)
        }
    }

    // --- Pharmacy Profiles Stream & Ops ---

    fun observePharmacies(): Flow<List<PharmacyProfile>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/pharmacies"
        emitAll(
            db.collection("users").document(uid).collection("pharmacies")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            PharmacyProfile(
                                id = doc.getString("id") ?: doc.id,
                                name = doc.getString("name") ?: "জনসেবা ফার্মেসি",
                                address = doc.getString("address") ?: "",
                                drugLicense = doc.getString("drugLicense") ?: "",
                                phone = doc.getString("phone") ?: "",
                                useBengaliDigits = doc.getBoolean("useBengaliDigits") ?: true
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun savePharmacy(pharmacy: PharmacyProfile): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (pharmacy.id.isBlank()) UUID.randomUUID().toString() else pharmacy.id
            val docRef = db.collection("users").document(uid).collection("pharmacies").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "name" to pharmacy.name,
                "address" to pharmacy.address,
                "drugLicense" to pharmacy.drugLicense,
                "phone" to pharmacy.phone,
                "useBengaliDigits" to pharmacy.useBengaliDigits,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/pharmacies/${pharmacy.id}")
            Result.failure(e)
        }
    }

    // --- Sales Stream & Ops ---

    fun observeSales(): Flow<List<SaleRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/sales"
        emitAll(
            db.collection("users").document(uid).collection("sales")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            SaleRecord(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                date = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                customerId = doc.getString("customerId"),
                                customerName = doc.getString("customerName") ?: "",
                                customerPhone = doc.getString("customerPhone") ?: "",
                                items = emptyList(),
                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                discount = doc.getDouble("discount") ?: 0.0,
                                grandTotal = doc.getDouble("total") ?: 0.0,
                                paidAmount = doc.getDouble("paidAmount") ?: 0.0,
                                dueAmount = doc.getDouble("dueAmount") ?: 0.0,
                                paymentMethod = doc.getString("paymentMethod") ?: "CASH"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveSale(sale: SaleRecord): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (sale.id.isBlank()) UUID.randomUUID().toString() else sale.id
            val docRef = db.collection("users").document(uid).collection("sales").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "invoiceNumber" to sale.id,
                "pharmacyId" to sale.pharmacyId,
                "customerName" to sale.customerName,
                "customerPhone" to sale.customerPhone,
                "subtotal" to sale.subtotal,
                "discount" to sale.discount,
                "total" to sale.grandTotal,
                "paidAmount" to sale.paidAmount,
                "dueAmount" to sale.dueAmount,
                "paymentMethod" to sale.paymentMethod,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            sale.customerId?.let { payload["customerId"] = it }

            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/sales/${sale.id}")
            Result.failure(e)
        }
    }

    // --- Customers Stream & Ops ---

    fun observeCustomers(): Flow<List<Customer>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/customers"
        emitAll(
            db.collection("users").document(uid).collection("customers")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            Customer(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                name = doc.getString("name") ?: "",
                                phone = doc.getString("phone") ?: "",
                                address = doc.getString("address") ?: "",
                                totalDue = doc.getDouble("totalDue") ?: 0.0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCustomer(customer: Customer): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (customer.id.isBlank()) UUID.randomUUID().toString() else customer.id
            val docRef = db.collection("users").document(uid).collection("customers").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "pharmacyId" to customer.pharmacyId,
                "name" to customer.name,
                "phone" to customer.phone,
                "address" to customer.address,
                "totalDue" to customer.totalDue,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/customers/${customer.id}")
            Result.failure(e)
        }
    }

    // --- Suppliers Stream & Ops ---

    fun observeSuppliers(): Flow<List<Supplier>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/suppliers"
        emitAll(
            db.collection("users").document(uid).collection("suppliers")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            Supplier(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                name = doc.getString("name") ?: "",
                                phone = doc.getString("phone") ?: "",
                                companyName = doc.getString("company") ?: "",
                                totalPayable = doc.getDouble("totalPayable") ?: 0.0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveSupplier(supplier: Supplier): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (supplier.id.isBlank()) UUID.randomUUID().toString() else supplier.id
            val docRef = db.collection("users").document(uid).collection("suppliers").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "pharmacyId" to supplier.pharmacyId,
                "name" to supplier.name,
                "phone" to supplier.phone,
                "company" to supplier.companyName,
                "totalPayable" to supplier.totalPayable,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            } else {
                docRef.update(payload).await()
            }
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/suppliers/${supplier.id}")
            Result.failure(e)
        }
    }

    // --- Expenses Stream & Ops ---

    fun observeExpenses(): Flow<List<Expense>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/expenses"
        emitAll(
            db.collection("users").document(uid).collection("expenses")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            Expense(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                title = doc.getString("note") ?: doc.getString("category") ?: "খরচ",
                                category = doc.getString("category") ?: "দোকান খরচ",
                                amount = doc.getDouble("amount") ?: 0.0,
                                date = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveExpense(expense: Expense): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (expense.id.isBlank()) UUID.randomUUID().toString() else expense.id
            val docRef = db.collection("users").document(uid).collection("expenses").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "pharmacyId" to expense.pharmacyId,
                "category" to expense.category,
                "amount" to expense.amount,
                "note" to expense.title,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/expenses/${expense.id}")
            Result.failure(e)
        }
    }

    // --- Cash Transactions Stream & Ops ---

    fun observeCashTransactions(): Flow<List<CashTransaction>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/cash_transactions"
        emitAll(
            db.collection("users").document(uid).collection("cash_transactions")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            CashTransaction(
                                id = doc.getString("id") ?: doc.id,
                                pharmacyId = doc.getString("pharmacyId") ?: "",
                                type = doc.getString("type") ?: "CASH_IN",
                                amount = doc.getDouble("amount") ?: 0.0,
                                reason = doc.getString("reason") ?: "",
                                date = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveCashTransaction(tx: CashTransaction): Result<String> {
        return try {
            val uid = requireUserId()
            val id = if (tx.id.isBlank()) UUID.randomUUID().toString() else tx.id
            val docRef = db.collection("users").document(uid).collection("cash_transactions").document(id)

            val payload = hashMapOf<String, Any>(
                "id" to id,
                "pharmacyId" to tx.pharmacyId,
                "type" to if (tx.type == "CASH_OUT") "OUT" else "IN",
                "amount" to tx.amount,
                "reason" to tx.reason,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(id)
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "anon"
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/cash_transactions/${tx.id}")
            Result.failure(e)
        }
    }
}
