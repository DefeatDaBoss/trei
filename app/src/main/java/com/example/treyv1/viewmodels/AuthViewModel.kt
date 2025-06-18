package com.example.treyv1.viewmodels

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {
    // State variables
    private val _phone = mutableStateOf("")
    val phone: State<String> = _phone

    private val _otpCode = mutableStateOf("")
    val otpCode: State<String> = _otpCode

    private val _otpSent = mutableStateOf(false)
    val otpSent: State<Boolean> = _otpSent

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("") // NEW: For error feedback
    val errorMessage: State<String> = _errorMessage

    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var activity: Activity

    // Phone number updates
    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
        _errorMessage.value = "" // Clear error when typing
    }

    // OTP code updates
    fun updateOtp(code: String) {
        _otpCode.value = code
        _errorMessage.value = "" // Clear error when typing
    }

    // Send OTP - IMPROVED VERSION
    fun sendOtp() {
        // 1. Validate input
        val rawPhone = _phone.value.trim()
        if (rawPhone.isEmpty()) {
            _errorMessage.value = "Please enter a phone number"
            return
        }

        // 2. Format phone number (E.164 format)
        val formattedPhone = formatPhoneNumber(rawPhone)
        if (formattedPhone == null) {
            _errorMessage.value = "Invalid phone number format. Include country code."
            return
        }

        // 3. Check activity reference
        if (!::activity.isInitialized) {
            _errorMessage.value = "System error. Please restart the app."
            return
        }

        // 4. Start verification process
        _isLoading.value = true
        _errorMessage.value = ""

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    _isLoading.value = false
                    Log.d("Auth", "Auto-verification completed")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isLoading.value = false
                    val errorMsg = when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format"
                        else -> "Verification failed: ${e.localizedMessage}"
                    }
                    _errorMessage.value = errorMsg
                    Log.e("Auth", "Verification failed", e)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _isLoading.value = false
                    this@AuthViewModel.verificationId = verificationId
                    resendToken = token
                    _otpSent.value = true
                    Log.d("Auth", "OTP sent successfully")
                }
            })
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Failed to start verification: ${e.localizedMessage}"
            Log.e("Auth", "Verification initialization failed", e)
        }
    }

    // Verify OTP - IMPROVED VERSION
    fun verifyOtp() {
        if (_otpCode.value.isEmpty()) {
            _errorMessage.value = "Please enter the OTP code"
            return
        }

        if (verificationId == null) {
            _errorMessage.value = "Verification session expired. Please request a new OTP."
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, _otpCode.value)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Invalid OTP format"
            Log.e("Auth", "OTP verification failed", e)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    Log.d("Auth", "Sign-in successful")
                    // Handle successful login here
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error occurred"
                    _errorMessage.value = "Authentication failed: $errorMsg"
                    Log.e("Auth", "Sign-in failed", task.exception)
                }
            }
    }

    // Helper function to format phone number
    private fun formatPhoneNumber(rawPhone: String): String? {
        // Remove all non-digit characters except '+'
        val cleanNumber = rawPhone.replace("[^+0-9]".toRegex(), "")

        return when {
            cleanNumber.matches(Regex("^\\+[0-9]{10,15}$")) -> cleanNumber
            cleanNumber.matches(Regex("^[0-9]{10,15}$")) -> "+$cleanNumber"
            else -> null
        }
    }
}