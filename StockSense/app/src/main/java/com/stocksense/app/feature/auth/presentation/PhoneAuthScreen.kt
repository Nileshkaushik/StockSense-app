package com.stocksense.app.feature.auth.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stocksense.app.core.ui.theme.*

@Composable
fun PhoneAuthScreen(
    onOtpSent: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val authState by viewModel.authState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f, targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (phoneNumber.length == 10) 1f else 0.98f,
        animationSpec = tween(200), label = "scale"
    )

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.OtpSent -> { onOtpSent(phoneNumber); viewModel.resetState() }
            is AuthState.Error -> errorMessage = (authState as AuthState.Error).message
            else -> {}
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {

        // Ambient glow blob
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.TopStart)
                .graphicsLayer { translationX = -120f; translationY = 80f }
                .blur(100.dp)
                .background(
                    Brush.radialGradient(listOf(AccentGreen.copy(glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AccentGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("↗", color = BackgroundDark, fontSize = 17.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(10.dp))
                Text("StockSense", color = TextPrimary, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
            }

            Spacer(Modifier.height(48.dp))

            // Step indicator
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(AccentGreen))
                Text("STEP 1 OF 3", color = AccentGreen, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.height(12.dp))

            // Progress bar
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.width(32.dp).height(2.5.dp).clip(RoundedCornerShape(2.dp)).background(AccentGreen))
                Box(Modifier.width(10.dp).height(2.5.dp).clip(RoundedCornerShape(2.dp)).background(CardBorder))
                Box(Modifier.width(10.dp).height(2.5.dp).clip(RoundedCornerShape(2.dp)).background(CardBorder))
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Enter your\nmobile number",
                color = TextPrimary, fontSize = 34.sp,
                fontWeight = FontWeight.Bold, lineHeight = 40.sp, letterSpacing = (-1.2).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "We'll send a one-time code to verify it's you.",
                color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
            )

            Spacer(Modifier.height(36.dp))

            Text("MOBILE NUMBER", color = TextSecondary, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(10.dp))

            // Phone input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(
                        1.5.dp,
                        if (phoneNumber.isNotEmpty()) AccentGreen else CardBorder,
                        RoundedCornerShape(16.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                    Text("🇮🇳  +91", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
                Box(Modifier.width(1.dp).height(22.dp).background(CardBorder))
                TextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 10) {
                            phoneNumber = it.filter { c -> c.isDigit() }
                            errorMessage = ""
                        }
                    },
                    placeholder = { Text("98765 43210", color = TextMuted, fontSize = 16.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AccentGreen
                    ),
                    textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp, color = TextPrimary),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        Text("${phoneNumber.length}/10",
                            color = if (phoneNumber.length == 10) AccentGreen else TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 12.dp))
                    }
                )
            }

            // Error
            AnimatedVisibility(errorMessage.isNotEmpty(),
                enter = fadeIn() + slideInVertically { -it }, exit = fadeOut()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LossRed.copy(0.08f))
                        .border(1.dp, LossRed.copy(0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠", fontSize = 12.sp)
                    Text(errorMessage, color = LossRed, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info cards
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🔒", fontSize = 18.sp)
                        Text("Your number is never shared", color = TextSecondary,
                            fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(AccentGreenDim)
                        .border(1.dp, AccentGreen.copy(0.2f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("🎁", fontSize = 18.sp)
                        Text("3 days free Premium", color = AccentGreen,
                            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 15.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (phoneNumber.length == 10) {
                        errorMessage = ""
                        viewModel.sendOtp(phoneNumber, activity)
                    } else {
                        errorMessage = "Please enter a valid 10-digit number"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
                    .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    disabledContainerColor = AccentGreen.copy(0.2f)
                ),
                enabled = phoneNumber.length == 10 && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = BackgroundDark,
                        modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                } else {
                    Text("Send OTP  →", color = BackgroundDark, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "By continuing you agree to our Terms & Privacy Policy",
                color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}