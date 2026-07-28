package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.category.PlaceholderScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.downloader.DownloaderScreen
import com.example.ui.maker.MakerScreen
import com.example.ui.maker.qrmaker.QrMakerScreen
import com.example.ui.maker.textmaker.TextMakerScreen
import com.example.ui.ai.AiMakerScreen
import com.example.ui.aiagent.AiAgentScreen
import com.example.ui.serverdata.ServerDataScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.backupsync.BackupSyncScreen
import com.example.ui.compliance.ComplianceScreen
import com.example.ui.apimanager.ApiManagerScreen
import com.example.ui.donation.DonationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.tools.ToolsScreen

import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.onboarding.SplashScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
      }
    }
  }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                onNavigateToCategory = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable("tools") {
            ToolsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTool = { route -> navController.navigate(route) }
            )
        }
        composable("device_tools") {
            com.example.ui.devicetools.DeviceToolsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("security_system") {
            com.example.ui.security.SecuritySystemScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("login_system") {
            com.example.ui.auth.LoginSystemScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("compliance") {
            com.example.ui.security.SecuritySystemScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("file_manager") {
            com.example.ui.filemanager.FileManagerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTool = { route -> navController.navigate(route) }
            )
        }
        composable("tool_word_counter") {
            com.example.ui.tools.text.WordCounterScreen(onBack = { navController.popBackStack() })
        }
        composable("aiassistant") {
            com.example.ui.aiassistant.AiAssistantScreen(onBack = { navController.popBackStack() })
        }
        composable("downloader") {
            DownloaderScreen(onBack = { navController.popBackStack() })
        }
        composable("maker") {
            MakerScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> 
                    try {
                        navController.navigate(route)
                    } catch (e: IllegalArgumentException) {
                        navController.navigate("maker_placeholder")
                    }
                }
            )
        }
        composable("qr_maker") {
            QrMakerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToScanner = { navController.navigate("qr_scanner") }
            )
        }
        composable("qr_scanner") {
            com.example.ui.maker.qrmaker.QrScannerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("text_maker") {
            TextMakerScreen(onBack = { navController.popBackStack() })
        }
        composable("maker_placeholder") {
            PlaceholderScreen(title = "Maker Feature", onBack = { navController.popBackStack() })
        }
        composable("apimanager") {
            ApiManagerScreen(onBack = { navController.popBackStack() })
        }
        composable("donation") {
            DonationScreen(onBack = { navController.popBackStack() })
        }
        composable("spotify") {
            com.example.ui.spotify.SpotifyScreen(onBack = { navController.popBackStack() })
        }
        composable("ai") {
            AiMakerScreen(onBack = { navController.popBackStack() })
        }
        composable("ai_image_edit") {
            com.example.ui.aimedia.AiImageEditScreen(
                onBack = { navController.popBackStack() },
                onNavigateToVideoGen = { navController.navigate("ai_video_generate") }
            )
        }
        composable("ai_video_generate") {
            com.example.ui.aimedia.AiVideoGenerateScreen(
                onBack = { navController.popBackStack() },
                onNavigateToImageEdit = { navController.navigate("ai_image_edit") }
            )
        }
        composable("ai_media_hub") {
            com.example.ui.aimedia.AiMediaHubScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("aiagent") {
            AiAgentScreen(onBack = { navController.popBackStack() })
        }
        composable("imagemaker") {
            com.example.ui.imagemaker.ImageMakerScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable("social_maker") {
            com.example.ui.socialmaker.SocialMediaMakerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("apiimagemaker") {
            com.example.ui.apiimagemaker.ApiImageMakerScreen(onBack = { navController.popBackStack() })
        }
        composable("supportai") {
            com.example.ui.supportai.SupportAiScreen(onBack = { navController.popBackStack() })
        }
        composable("server") {
            ServerDataScreen(onBack = { navController.popBackStack() })
        }
        composable("premium") {
            PlaceholderScreen(title = "Premium", onBack = { navController.popBackStack() })
        }
        composable("backupsync") {
            BackupSyncScreen(onBack = { navController.popBackStack() })
        }
        composable("compliance") {
            ComplianceScreen(onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToOnboarding = {
                    navController.navigate("onboarding")
                }
            )
        }
    }
}
