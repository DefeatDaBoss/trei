package com.example.treyv1.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.treyv1.viewmodels.AuthViewModel

@Composable
fun PhoneAuthScreen(viewModel: AuthViewModel) {
    val phoneState = viewModel.phone.value
    val otpCodeState = viewModel.otpCode.value
    val otpSentState = viewModel.otpSent.value
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()) {

        Text(
            text = "Phone Authentication",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Error message display
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Phone number field
        OutlinedTextField(
            value = phoneState,
            onValueChange = { viewModel.updatePhone(it) },
            label = { Text("Phone Number (+CountryCodeNumber)") },
            placeholder = { Text("e.g., +1234567890") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = !isLoading && !otpSentState
        )

        if (!otpSentState) {
            // Send OTP button
            Button(
                onClick = { viewModel.sendOtp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                enabled = !isLoading && phoneState.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp))
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // OTP input field
            OutlinedTextField(
                value = otpCodeState,
                onValueChange = { viewModel.updateOtp(it) },
                label = { Text("OTP Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !isLoading
            )

            // Verify OTP button
            Button(
                onClick = { viewModel.verifyOtp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = !isLoading && otpCodeState.length >= 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp))
                } else {
                    Text("Verify OTP")
                }
            }
        }
    }
}