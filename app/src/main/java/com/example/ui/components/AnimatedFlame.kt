package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseAccent

@Composable
fun AnimatedFlame(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isActive: Boolean = true,
    reducedMotion: Boolean = false,
    flameStyle: String = "flame_classic"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_transition")

    val pulseScale by if (reducedMotion || !isActive) {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "scale"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flame_pulse"
        )
    }

    val swayAngle by if (reducedMotion || !isActive) {
        rememberInfiniteTransition(label = "static_sway").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "sway"
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flame_sway"
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            val (flameOuterColors, glowColor) = when (flameStyle) {
                "flame_violet_inferno" -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF6B21A8)) to Color(0xFFA855F7)
                "flame_cyan_plasma" -> listOf(Color(0xFFE0F2FE), Color(0xFF06B6D4), Color(0xFF0369A1)) to Color(0xFF06B6D4)
                "flame_emerald_mystic" -> listOf(Color(0xFFD1FAE5), Color(0xFF10B981), Color(0xFF047857)) to Color(0xFF10B981)
                "flame_golden_sun" -> listOf(Color(0xFFFEF3C7), Color(0xFFFBBF24), Color(0xFFD97706)) to Color(0xFFFBBF24)
                else -> listOf(GoldAccent, FlameOrange, RoseAccent) to FlameOrange
            }

            // Outer Aura Glow
            if (isActive) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.35f * pulseScale),
                            glowColor.copy(alpha = 0.15f * pulseScale),
                            Color.Transparent
                        ),
                        center = Offset(w / 2f, h * 0.6f),
                        radius = w * 0.65f * pulseScale
                    )
                )
            }

            // Outer Flame Body
            val outerPath = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                cubicTo(w * 0.82f, h * 0.32f, w * 0.95f, h * 0.65f, w * 0.75f, h * 0.92f)
                cubicTo(w * 0.65f, h * 1.0f, w * 0.35f, h * 1.0f, w * 0.25f, h * 0.92f)
                cubicTo(w * 0.05f, h * 0.65f, w * 0.18f, h * 0.32f, w * 0.5f, h * 0.05f)
                close()
            }

            drawPath(
                path = outerPath,
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        flameOuterColors
                    } else {
                        listOf(Color(0xFF64748B), Color(0xFF334155))
                    }
                )
            )

            // Inner Core Flame
            if (isActive) {
                val innerPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.32f)
                    cubicTo(w * 0.68f, h * 0.52f, w * 0.72f, h * 0.72f, w * 0.62f, h * 0.88f)
                    cubicTo(w * 0.55f, h * 0.94f, w * 0.45f, h * 0.94f, w * 0.38f, h * 0.88f)
                    cubicTo(w * 0.28f, h * 0.72f, w * 0.32f, h * 0.52f, w * 0.5f, h * 0.32f)
                    close()
                }

                drawPath(
                    path = innerPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, flameOuterColors.first(), flameOuterColors[1])
                    )
                )
            }
        }
    }
}
