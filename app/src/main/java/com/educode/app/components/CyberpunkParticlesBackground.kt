package com.educode.app.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speedX: Float,
    var speedY: Float,
    var color: Color,
    var alpha: Float
)

@Composable
fun CyberpunkParticlesBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 1f,
                speedX = (Random.nextFloat() - 0.5f) * 0.015f,
                speedY = (Random.nextFloat() - 0.5f) * 0.015f,
                color = if (Random.nextBoolean()) Color(0xFF00E5FF) else Color(0xFF6B21A8),
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }

    var frameTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (isActive) {
            withFrameNanos { time ->
                val dt = ((time - lastTime) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastTime = time

                particles.forEach { p ->
                    p.x += p.speedX * dt
                    p.y += p.speedY * dt

                    if (p.x < 0f) p.x += 1f
                    if (p.x > 1f) p.x -= 1f
                    if (p.y < 0f) p.y += 1f
                    if (p.y > 1f) p.y -= 1f
                }
                frameTime = time
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = frameTime // Bind frameTime in draw phase to trigger draw-only invalidation
        val w = size.width
        val h = size.height
        
        for (p in particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x * w, p.y * h)
            )
        }
    }
}
