package com.example.ui.animation

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
/ Checks if the user enabled "Remove Animations" or set animator scale to 0 in system settings.
 */
fun isSystemAnimationDisabled(context: Context): Boolean {
    return try {
        val durationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        durationScale == 0.0f
    } catch (e: Exception) {
        false
    }
}

/**
 * Standard slide-in + fade transition for screen navigation.
 */
fun slideInRightFade(durationMs: Int = 300): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMs, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(durationMs))
}

fun slideOutRightFade(durationMs: Int = 200): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMs, easing = FastOutLinearInEasing)
    ) + fadeOut(animationSpec = tween(durationMs))
}

/**
 * Reusable wrapper for Staggered Fade-in & Slide-up animation on cards or list items.
 */
@Composable
fun StaggeredListItem(
    index: Int,
    baseDelayMs: Int = 50,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val animationsDisabled = remember { isSystemAnimationDisabled(context) }

    if (animationsDisabled) {
        Box(modifier = modifier) { content() }
        return
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * baseDelayMs).toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Animated Checkmark drawing vector path for action success feedback (e.g., download complete, file saved).
 */
@Composable
fun AnimatedCheckmark(
    size: Dp = 48.dp,
    color: Color = Color(0xFF4CAF50),
    strokeWidth: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsDisabled = remember { isSystemAnimationDisabled(context) }

    val progress = remember { Animatable(if (animationsDisabled) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!animationsDisabled) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
            )
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val path = Path().apply {
            moveTo(this@Canvas.size.width * 0.2f, this@Canvas.size.height * 0.5f)
            lineTo(this@Canvas.size.width * 0.45f, this@Canvas.size.height * 0.75f)
            lineTo(this@Canvas.size.width * 0.8f, this@Canvas.size.height * 0.25f)
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(path, false)
        val pathLength = pathMeasure.length

        val animatedPath = Path()
        pathMeasure.getSegment(0f, pathLength * progress.value, animatedPath, true)

        drawPath(
            path = animatedPath,
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}
