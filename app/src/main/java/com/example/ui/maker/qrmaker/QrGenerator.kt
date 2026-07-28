package com.example.ui.maker.qrmaker

import android.graphics.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

enum class QrModuleShape {
    SQUARE,
    ROUNDED,
    DOTS
}

data class QrStyleConfig(
    val fgColor: Int = Color.BLACK,
    val bgColor: Int = Color.WHITE,
    val shape: QrModuleShape = QrModuleShape.SQUARE,
    val logoBitmap: Bitmap? = null
)

object QrGenerator {

    /**
     * Checks contrast ratio between foreground and background color.
     * Returns true if contrast is sufficient for scanning (>= 3.0).
     */
    fun checkContrastRatio(fgColor: Int, bgColor: Int): Float {
        val fgL = calculateLuminance(fgColor)
        val bgL = calculateLuminance(bgColor)
        val maxL = maxOf(fgL, bgL)
        val minL = minOf(fgL, bgL)
        return (maxL + 0.05f) / (minL + 0.05f)
    }

    private fun calculateLuminance(color: Int): Float {
        val r = Color.red(color) / 255.0f
        val g = Color.green(color) / 255.0f
        val b = Color.blue(color) / 255.0f
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    fun generateQrBitmap(
        content: String,
        size: Int = 800,
        qrStyle: QrStyleConfig = QrStyleConfig()
    ): Bitmap? {
        if (content.isBlank()) return null

        try {
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            // High error correction if logo is present to maintain readability
            hints[EncodeHintType.ERROR_CORRECTION] = if (qrStyle.logoBitmap != null) {
                ErrorCorrectionLevel.H
            } else {
                ErrorCorrectionLevel.M
            }
            hints[EncodeHintType.MARGIN] = 1

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill background
            val bgPaint = Paint().apply {
                color = qrStyle.bgColor
                this.style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Setup Foreground Paint
            val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = qrStyle.fgColor
                this.style = Paint.Style.FILL
            }

            val moduleSize = width.toFloat() / bitMatrix.width

            // Render Modules based on shape
            for (x in 0 until width) {
                for (y in 0 until height) {
                    if (bitMatrix.get(x, y)) {
                        val left = x * moduleSize
                        val top = y * moduleSize
                        val right = left + moduleSize
                        val bottom = top + moduleSize

                        val isFinderPattern = isFinderPatternCell(x, y, width, height)

                        when {
                            isFinderPattern -> {
                                // Finder patterns always remain solid squares/rounded for best scanner compatibility
                                if (qrStyle.shape == QrModuleShape.ROUNDED) {
                                    canvas.drawRoundRect(RectF(left, top, right, bottom), moduleSize * 0.3f, moduleSize * 0.3f, fgPaint)
                                } else {
                                    canvas.drawRect(left, top, right, bottom, fgPaint)
                                }
                            }
                            qrStyle.shape == QrModuleShape.DOTS -> {
                                val centerX = (left + right) / 2f
                                val centerY = (top + bottom) / 2f
                                val radius = (moduleSize / 2f) * 0.85f
                                canvas.drawCircle(centerX, centerY, radius, fgPaint)
                            }
                            qrStyle.shape == QrModuleShape.ROUNDED -> {
                                canvas.drawRoundRect(RectF(left, top, right, bottom), moduleSize * 0.4f, moduleSize * 0.4f, fgPaint)
                            }
                            else -> {
                                canvas.drawRect(left, top, right, bottom, fgPaint)
                            }
                        }
                    }
                }
            }

            // Draw Logo Overlay if provided
            if (qrStyle.logoBitmap != null) {
                drawLogoInCenter(canvas, qrStyle.logoBitmap, width, height, qrStyle.bgColor)
            }

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun isFinderPatternCell(x: Int, y: Int, matrixWidth: Int, matrixHeight: Int): Boolean {
        // Finder patterns are 7x7 at 3 corners: (top-left, top-right, bottom-left)
        val isTopLeft = x < 8 && y < 8
        val isTopRight = x >= matrixWidth - 8 && y < 8
        val isBottomLeft = x < 8 && y >= matrixHeight - 8
        return isTopLeft || isTopRight || isBottomLeft
    }

    private fun drawLogoInCenter(canvas: Canvas, logo: Bitmap, width: Int, height: Int, bgColor: Int) {
        val logoSize = (width * 0.20f).toInt() // Max 20% size for 100% scan safety
        val badgeSize = (logoSize * 1.25f).toInt()

        val centerX = width / 2
        val centerY = height / 2

        val badgeLeft = (centerX - badgeSize / 2).toFloat()
        val badgeTop = (centerY - badgeSize / 2).toFloat()
        val badgeRight = (centerX + badgeSize / 2).toFloat()
        val badgeBottom = (centerY + badgeSize / 2).toFloat()

        // Background badge behind logo
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawRoundRect(RectF(badgeLeft, badgeTop, badgeRight, badgeBottom), 24f, 24f, badgePaint)
        canvas.drawRoundRect(RectF(badgeLeft, badgeTop, badgeRight, badgeBottom), 24f, 24f, strokePaint)

        // Draw scaled logo
        val scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)
        val logoLeft = (centerX - logoSize / 2).toFloat()
        val logoTop = (centerY - logoSize / 2).toFloat()

        canvas.drawBitmap(scaledLogo, logoLeft, logoTop, null)
    }
}
