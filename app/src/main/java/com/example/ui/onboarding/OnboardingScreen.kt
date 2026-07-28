package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.StaggeredListItem
import kotlinx.coroutines.launch

data class OnboardingSlideData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val highlights: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlideData(
            title = "Selamat Datang di Super Tools",
            subtitle = "Pusat kendali serbaguna untuk seluruh kebutuhan digital Anda. Semua fitur terintegrasi dalam satu aplikasi yang cepat, ringkas, dan tanpa batasan.",
            icon = Icons.Outlined.BuildCircle,
            primaryColor = MaterialTheme.colorScheme.primary,
            highlights = listOf(
                "File Manager & Pembersih Penyimpanan",
                "Alat Pembuat & Pengolah Konten",
                "Fitur Asisten Kecerdasan Buatan (AI)"
            )
        ),
        OnboardingSlideData(
            title = "Kategori Tools Lengkap",
            subtitle = "Akses langsung berbagai alat seperti Kompres Gambar, Pengolah Video, Ekstraktor ZIP, Pembuat QR Code, hingga PDF Tools tanpa perlu pindah aplikasi.",
            icon = Icons.Outlined.FolderSpecial,
            primaryColor = Color(0xFF00897B),
            highlights = listOf(
                "File Tools & Pemindai Duplikat",
                "Image & Video Compressor",
                "Audio & PDF Converter"
            )
        ),
        OnboardingSlideData(
            title = "Pusat Asisten AI Cerdas",
            subtitle = "Didukung oleh model AI canggih untuk menjawab pertanyaan, membantu penulisan kode, analisis dokumen, hingga pembuatan gambar kreatif secara instan.",
            icon = Icons.Outlined.AutoAwesome,
            primaryColor = Color(0xFF7E57C2),
            highlights = listOf(
                "Chatbot AI & AI Agent Produktivitas",
                "Generator Gambar AI Otomatis",
                "Asisten Pendukung & Analis Data"
            )
        ),
        OnboardingSlideData(
            title = "Transparansi Izin & Privasi",
            subtitle = "Privasi Anda adalah prioritas utama. Seluruh proses pengolahan file berjalan secara lokal di perangkat tanpa pengiriman data tanpa izin.",
            icon = Icons.Outlined.Security,
            primaryColor = Color(0xFF2E7D32),
            highlights = listOf(
                "Akses Penyimpanan (File Manager)",
                "Izin Kamera (Pemindai QR)",
                "Izin Mikrofon (Pengolah Audio)"
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Handyman,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Super Tools",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                TextButton(
                    onClick = {
                        OnboardingPreferences.setOnboardingCompleted(context, true)
                        onFinishOnboarding()
                    }
                ) {
                    Text(
                        text = "Lewati",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expanding Dot Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(widthAnim)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) slides[pagerState.currentPage].primaryColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Next / Get Started Action Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < slides.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            OnboardingPreferences.setOnboardingCompleted(context, true)
                            onFinishOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = slides[pagerState.currentPage].primaryColor
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (pagerState.currentPage == slides.size - 1) "Mulai Sekarang" else "Lanjut",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (pagerState.currentPage == slides.size - 1) Icons.Filled.Check else Icons.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            val slide = slides[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Staggered Element 1: Hero Graphic Card
                StaggeredListItem(index = 0) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        slide.primaryColor.copy(alpha = 0.25f),
                                        slide.primaryColor.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(slide.primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = null,
                                tint = slide.primaryColor,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Staggered Element 2: Headline Title
                StaggeredListItem(index = 1) {
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Staggered Element 3: Subtitle Description
                StaggeredListItem(index = 2) {
                    Text(
                        text = slide.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Staggered Element 4: Key Feature Highlights
                StaggeredListItem(index = 3) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            slide.highlights.forEach { highlight ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = slide.primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = highlight,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
