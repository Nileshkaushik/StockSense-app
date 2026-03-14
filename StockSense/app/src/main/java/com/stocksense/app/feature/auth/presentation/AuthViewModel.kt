package com.stocksense.app.feature.auth.presentation

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stocksense.app.feature.auth.data.AuthRepository
import com.stocksense.app.feature.auth.data.AuthResult
import com.stocksense.app.feature.auth.domain.TrialStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    data class OtpVerified(val trialStatus: TrialStatus) : AuthState()
    object GmailLinked : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        repository.sendOtp(phoneNumber, activity) { result ->
            _authState.value = when (result) {
                is AuthResult.OtpSent    -> AuthState.OtpSent
                is AuthResult.Failure    -> AuthState.Error(result.message)
                else                     -> AuthState.Error("Unexpected error.")
            }
        }
    }

    fun verifyOtp(otp: String, phoneNumber: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            _authState.value = when (val result = repository.verifyOtp(otp, phoneNumber)) {
                is AuthResult.OtpVerified -> AuthState.OtpVerified(result.trialStatus)
                is AuthResult.Failure     -> AuthState.Error(result.message)
                else                      -> AuthState.Error("Unexpected error.")
            }
        }
    }

    fun linkGmailWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            _authState.value = when (val result = repository.linkGmail(context)) {
                is AuthResult.GmailLinked -> AuthState.GmailLinked
                is AuthResult.Failure     -> AuthState.Error(result.message)
                else                      -> AuthState.Error("Unexpected error.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}