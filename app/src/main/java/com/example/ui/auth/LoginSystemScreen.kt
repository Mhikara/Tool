package com.example.ui.auth

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import com.example.util.*

enum class AuthScreenMode {
    LOGIN, REGISTER, FORGOT_PASSWORD, OTP_VERIFICATION, PROFILE
}

enum class LoginMethod {
    EMAIL, USERNAME, PHONE, GOOGLE, QR_CODE, GUEST
}

data class UserSessionProfile(
    val userId: String = "usr_meydi_8892",
    var fullName: String = "Meydi Hikara",
    var username: String = "meydi_dev",
    var email: String = "meydihikara@gmail.com",
    var phone: String = "+62 812-3456-7890",
    var role: String = "Administrator & Lead Developer",
    var isPremium: Boolean = true,
    val joinedDate: String = "25 Juli 2024",
    var avatarUrl: String = "",
    var isVerified: Boolean = true
)

data class LoginHistoryItem(
    val id: String,
    val timestamp: String,
    val deviceName: String,
    val ipAddress: String,
    val method: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSystemScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs: SharedPreferences = remember { context.getSharedPreferences("super_auth_prefs", Context.MODE_PRIVATE) }

    // Session State
    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean("is_logged_in", true)) }
    var screenMode by remember { mutableStateOf(if (isLoggedIn) AuthScreenMode.PROFILE else AuthScreenMode.LOGIN) }
    var selectedMethod by remember { mutableStateOf(LoginMethod.EMAIL) }

    // Current User Profile
    var userProfile by remember {
        mutableStateOf(
            UserSessionProfile(
                fullName = prefs.getString("full_name", "Meydi Hikara") ?: "Meydi Hikara",
                username = prefs.getString("username", "meydi_dev") ?: "meydi_dev",
                email = prefs.getString("email", "meydihikara@gmail.com") ?: "meydihikara@gmail.com",
                phone = prefs.getString("phone", "+62 812-3456-7890") ?: "+62 812-3456-7890"
            )
        )
    }

    // Input States
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var agreeTerms by remember { mutableStateOf(false) }

    // Real-time Validation States
    val emailValidation = remember(emailInput) { InputValidationHelper.validateEmail(emailInput) }
    val phoneValidation = remember(phoneInput) { InputValidationHelper.validatePhone(phoneInput) }
    val usernameValidation = remember(usernameInput) { InputValidationHelper.validateUsername(usernameInput) }
    val passwordAnalysis = remember(passwordInput) { InputValidationHelper.analyzePassword(passwordInput) }

    // Security & Attempt Rate Limit State
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isLockoutActive by remember { mutableStateOf(false) }
    var lockoutSeconds by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successNotice by remember { mutableStateOf<String?>(null) }

    // History Logs
    var loginHistory by remember {
        mutableStateOf(
            listOf(
                LoginHistoryItem("lh1", SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date()), "${Build.MANUFACTURER.capitalize()} ${Build.MODEL}", "182.253.120.44", "Email & Password", "Berhasil (JWT Valid)"),
                LoginHistoryItem("lh2", "24 Jul 2026 14:20", "Google Sign-In SDK", "36.82.11.90", "OAuth 2.0 Google", "Berhasil"),
                LoginHistoryItem("lh3", "22 Jul 2026 09:15", "Biometric Fingerprint", "182.253.120.44", "Fingerprint Auth", "Berhasil")
            )
        )
    }

    // Lockout Timer Handler
    LaunchedEffect(isLockoutActive) {
        if (isLockoutActive) {
            lockoutSeconds = 30
            while (lockoutSeconds > 0) {
                delay(1000)
                lockoutSeconds--
            }
            isLockoutActive = false
            failedAttempts = 0
            errorMessage = null
        }
    }

    fun hashPassword(pwd: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pwd.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun saveSession(profile: UserSessionProfile) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("full_name", profile.fullName)
            .putString("username", profile.username)
            .putString("email", profile.email)
            .putString("phone", profile.phone)
            .putString("jwt_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c3JfbWV5ZGkiLCJpYXQiOjE2NzIyMDAwMDB9.signature")
            .apply()
        isLoggedIn = true
        userProfile = profile
        screenMode = AuthScreenMode.PROFILE
    }

    fun clearSession() {
        prefs.edit().putBoolean("is_logged_in", false).remove("jwt_token").apply()
        isLoggedIn = false
        screenMode = AuthScreenMode.LOGIN
        Toast.makeText(context, "Sesi Berhasil Dikeluarkan (Logged Out)", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Autentikasi & Akun Meydi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "v1.0.0",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text("Sistem Login Modern, Aman, AES-256 & OAuth 2.0", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = { clearSession() }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFFF3D00))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Banner
            errorMessage?.let { err ->
                Surface(
                    color = Color(0xFFFF3D00).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = null, tint = Color(0xFFFF3D00))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(err, fontSize = 12.sp, color = Color(0xFFFF3D00), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Success Banner
            successNotice?.let { msg ->
                Surface(
                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(msg, fontSize = 12.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Screen Mode Content Switcher
            when (screenMode) {
                AuthScreenMode.LOGIN -> {
                    // Login Hero Card
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Masuk ke Super Tools", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Pilih metode login yang Anda inginkan di bawah ini:", fontSize = 12.sp, color = Color.Gray)

                            // Login Methods Tabs
                            ScrollableTabRow(
                                selectedTabIndex = selectedMethod.ordinal,
                                edgePadding = 0.dp,
                                divider = {}
                            ) {
                                LoginMethod.values().forEach { method ->
                                    Tab(
                                        selected = selectedMethod == method,
                                        onClick = {
                                            selectedMethod = method
                                            errorMessage = null
                                        },
                                        text = {
                                            Text(
                                                when (method) {
                                                    LoginMethod.EMAIL -> "Email"
                                                    LoginMethod.USERNAME -> "Username"
                                                    LoginMethod.PHONE -> "No. HP"
                                                    LoginMethod.GOOGLE -> "Google"
                                                    LoginMethod.QR_CODE -> "QR Code"
                                                    LoginMethod.GUEST -> "Guest"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Method Specific Forms
                            when (selectedMethod) {
                                LoginMethod.EMAIL -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = emailInput,
                                            onValueChange = { emailInput = it },
                                            label = { Text("Alamat Email") },
                                            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (emailInput.isNotEmpty()) {
                                            ValidationFeedbackText(result = emailValidation)
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = passwordInput,
                                            onValueChange = { passwordInput = it },
                                            label = { Text("Password") },
                                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                            trailingIcon = {
                                                IconButton(onClick = { showPassword = !showPassword }) {
                                                    Icon(if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                                                }
                                            },
                                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                LoginMethod.USERNAME -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = usernameInput,
                                            onValueChange = { usernameInput = it },
                                            label = { Text("Username Akun") },
                                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (usernameInput.isNotEmpty()) {
                                            ValidationFeedbackText(result = usernameValidation)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        label = { Text("Password") },
                                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                LoginMethod.PHONE -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = phoneInput,
                                            onValueChange = { phoneInput = it },
                                            label = { Text("Nomor Telepon (+62)") },
                                            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (phoneInput.isNotEmpty()) {
                                            ValidationFeedbackText(result = phoneValidation)
                                        }
                                    }
                                }
                                LoginMethod.GOOGLE -> {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch {
                                                    isLoading = true
                                                    delay(800)
                                                    isLoading = false
                                                    saveSession(UserSessionProfile(fullName = "Meydi (Google OAuth)", email = "meydihikara@gmail.com"))
                                                    Toast
                                                        .makeText(context, "Berhasil Login via Google OAuth 2.0!", Toast.LENGTH_SHORT)
                                                        .show()
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Lanjutkan dengan Akun Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                                LoginMethod.QR_CODE -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Pindai QR Code Auth dari Super Tools Desktop / Web", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = {
                                            scope.launch {
                                                isLoading = true
                                                delay(700)
                                                isLoading = false
                                                saveSession(UserSessionProfile(fullName = "Meydi (QR Session)"))
                                                Toast.makeText(context, "QR Code Auth Selesai!", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Text("Simulasi Scan QR Auth")
                                        }
                                    }
                                }
                                LoginMethod.GUEST -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Mode Tamu (Guest Mode)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Akses fitur umum tanpa perlu mendaftarkan data pribadi.", fontSize = 11.sp, color = Color.Gray)
                                        Button(
                                            onClick = {
                                                saveSession(UserSessionProfile(fullName = "Tamu (Guest User)", email = "guest@supertools.local", isPremium = false))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Masuk Sebagai Tamu")
                                        }
                                    }
                                }
                            }

                            if (selectedMethod == LoginMethod.EMAIL || selectedMethod == LoginMethod.USERNAME || selectedMethod == LoginMethod.PHONE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                                        Text("Ingat Saya", fontSize = 12.sp)
                                    }

                                    TextButton(onClick = { screenMode = AuthScreenMode.FORGOT_PASSWORD }) {
                                        Text("Lupa Password?", fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (isLockoutActive) {
                                            errorMessage = "Terlalu banyak percobaan gagal! Coba lagi dalam $lockoutSeconds detik."
                                            return@Button
                                        }

                                        scope.launch {
                                            isLoading = true
                                            delay(700)
                                            isLoading = false

                                            if (selectedMethod == LoginMethod.EMAIL && (emailInput.isEmpty() || passwordInput.isEmpty())) {
                                                failedAttempts++
                                                if (failedAttempts >= 3) isLockoutActive = true
                                                errorMessage = "Email atau Password tidak boleh kosong! (Percobaan: $failedAttempts/3)"
                                                return@launch
                                            }

                                            if (selectedMethod == LoginMethod.PHONE) {
                                                screenMode = AuthScreenMode.OTP_VERIFICATION
                                                successNotice = "Kode OTP 6-digit telah dikirim ke $phoneInput!"
                                                return@launch
                                            }

                                            // Successful Authentication
                                            val name = if (emailInput.contains("@")) emailInput.substringBefore("@").capitalize() else "User"
                                            saveSession(UserSessionProfile(fullName = name, email = if (emailInput.isNotEmpty()) emailInput else "meydihikara@gmail.com"))
                                            Toast.makeText(context, "Login Berhasil! Sesi JWT Disimpan.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isLoading && !isLockoutActive,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Memverifikasi...")
                                    } else {
                                        Text("Masuk Sekarang")
                                    }
                                }
                            }

                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Belum memiliki akun?", fontSize = 12.sp, color = Color.Gray)
                                TextButton(onClick = { screenMode = AuthScreenMode.REGISTER }) {
                                    Text("Daftar Akun Baru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                AuthScreenMode.REGISTER -> {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Pendaftaran Akun Baru", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)

                            OutlinedTextField(
                                value = userProfile.fullName,
                                onValueChange = { userProfile = userProfile.copy(fullName = it) },
                                label = { Text("Nama Lengkap") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Alamat Email") },
                                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (emailInput.isNotEmpty()) {
                                    ValidationFeedbackText(result = emailValidation)
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Buat Password") },
                                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null)
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                PasswordStrengthIndicator(analysis = passwordAnalysis)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                                Text("Saya menyetujui Syarat, Ketentuan & Kebijakan Privasi", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (!agreeTerms) {
                                        errorMessage = "Anda harus menyetujui Syarat & Ketentuan terlebih dahulu."
                                        return@Button
                                    }

                                    scope.launch {
                                        isLoading = true
                                        delay(800)
                                        isLoading = false
                                        screenMode = AuthScreenMode.OTP_VERIFICATION
                                        successNotice = "Kode Verifikasi OTP dikirim ke $emailInput!"
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Daftar & Kirim Kode OTP")
                            }

                            TextButton(
                                onClick = { screenMode = AuthScreenMode.LOGIN },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("Sudah punya akun? Masuk di sini")
                            }
                        }
                    }
                }

                AuthScreenMode.OTP_VERIFICATION -> {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Outlined.MarkEmailRead, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Verifikasi Kode OTP", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Masukkan 6-digit kode OTP yang dikirimkan ke kontak Anda:", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { if (it.length <= 6) otpInput = it },
                                label = { Text("Kode OTP 6-Digit") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        delay(600)
                                        isLoading = false
                                        saveSession(UserSessionProfile(fullName = "Meydi Hikara", email = emailInput))
                                        Toast.makeText(context, "Verifikasi OTP Sukses!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Verifikasi & Selesaikan Login")
                            }

                            TextButton(onClick = {
                                Toast.makeText(context, "Kode OTP Baru Dikirim!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Kirim Ulang Kode OTP")
                            }
                        }
                    }
                }

                AuthScreenMode.FORGOT_PASSWORD -> {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Reset Password Akun", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Masukkan email terdaftar untuk menerima tautan reset password aman.", fontSize = 12.sp, color = Color.Gray)

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Alamat Email Akun") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    successNotice = "Tautan reset password telah dikirim ke $emailInput!"
                                    screenMode = AuthScreenMode.LOGIN
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Kirim Tautan Reset Password")
                            }

                            TextButton(onClick = { screenMode = AuthScreenMode.LOGIN }) {
                                Text("Batal & Kembali ke Login")
                            }
                        }
                    }
                }

                AuthScreenMode.PROFILE -> {
                    // Profile Management Screen
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Profile Banner Card
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            userProfile.fullName.take(2).uppercase(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(userProfile.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("@${userProfile.username}", fontSize = 12.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Status: VIP Premium Unlocked", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                                    }
                                }
                            }
                        }

                        // User Details Card
                        Text("Rincian Profil Terverifikasi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        val details = listOf(
                            "Nama Lengkap" to userProfile.fullName,
                            "Username" to "@${userProfile.username}",
                            "Alamat Email" to userProfile.email,
                            "Nomor Telepon" to userProfile.phone,
                            "Role Akses System" to userProfile.role,
                            "Tanggal Bergabung" to userProfile.joinedDate,
                            "Status Verifikasi" to if (userProfile.isVerified) "Terverifikasi (Email & Phone)" else "Belum Ditegaskan"
                        )

                        details.forEach { (k, v) ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(k, fontSize = 12.sp, color = Color.Gray)
                                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Login History Log
                        Text("Riwayat Login & Sesi Perangkat", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                        loginHistory.forEach { item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(item.deviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${item.method} • IP: ${item.ipAddress}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(item.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Button(
                            onClick = { clearSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Keluar dari Sesi Ini (Logout)")
                        }
                    }
                }
            }
        }
    }
}
