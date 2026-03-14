package com.stocksense.app.feature.auth.presentation

import android.app.Activity
import androidx.credentials.Credential
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object OtpVerified : AuthState()
    object GmailLinked : AuthState()
    data class Error(val message: String) : AuthState()
}

@Suppress("SpellCheckingInspection")
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // ── REMOVED: _phoneNumber, phoneNumber, updatePhoneNumber
    // Phone number travels as a NavGraph argument — no need to store it here

    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(
                        e.message ?: "Verification failed. Please try again."
                    )
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    _authState.value = AuthState.OtpSent
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val verificationId = storedVerificationId ?: run {
            _authState.value = AuthState.Error("Session expired. Please try again.")
            return
        }
        _authState.value = AuthState.Loading
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    fun linkGmailWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
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
                // Use e.message for the actual failure reason from the OS
                _authState.value = AuthState.Error(
                    e.message ?: "Google sign-in failed. Please try again."
                )
            }
        }
    }

    private fun handleGoogleCredential(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken, null
                )
                auth.currentUser
                    ?.linkWithCredential(firebaseCredential)
                    ?.addOnSuccessListener {
                        _authState.value = AuthState.GmailLinked
                    }
                    ?.addOnFailureListener { e ->
                        if (e is FirebaseAuthUserCollisionException) {
                            // Already linked — treat as success
                            _authState.value = AuthState.GmailLinked
                        } else {
                            _authState.value = AuthState.Error(
                                e.message ?: "Failed to link Google account."
                            )
                        }
                    }
            } catch (e: GoogleIdTokenParsingException) {
                // Use e.message for the actual parse failure reason
                _authState.value = AuthState.Error(
                    e.message ?: "Invalid Google token. Please try again."
                )
            }
        } else {
            _authState.value = AuthState.Error("Unsupported credential type.")
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            auth.signInWithCredential(credential)
                .addOnSuccessListener { _authState.value = AuthState.OtpVerified }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "Invalid OTP. Please try again."
                    )
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        private const val WEB_CLIENT_ID =
            "855241433770-gb9hk6pufh9mtsfuojqribc3jert5fs5.apps.googleusercontent.com"
    }
}