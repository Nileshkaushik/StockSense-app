package com.stocksense.app.feature.auth.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stocksense.app.core.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onLoggedIn: () -> Unit,
    onGmailPending: () -> Unit,
    onNotLoggedIn: () -> Unit
) {
    // FirebaseAuth.getInstance() is a singleton — safe to call directly here,
    // no ViewModel needed for a one-shot read on startup
    val auth = FirebaseAuth.getInstance()

    // ── Entrance animations ───────────────────────────────────────────────────
    val logoScale    = remember { Animatable(0.65f) }
    val logoAlpha    = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentSlide = remember { Animatable(24f) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f, targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    LaunchedEffect(Unit) {
        // Logo pops in
        launch {
            logoScale.animateTo(
                1f,
                animationSpec = tween(520, easing = EaseOutBack)
            )
        }
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(380))
        }

        // App name slides up slightly after logo
        delay(260)
        launch {
            contentAlpha.animateTo(1f, animationSpec = tween(420))
        }
        launch {
            contentSlide.animateTo(0f, animationSpec = tween(420, easing = EaseOutQuart))
        }

        // Minimum display time — feels intentional, not a flash
        delay(1100)

        // ── Auth state check ──────────────────────────────────────────────────
        val currentUser = auth.currentUser
        when {
            currentUser == null -> onNotLoggedIn()

            // Phone verified but dropped off before linking Gmail
            currentUser.providerData.none {
                it.providerId == GoogleAuthProvider.PROVIDER_ID
            } -> onGmailPending()

            // Fully authenticated — straight to Dashboard
            else -> onLoggedIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {

        // Central ambient glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .blur(85.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentGreen.copy(glowAlpha), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        // ── Logo + name ───────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // App icon — bigger than on auth screens (72dp), this is the hero moment
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha  = logoAlpha.value
                    }
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AccentGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "↗",
                    color = BackgroundDark,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // App name + tagline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.graphicsLayer {
                    alpha      = contentAlpha.value
                    translationY = contentSlide.value
                }
            ) {
                Text(
                    "StockSense",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )
                Text(
                    "Real-time portfolio tracker",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    letterSpacing = 0.3.sp
                )
            }
        }

        // ── Version number — bottom of screen ────────────────────────────────
        Text(
            "v1.0.0",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
                .graphicsLayer { alpha = contentAlpha.value }
        )
    }
}