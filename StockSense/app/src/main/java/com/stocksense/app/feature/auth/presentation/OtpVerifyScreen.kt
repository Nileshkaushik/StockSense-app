package com.stocksense.app.feature.auth.presentation

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.delay

@Composable
fun OtpVerifyScreen(
    phoneNumber: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    var otpValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var timerSeconds by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // Digit scale animations
    val digitScales = remember { List(6) { Animatable(1f) } }

    LaunchedEffect(canResend) {
        if (!canResend) {
            timerSeconds = 30
            while (timerSeconds > 0) { delay(1000); timerSeconds-- }
            canResend = true
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.OtpVerified -> { onVerified(); viewModel.resetState() }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                otpValue = ""
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
    }

    // Animate each digit box when filled
    LaunchedEffect(otpValue) {
        val index = otpValue.length - 1
        if (index in 0..5) {
            digitScales[index].animateTo(
                1.12f, animationSpec = tween(80)
            )
            digitScales[index].animateTo(
                1f, animationSpec = tween(80)
            )
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {

        // Ambient glow
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 120f; translationY = -60f }
                .blur(90.dp)
                .background(
                    Brush.radialGradient(listOf(AccentGreen.copy(glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(60.dp))

            // Back
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("←", color = TextSecondary, fontSize = 14.sp)
                Text("Back", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))

            // Step + progress
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(AccentGreen))
                Text("STEP 2 OF 3", color = AccentGreen, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(2) {
                    Box(Modifier.width(32.dp).height(2.5.dp).clip(RoundedCornerShape(2.dp)).background(AccentGreen))
                }
                Box(Modifier.width(10.dp).height(2.5.dp).clip(RoundedCornerShape(2.dp)).background(CardBorder))
            }

            Spacer(Modifier.height(28.dp))

            Text("Verify your\nnumber", color = TextPrimary, fontSize = 34.sp,
                fontWeight = FontWeight.Bold, lineHeight = 40.sp, letterSpacing = (-1.2).sp)

            Spacer(Modifier.height(24.dp))

            // Phone chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(SurfaceDark)
                    .border(1.dp, CardBorder, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(AccentGreen))
                Text(
                    "+91 ${phoneNumber.chunked(5).joinToString(" ")}",
                    color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium
                )
                Text("·", color = CardBorder, fontSize = 14.sp)
                Text("Change", color = AccentGreen, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBack() })
            }

            Spacer(Modifier.height(32.dp))

            Text("ENTER OTP", color = TextSecondary, fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(14.dp))

            // OTP boxes — tap anywhere to focus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource()
                        }
                    ) { focusRequester.requestFocus() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    repeat(6) { index ->
                        val digit = otpValue.getOrNull(index)?.toString() ?: ""
                        val isFilled = digit.isNotEmpty()
                        val isActive = index == otpValue.length && authState !is AuthState.Loading

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .graphicsLayer {
                                    scaleX = digitScales[index].value
                                    scaleY = digitScales[index].value
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isFilled -> AccentGreenDim
                                        else -> CardDark
                                    }
                                )
                                .border(
                                    width = if (isActive) 2.dp else 1.5.dp,
                                    color = when {
                                        isFilled -> AccentGreen.copy(0.7f)
                                        isActive -> AccentGreen
                                        else -> CardBorder
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isFilled) {
                                Text(
                                    digit,
                                    color = AccentGreen,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            } else if (isActive) {
                                // Blinking cursor
                                val cursorAlpha by rememberInfiniteTransition(label = "cursor")
                                    .animateFloat(
                                        initialValue = 1f, targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(500), repeatMode = RepeatMode.Reverse
                                        ), label = "cursor"
                                    )
                                Box(
                                    Modifier.width(2.dp).height(26.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .graphicsLayer { alpha = cursorAlpha }
                                        .background(AccentGreen)
                                )
                            }
                        }
                    }
                }

                // Hidden capture field
                TextField(
                    value = otpValue,
                    onValueChange = {
                        if (it.length <= 6 && authState !is AuthState.Loading) {
                            otpValue = it.filter { c -> c.isDigit() }
                            errorMessage = ""
                            if (otpValue.length == 6) {
                                viewModel.verifyOtp(otpValue)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Transparent,
                        unfocusedTextColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer { alpha = 0.01f }
                        .focusRequester(focusRequester)
                )
            }

            // Error
            AnimatedVisibility(errorMessage.isNotEmpty(),
                enter = fadeIn() + slideInVertically { -it }, exit = fadeOut()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
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

            Spacer(Modifier.height(24.dp))

            // Resend row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (canResend) {
                    Text("Didn't get it? ", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        "Resend OTP",
                        color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            canResend = false
                            otpValue = ""
                            errorMessage = ""
                            viewModel.sendOtp(phoneNumber, context as Activity)
                        }
                    )
                } else {
                    Text("Resend in ", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        "0:${timerSeconds.toString().padStart(2, '0')}",
                        color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Verify button
            Button(
                onClick = {
                    if (otpValue.length == 6) viewModel.verifyOtp(otpValue)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    disabledContainerColor = AccentGreen.copy(0.2f)
                ),
                enabled = otpValue.length == 6 && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(color = BackgroundDark,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                        Text("Verifying...", color = BackgroundDark, fontSize = 15.sp,
                            fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Verify & Continue  →", color = BackgroundDark, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}