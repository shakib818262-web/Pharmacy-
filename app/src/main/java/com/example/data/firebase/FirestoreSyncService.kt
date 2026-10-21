package com.example.data.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.CashTransaction
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.LedgerEntry
import com.example.data.model.Medicine
import com.example.data.model.PharmacyProfile
import com.example.data.model.PurchaseRecord
import com.example.data.model.SaleRecord
import com.example.data.model.Supplier
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface SyncStatus {
    object Offline : SyncStatus
    object Syncing : SyncStatus
    data class Synced(val lastSyncTime: Long) : SyncStatus
    data class Error(val message: String) : SyncStatus
}

class FirestoreSyncService(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Offline)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private var db: FirebaseFirestore? = null
    private var listeners = mutableListOf<ListenerRegistration>()

    init {
        try {
            val databaseId = context.getString(R.string.firestore_database_id)
            db = FirebaseFirestore.getInstance(databaseId)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error getting Firestore instance", e)
        }

        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun signInWithGoogle(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(activity)
        val clientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onError("Google Sign-In configuration missing")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    _currentUser.value = auth.currentUser
                    onSuccess()
                } else {
                    onError("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("FirestoreSync", "Sign-in cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Sign-in failed", e)
                onError(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        val credentialManager = CredentialManager.create(context)
        auth.signOut()
        _currentUser.value = null
        stopListening()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (_: Exception) {
            } finally {
                onComplete()
            }
        }
    }

    // Cloud Sync Operations
    fun uploadMedicineToCloud(pharmacyId: String, medicine: Medicine) {
        val uid = auth.currentUser?.uid ?: return
        val firestore = db ?: return
        scope.launch {
            try {
                _syncStatus.value = SyncStatus.Syncing
                firestore.collection("users").document(uid)
                    .collection("pharmacies").document(pharmacyId)
                    .collection("medicines").document(medicine.id)
                    .set(medicine.toJson().toMap().plus("userId" to uid))
                    .await()
                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Upload medicine failed", e)
            }
        }
    }

    fun uploadSaleToCloud(pharmacyId: String, sale: SaleRecord) {
        val uid = auth.currentUser?.uid ?: return
        val firestore = db ?: return
        scope.launch {
            try {
                _syncStatus.value = SyncStatus.Syncing
                firestore.collection("users").document(uid)
                    .collection("pharmacies").document(pharmacyId)
                    .collection("sales").document(sale.id)
                    .set(sale.toJson().toMap().plus("userId" to uid))
                    .await()
                _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Upload sale failed", e)
            }
        }
    }

    fun startListeningToPharmacyCloud(
        pharmacyId: String,
        onMedicinesUpdated: (List<Medicine>) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        val firestore = db ?: return
        stopListening()

        val listener = firestore.collection("users").document(uid)
            .collection("pharmacies").document(pharmacyId)
            .collection("medicines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _syncStatus.value = SyncStatus.Error(error.localizedMessage ?: "Sync error")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = mutableListOf<Medicine>()
                    for (doc in snapshot.documents) {
                        try {
                            val data = doc.data
                            if (data != null) {
                                val json = org.json.JSONObject(data)
                                list.add(Medicine.fromJson(json))
                            }
                        } catch (_: Exception) {}
                    }
                    if (list.isNotEmpty()) {
                        onMedicinesUpdated(list)
                        _syncStatus.value = SyncStatus.Synced(System.currentTimeMillis())
                    }
                }
            }
        listeners.add(listener)
    }

    fun stopListening() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }
}

// Helper to convert JSONObject to Map
private fun org.json.JSONObject.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    val keys = this.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val value = this.get(key)
        if (value != org.json.JSONObject.NULL) {
            map[key] = value
        }
    }
    return map
}
