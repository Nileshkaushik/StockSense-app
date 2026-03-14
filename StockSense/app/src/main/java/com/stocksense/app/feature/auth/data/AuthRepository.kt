package com.stocksense.app.feature.auth.data

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.stocksense.app.feature.auth.domain.TrialManager
import com.stocksense.app.feature.auth.domain.TrialStatus
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    object OtpSent : AuthResult()
    data class OtpVerified(val trialStatus: TrialStatus) : AuthResult()
    object GmailLinked : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val trialManager: TrialManager
) {
    companion object {
        private const val WEB_CLIENT_ID =
            "855241433770-gb9hk6pufh9mtsfuojqribc3jert5fs5.apps.googleusercontent.com"
    }

    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val currentUser: FirebaseUser? get() = auth.currentUser

    // ── Phone OTP ─────────────────────────────────────────────────────────────

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onResult: (AuthResult) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential, phoneNumber, onResult)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    onResult(AuthResult.Failure(
                        e.message ?: "Verification failed. Please try again."
                    ))
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    onResult(AuthResult.OtpSent)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun verifyOtp(otp: String, phoneNumber: String): AuthResult {
        val verificationId = storedVerificationId
            ?: return AuthResult.Failure("Session expired. Please try again.")
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        return signInWithPhoneCredentialSuspend(credential, phoneNumber)
    }

    private fun signInWithPhoneCredential(
        credential: PhoneAuthCredential,
        phoneNumber: String,
        onResult: (AuthResult) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                // Trial check happens in ViewModel after OtpVerified
                onResult(AuthResult.OtpVerified(
                    TrialStatus(hasActiveTrial = false, trialUsed = false)
                ))
            }
            .addOnFailureListener { e ->
                onResult(AuthResult.Failure(
                    e.message ?: "Sign in failed. Please try again."
                ))
            }
    }

    private suspend fun signInWithPhoneCredentialSuspend(
        credential: PhoneAuthCredential,
        phoneNumber: String
    ): AuthResult {
        return try {
            auth.signInWithCredential(credential).await()
            val trialStatus = trialManager.checkAndGrantTrial("+91$phoneNumber")
            AuthResult.OtpVerified(trialStatus)
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Invalid OTP. Please try again.")
        }
    }

    // ── Gmail link ────────────────────────────────────────────────────────────

    suspend fun linkGmail(context: Context): AuthResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            handleGoogleCredential(result.credential)
        } catch (e: GetCredentialException) {
            AuthResult.Failure(e.message ?: "Google sign-in failed. Please try again.")
        }
    }

    private suspend fun handleGoogleCredential(
        credential: androidx.credentials.Credential
    ): AuthResult {
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) return AuthResult.Failure("Unsupported credential type.")

        return try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(
                googleIdTokenCredential.idToken, null
            )
            auth.currentUser
                ?.linkWithCredential(firebaseCredential)
                ?.await()
            AuthResult.GmailLinked
        } catch (_: FirebaseAuthUserCollisionException) {
            AuthResult.GmailLinked // Already linked — treat as success
        } catch (e: GoogleIdTokenParsingException) {
            AuthResult.Failure(e.message ?: "Invalid Google token. Please try again.")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Failed to link Google account.")
        }
    }
}