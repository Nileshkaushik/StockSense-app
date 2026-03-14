package com.stocksense.app.feature.auth.domain

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class TrialStatus(
    val hasActiveTrial: Boolean,
    val trialUsed: Boolean
)

@Singleton
class TrialManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION = "trial_registry"
        private const val FIELD_STARTED_AT = "trialStartedAt"
        private const val FIELD_USED = "trialUsed"
    }

    // Call after successful phone auth
    // phoneNumber must be in +91XXXXXXXXXX format
    suspend fun checkAndGrantTrial(phoneNumber: String): TrialStatus {
        val docRef = firestore.collection(COLLECTION).document(phoneNumber)
        val snapshot = docRef.get().await()

        return if (!snapshot.exists()) {
            // First time this number has ever logged in — grant trial
            val data = mapOf(
                FIELD_STARTED_AT to Timestamp.now(),
                FIELD_USED to true
            )
            docRef.set(data).await()
            TrialStatus(hasActiveTrial = true, trialUsed = false)
        } else {
            // Number exists — trial already used
            TrialStatus(hasActiveTrial = false, trialUsed = true)
        }
    }

    suspend fun getTrialStatus(phoneNumber: String): TrialStatus {
        val snapshot = firestore
            .collection(COLLECTION)
            .document(phoneNumber)
            .get()
            .await()

        return if (!snapshot.exists()) {
            TrialStatus(hasActiveTrial = false, trialUsed = false)
        } else {
            val trialUsed = snapshot.getBoolean(FIELD_USED) ?: false
            TrialStatus(hasActiveTrial = !trialUsed, trialUsed = trialUsed)
        }
    }
}