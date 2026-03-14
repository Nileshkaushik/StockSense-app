package com.stocksense.app.feature.auth.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
    // ← FIXED import
import com.stocksense.app.core.ui.theme.*

@Composable
fun LinkGmailScreen(
    onLinked: () -> Unit,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    var errorMessage by remember { mutableStateOf("") }

    // Pulsing glow — bottom-left this time for variety across the 3-screen flow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    // Button entrance scale
    val buttonScale by animateFloatAsState(
        targetValue = if (authState !is AuthState.Loading) 1f else 0.97f,
        animationSpec = tween(200), label = "btnScale"
    )

    // Benefit card entrance
    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        cardsVisible = true
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.GmailLinked -> {
                onLinked()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {

        // Ambient glow — bottom-left
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { translationX = -80f; translationY = 80f }
                .blur(90.dp)
                .background(
                    Brush.radialGradient(listOf(AccentGreen.copy(glowAlpha), Color.Transparent)),
                    CircleShape
                )
        )
        // Secondary glow — top-right, subtle
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 60f; translationY = -60f }
                .blur(80.dp)
                .background(
                    Brush.radialGradient(listOf(AccentGreen.copy(glowAlpha * 0.5f), Color.Transparent)),
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

            // Step indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(AccentGreen))
                Text(
                    "STEP 3 OF 3", color = AccentGreen, fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp
                )
            }
            Spacer(Modifier.height(10.dp))

            // Progress — all 3 bars filled
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) {
                    Box(
                        Modifier
                            .width(32.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentGreen)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Link your\nGoogle account",
                color = TextPrimary, fontSize = 34.sp,
                fontWeight = FontWeight.Bold, lineHeight = 40.sp,
                letterSpacing = (-1.2).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "One last step — connects your profile and keeps things safe.",
                color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            // ── "Why Gmail?" benefit cards ────────────────────────────────────
            AnimatedVisibility(
                visible = cardsVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    BenefitCard(
                        icon = "🔑",
                        title = "Account recovery",
                        subtitle = "Lose your SIM? Recover access via Gmail instantly"
                    )
                    BenefitCard(
                        icon = "☁️",
                        title = "Backup & sync",
                        subtitle = "Your portfolio backs up to Drive automatically"
                    )
                    BenefitCard(
                        icon = "🔒",
                        title = "Two-factor security",
                        subtitle = "Phone + Gmail = strongest account protection"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Error card ────────────────────────────────────────────────────
            AnimatedVisibility(
                errorMessage.isNotEmpty(),
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(LossRed.copy(0.08f))
                        .border(1.dp, LossRed.copy(0.25f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⚠", fontSize = 12.sp)
                    Text(errorMessage, color = LossRed, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Google Sign-In button ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer { scaleX = buttonScale; scaleY = buttonScale }
            ) {
                Button(
                    onClick = {
                        if (authState !is AuthState.Loading) {
                            errorMessage = ""
                            viewModel.linkGmailWithGoogle(context)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceDark,
                        disabledContainerColor = SurfaceDark
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = if (authState is AuthState.Loading)
                            CardBorder else AccentGreen.copy(0.5f)
                    ),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = AccentGreen,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                "Connecting...", color = TextSecondary,
                                fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Google "G" — colored dots forming the logo feel
                            GoogleGLogo()
                            Text(
                                "Continue with Google", color = TextPrimary,
                                fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // No skip allowed — blueprint rule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Google linking is required to continue",
                    color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ── Reusable benefit card ─────────────────────────────────────────────────────
@Composable
private fun BenefitCard(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon bubble
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AccentGreenDim),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title, color = TextPrimary,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle, color = TextSecondary,
                fontSize = 12.sp, lineHeight = 16.sp
            )
        }
    }
}

// ── Google G logo — drawn from shapes, no asset needed ───────────────────────
@Composable
private fun GoogleGLogo() {
    // Four-colored arc segments spelling the Google G identity
    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Blue
            drawArc(
                color = Color(0xFF4285F4),
                startAngle = -90f, sweepAngle = 90f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
            )
            // Red
            drawArc(
                color = Color(0xFFEA4335),
                startAngle = 180f, sweepAngle = 90f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
            )
            // Yellow
            drawArc(
                color = Color(0xFFFBBC04),
                startAngle = 90f, sweepAngle = 90f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
            )
            // Green
            drawArc(
                color = Color(0xFF34A853),
                startAngle = 0f, sweepAngle = 90f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
            )
            // Blue inner shelf (the horizontal bar of the G)
            drawLine(
                color = Color(0xFF4285F4),
                start = androidx.compose.ui.geometry.Offset(cx, cy),
                end = androidx.compose.ui.geometry.Offset(size.width, cy),
                strokeWidth = 3.5.dp.toPx()
            )
        }
    }
}