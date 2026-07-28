package com.example.util

import android.util.Patterns
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Result model for generic field validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
)

/**
 * Levels of password strength
 */
enum class PasswordStrength(
    val label: String,
    val score: Float, // 0.0 to 1.0
    val color: Color
) {
    EMPTY("Kosong", 0f, Color.Gray),
    WEAK("Sangat Lemah", 0.2f, Color(0xFFFF3D00)),
    FAIR("Cukup", 0.4f, Color(0xFFFF9100)),
    GOOD("Baik", 0.7f, Color(0xFFFFC400)),
    STRONG("Kuat", 0.9f, Color(0xFF00E676)),
    VERY_STRONG("Sangat Kuat", 1.0f, Color(0xFF00B0FF))
}

/**
 * Detailed analysis of password complexity criteria
 */
data class PasswordAnalysis(
    val strength: PasswordStrength,
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean,
    val scorePercentage: Int,
    val suggestions: List<String>
)

/**
 * Reusable Input Validation Helper
 */
object InputValidationHelper {

    /**
     * Validates email format using Android Patterns or Regex
     */
    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(isValid = false, message = "Email tidak boleh kosong")
        }
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (!emailRegex.matches(trimmed)) {
            return ValidationResult(isValid = false, message = "Format email tidak valid (contoh: nama@domain.com)")
        }
        return ValidationResult(isValid = true, message = "Format email valid")
    }

    /**
     * Evaluates password strength and returns a comprehensive PasswordAnalysis
     */
    fun analyzePassword(password: String): PasswordAnalysis {
        if (password.isEmpty()) {
            return PasswordAnalysis(
                strength = PasswordStrength.EMPTY,
                hasMinLength = false,
                hasUppercase = false,
                hasLowercase = false,
                hasDigit = false,
                hasSpecialChar = false,
                scorePercentage = 0,
                suggestions = listOf("Masukkan kata sandi")
            )
        }

        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        var points = 0
        if (password.length >= 8) points += 20
        if (password.length >= 12) points += 15
        if (hasUppercase) points += 20
        if (hasLowercase) points += 15
        if (hasDigit) points += 15
        if (hasSpecialChar) points += 15

        val percentage = points.coerceAtMost(100)

        val strength = when {
            percentage < 25 -> PasswordStrength.WEAK
            percentage < 50 -> PasswordStrength.FAIR
            percentage < 70 -> PasswordStrength.GOOD
            percentage < 90 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }

        val suggestions = mutableListOf<String>()
        if (!hasMinLength) suggestions.add("Minimal 8 karakter")
        if (!hasUppercase) suggestions.add("Tambahkan huruf kapital (A-Z)")
        if (!hasLowercase) suggestions.add("Tambahkan huruf kecil (a-z)")
        if (!hasDigit) suggestions.add("Tambahkan angka (0-9)")
        if (!hasSpecialChar) suggestions.add("Tambahkan simbol (@, #, $, dll)")

        return PasswordAnalysis(
            strength = strength,
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecialChar,
            scorePercentage = percentage,
            suggestions = suggestions
        )
    }

    /**
     * Validates phone number pattern (Indonesian & International formats)
     */
    fun validatePhone(phone: String): ValidationResult {
        val cleanPhone = phone.replace("[\\s\\-\\(\\)]".toRegex(), "")
        if (cleanPhone.isEmpty()) {
            return ValidationResult(isValid = false, message = "Nomor telepon tidak boleh kosong")
        }

        // Format check: starts with +62, 62, or 08
        val isValidPattern = cleanPhone.matches("^(\\+?62|0)8[1-9][0-9]{7,11}$".toRegex())
        if (!isValidPattern) {
            return ValidationResult(
                isValid = false,
                message = "Format nomor telepon tidak valid (contoh: 08123456789 atau +628123456789)"
            )
        }

        if (cleanPhone.length < 10 || cleanPhone.length > 15) {
            return ValidationResult(isValid = false, message = "Panjang nomor telepon harus antara 10-15 digit")
        }

        return ValidationResult(isValid = true, message = "Nomor telepon valid")
    }

    /**
     * Validates username requirement
     */
    fun validateUsername(username: String): ValidationResult {
        val trimmed = username.trim()
        if (trimmed.length < 4) {
            return ValidationResult(isValid = false, message = "Username minimal 4 karakter")
        }
        if (!trimmed.matches("^[a-zA-Z0-9_]+$".toRegex())) {
            return ValidationResult(isValid = false, message = "Username hanya boleh menggunakan huruf, angka, dan underscore (_)")
        }
        return ValidationResult(isValid = true, message = "Username tersedia")
    }
}

/**
 * Reusable Compose Component: Password Strength Indicator Bar & Feedback
 */
@Composable
fun PasswordStrengthIndicator(
    analysis: PasswordAnalysis,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = analysis.strength.score,
        label = "strengthProgress"
    )
    val animatedColor by animateColorAsState(
        targetValue = analysis.strength.color,
        label = "strengthColor"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kekuatan Password:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${analysis.strength.label} (${analysis.scorePercentage}%)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

        // Animated Progress Bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Real-time criteria checklist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CriteriaBadge(label = "8+ Karakter", isMet = analysis.hasMinLength, modifier = Modifier.weight(1f))
            CriteriaBadge(label = "A-Z", isMet = analysis.hasUppercase, modifier = Modifier.weight(1f))
            CriteriaBadge(label = "a-z", isMet = analysis.hasLowercase, modifier = Modifier.weight(1f))
            CriteriaBadge(label = "0-9", isMet = analysis.hasDigit, modifier = Modifier.weight(1f))
            CriteriaBadge(label = "Simbol", isMet = analysis.hasSpecialChar, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CriteriaBadge(label: String, isMet: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = if (isMet) Color(0xFF00E676).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isMet) Color(0xFF00E676) else Color.Gray)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = if (isMet) FontWeight.Bold else FontWeight.Normal,
                color = if (isMet) Color(0xFF00E676) else Color.Gray
            )
        }
    }
}

/**
 * Reusable Helper for Field Validation Status Message
 */
@Composable
fun ValidationFeedbackText(
    result: ValidationResult,
    modifier: Modifier = Modifier
) {
    if (result.message.isNullOrEmpty()) return

    Row(
        modifier = modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (result.isValid) Color(0xFF00E676) else Color(0xFFFF3D00),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = result.message,
            fontSize = 11.sp,
            color = if (result.isValid) Color(0xFF00E676) else Color(0xFFFF3D00),
            fontWeight = FontWeight.Medium
        )
    }
}
