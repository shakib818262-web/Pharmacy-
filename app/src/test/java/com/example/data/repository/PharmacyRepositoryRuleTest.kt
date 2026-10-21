package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Medicine
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class PharmacyRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveMedicine_authenticatedOwner_createsDocumentSuccessfully(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestorePharmacyRepository(firestore, auth)

        val medicine = Medicine(
            id = "med_" + UUID.randomUUID().toString().take(8),
            name = "Napa 500mg",
            genericName = "Paracetamol",
            company = "Beximco",
            mrpPerPiece = 1.2,
            currentStockPieces = 150
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMedicine(medicine)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun observeMedicines_authenticatedOwner_emitsCreatedMedicine(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = FirestorePharmacyRepository(firestore, auth)
        val medId = "med_" + UUID.randomUUID().toString().take(8)

        val medicine = Medicine(
            id = medId,
            name = "Seclo 20mg",
            genericName = "Omeprazole",
            mrpPerPiece = 5.0,
            currentStockPieces = 60
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMedicine(medicine)
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMedicines().first { list -> list.any { it.id == medId } }
        }
        assertTrue(emitted.any { it.id == medId })
    }

    @Test
    fun crossUserAccess_bobCannotAccessAliceMedicines_failsWithPermissionDenied(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestorePharmacyRepository(firestore, auth)
        val medId = "med_" + UUID.randomUUID().toString().take(8)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveMedicine(
                Medicine(id = medId, name = "Ceevit", mrpPerPiece = 2.0, currentStockPieces = 30)
            )
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("medicines").document(medId).get().await()
            }
            fail("Expected PERMISSION_DENIED exception")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun unauthenticatedAccess_fails(): Unit = runBlocking {
        auth.signOut()
        val repository = FirestorePharmacyRepository(firestore, auth)

        val result = repository.saveMedicine(
            Medicine(id = "med_anon", name = "Test", mrpPerPiece = 1.0, currentStockPieces = 10)
        )
        assertTrue(result.isFailure)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@pharmacy.test"
        const val BOB_EMAIL = "bob@pharmacy.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
