package com.stocksense.app.feature.auth.presentation

import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.getSystemService
import com.stocksense.app.core.ui.theme.*

@Composable
fun PermissionSetupScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current

    // ── Permission states ─────────────────────────────────────────────────────

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true // Pre-Android 13 — notifications always on
        )
    }

    var batteryUnrestricted by remember {
        mutableStateOf(
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(context.packageName) == true
        )
    }

    // ── Launchers ─────────────────────────────────────────────────────────────

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        batteryUnrestricted = context.getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    // ── Animations ────────────────────────────────────────────────────────────

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f, targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        cardsVisible = true
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    Box(Modifier.fillMaxSize().background(BackgroundDark)) {

        // Glow — top center
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer { translationY = -80f }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(72.dp))

            // App icon — larger than on auth screens, this is a celebration moment
            Box(
                Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AccentGreen),
                contentAlignment = Alignment.Center
            ) {
                Text("↗", color = BackgroundDark, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(22.dp))

            Text(
                "Almost there",
                color = TextPrimary, fontSize = 34.sp,
                fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Two quick permissions so StockSense\ncan alert you the instant a price moves.",
                color = TextSecondary, fontSize = 14.sp,
                lineHeight = 20.sp, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // ── Permission cards ──────────────────────────────────────────────

            AnimatedVisibility(
                visible = cardsVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Card 1 — Notifications
                    PermissionCard(
                        icon = "🔔",
                        title = "Price alert notifications",
                        subtitle = "Notifies you the instant a price target is hit, even when the app is closed.",
                        isGranted = notificationGranted,
                        grantedLabel = "Notifications enabled",
                        actionLabel = "Allow notifications",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        }
                    )

                    // Card 2 — Battery optimization
                    PermissionCard(
                        icon = "⚡",
                        title = "Background activity",
                        subtitle = "Prevents Android from pausing the price monitor. Set StockSense to 'Unrestricted' in the next screen.",
                        isGranted = batteryUnrestricted,
                        grantedLabel = "Running unrestricted",
                        actionLabel = "Open battery settings",
                        onAction = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            batteryLauncher.launch(intent)
                        }
                    )

                    // Info strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("ℹ", fontSize = 13.sp)
                        Text(
                            "Used only for price alerts. No personal data is shared with third parties.",
                            color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── CTA button — label adapts to state ───────────────────────────

            val buttonLabel = when {
                !notificationGranted -> "Skip for now  →"
                !batteryUnrestricted -> "Continue anyway  →"
                else                 -> "Start tracking  →"
            }
            val buttonContainerColor = if (notificationGranted) AccentGreen else SurfaceDark
            val buttonTextColor = if (notificationGranted) BackgroundDark else TextSecondary

            Button(
                onClick = { onComplete() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor
                ),
                border = if (!notificationGranted)
                    androidx.compose.foundation.BorderStroke(1.5.dp, CardBorder)
                else null
            ) {
                Text(
                    buttonLabel,
                    color = buttonTextColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Contextual hint — changes color based on state
            Text(
                when {
                    !notificationGranted  -> "You can enable notifications later in Settings"
                    !batteryUnrestricted  -> "Battery setting recommended for reliable alerts"
                    else                  -> "All set — alerts will fire instantly"
                },
                color = when {
                    !notificationGranted  -> TextMuted
                    !batteryUnrestricted  -> WarningAmber.copy(alpha = 0.8f)
                    else                  -> AccentGreen.copy(alpha = 0.8f)
                },
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ── Reusable permission card ──────────────────────────────────────────────────
@Composable
private fun PermissionCard(
    icon: String,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    grantedLabel: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isGranted) AccentGreen.copy(alpha = 0.4f) else CardBorder,
        animationSpec = tween(400), label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isGranted) AccentGreenDim else SurfaceDark,
        animationSpec = tween(400), label = "bg"
    )
    val iconBgColor by animateColorAsState(
        targetValue = if (isGranted) AccentGreenMid else CardDark,
        animationSpec = tween(400), label = "iconBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon bubble
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Title row + granted badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    title, color = TextPrimary,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
                AnimatedVisibility(
                    visible = isGranted,
                    enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                    exit = fadeOut(tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(AccentGreen)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "✓", color = BackgroundDark,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                subtitle, color = TextSecondary,
                fontSize = 12.sp, lineHeight = 17.sp
            )

            Spacer(Modifier.height(8.dp))

            // Action — animated swap between button and confirmed label
            AnimatedContent(
                targetState = isGranted,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                label = "cardAction"
            ) { granted ->
                if (granted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape).background(AccentGreen)
                        )
                        Text(
                            grantedLabel, color = AccentGreen,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onAction,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, AccentGreen.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Text(
                            actionLabel, color = AccentGreen,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}