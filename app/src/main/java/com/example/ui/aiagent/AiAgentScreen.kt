package com.example.ui.aiagent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Models ---
enum class StepStatus {
    PENDING, RUNNING, SUCCESS, FAILED, NEEDS_CONFIRMATION
}

data class AgentStep(
    val id: String,
    val title: String,
    val description: String,
    val tool: String,
    var status: StepStatus = StepStatus.PENDING,
    var result: String? = null
)

// --- ViewModel ---
class AiAgentViewModel : ViewModel() {
    private val _prompt = MutableStateFlow("")
    val prompt = _prompt.asStateFlow()

    private val _isAgentRunning = MutableStateFlow(false)
    val isAgentRunning = _isAgentRunning.asStateFlow()
    
    private val _agentStatusText = MutableStateFlow("Siap menerima tugas.")
    val agentStatusText = _agentStatusText.asStateFlow()

    private val _steps = MutableStateFlow<List<AgentStep>>(emptyList())
    val steps = _steps.asStateFlow()
    
    private val _needsConfirmation = MutableStateFlow<AgentStep?>(null)
    val needsConfirmation = _needsConfirmation.asStateFlow()

    fun updatePrompt(text: String) {
        _prompt.value = text
    }

    fun startAgent(taskDesc: String) {
        if (taskDesc.isBlank()) return
        
        _prompt.value = ""
        _isAgentRunning.value = true
        _agentStatusText.value = "[Planner] Menganalisis instruksi..."
        _steps.value = emptyList()
        _needsConfirmation.value = null
        
        viewModelScope.launch {
            // Mock Planner
            delay(1500)
            
            val mockPlan = generateMockPlan(taskDesc)
            _steps.value = mockPlan
            
            _agentStatusText.value = "[Executor] Memulai eksekusi task..."
            delay(1000)
            
            executeSteps()
        }
    }
    
    fun stopAgent() {
        _isAgentRunning.value = false
        _agentStatusText.value = "Dihentikan oleh pengguna."
        _needsConfirmation.value = null
    }
    
    fun confirmAction(approved: Boolean) {
        val step = _needsConfirmation.value ?: return
        _needsConfirmation.value = null
        
        if (approved) {
            viewModelScope.launch {
                updateStepStatus(step.id, StepStatus.RUNNING)
                delay(1000)
                updateStepStatus(step.id, StepStatus.SUCCESS, "Dikonfirmasi pengguna. Aksi berhasil.")
                executeNextStep(step.id)
            }
        } else {
            updateStepStatus(step.id, StepStatus.FAILED, "Dibatalkan oleh pengguna.")
            _isAgentRunning.value = false
            _agentStatusText.value = "Task dibatalkan."
        }
    }

    private suspend fun executeSteps() {
        val allSteps = _steps.value
        if (allSteps.isEmpty()) return
        
        executeNextStep(null)
    }
    
    private suspend fun executeNextStep(lastStepId: String?) {
        if (!_isAgentRunning.value) return
        
        val allSteps = _steps.value
        val nextStepIndex = if (lastStepId == null) 0 else allSteps.indexOfFirst { it.id == lastStepId } + 1
        
        if (nextStepIndex >= allSteps.size) {
            _isAgentRunning.value = false
            _agentStatusText.value = "[Reporter] Semua task selesai dengan sukses."
            return
        }
        
        val step = allSteps[nextStepIndex]
        _agentStatusText.value = "[Executor] Menjalankan: ${step.title}"
        
        updateStepStatus(step.id, StepStatus.RUNNING)
        delay(1500) // Simulate processing time
        
        // Simulating condition where confirmation is needed
        if (step.tool == "File Delete" || step.tool == "Upload") {
            updateStepStatus(step.id, StepStatus.NEEDS_CONFIRMATION)
            _agentStatusText.value = "[Guardrail] Membutuhkan konfirmasi pengguna!"
            _needsConfirmation.value = step
            return // Wait for user action
        }
        
        // Simulating success/verification
        updateStepStatus(step.id, StepStatus.SUCCESS, "Verifikasi sukses. Output sesuai.")
        
        // Proceed to next
        executeNextStep(step.id)
    }

    private fun updateStepStatus(id: String, status: StepStatus, result: String? = null) {
        _steps.value = _steps.value.map {
            if (it.id == id) it.copy(status = status, result = result ?: it.result)
            else it
        }
    }

    private fun generateMockPlan(taskDesc: String): List<AgentStep> {
        // Simple heuristic mock for demonstration
        val lowerDesc = taskDesc.lowercase()
        val steps = mutableListOf<AgentStep>()
        
        if (lowerDesc.contains("video") || lowerDesc.contains("kompres")) {
            steps.add(AgentStep("1", "Mencari File Video", "Scan direktori untuk file video", "File Manager"))
            steps.add(AgentStep("2", "Kompres Video", "Kompresi video di bawah target ukuran", "Video Compressor"))
        }
        if (lowerDesc.contains("download")) {
            steps.add(AgentStep("1", "Download Media", "Mengunduh file dari URL", "Downloader"))
        }
        if (lowerDesc.contains("potong") || lowerDesc.contains("cut")) {
            steps.add(AgentStep("2", "Potong Media", "Memotong durasi file", "Video/Audio Cutter"))
        }
        if (lowerDesc.contains("hapus") || lowerDesc.contains("clean")) {
            steps.add(AgentStep("3", "Hapus File", "Menghapus file asli", "File Delete"))
        }
        
        if (steps.isEmpty()) {
            steps.add(AgentStep("1", "Analisis Teks", "Membaca teks dari instruksi", "AI Tools"))
            steps.add(AgentStep("2", "Proses Data", "Memproses data sesuai task", "System"))
        }
        
        return steps
    }
}

// --- UI Components ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAgentScreen(onBack: () -> Unit) {
    val viewModel: AiAgentViewModel = viewModel()
    val prompt by viewModel.prompt.collectAsState()
    val isAgentRunning by viewModel.isAgentRunning.collectAsState()
    val statusText by viewModel.agentStatusText.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val needsConfirmation by viewModel.needsConfirmation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("AI Agent Otonom", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Super Tools Automation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (isAgentRunning) {
                        IconButton(onClick = viewModel::stopAgent) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop Agent", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAgentRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                    } else if (steps.isNotEmpty() && steps.all { it.status == StepStatus.SUCCESS }) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(statusText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Steps List (Task Graph)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (steps.isEmpty() && !isAgentRunning) {
                    item {
                        EmptyStateView()
                    }
                } else {
                    items(steps) { step ->
                        StepItemView(step)
                    }
                }
            }
            
            // Confirmation Dialog
            if (needsConfirmation != null) {
                ConfirmationDialog(
                    step = needsConfirmation!!,
                    onApprove = { viewModel.confirmAction(true) },
                    onDecline = { viewModel.confirmAction(false) }
                )
            }

            // Input Area
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Perintah Agent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = viewModel::updatePrompt,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Contoh: Download video ini lalu kompres jadi MP3...") },
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isAgentRunning,
                            maxLines = 3
                        )
                        FloatingActionButton(
                            onClick = { viewModel.startAgent(prompt) },
                            containerColor = if (isAgentRunning) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Filled.Send, 
                                contentDescription = "Mulai", 
                                tint = if (isAgentRunning) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItemView(step: AgentStep) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(step.status) {
                StepStatus.RUNNING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                StepStatus.NEEDS_CONFIRMATION -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                StepStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                StepStatus.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                StepStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (step.status) {
                            StepStatus.RUNNING -> MaterialTheme.colorScheme.primary
                            StepStatus.SUCCESS -> Color(0xFF4CAF50)
                            StepStatus.FAILED, StepStatus.NEEDS_CONFIRMATION -> MaterialTheme.colorScheme.error
                            StepStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step.status == StepStatus.RUNNING) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else if (step.status == StepStatus.SUCCESS) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else if (step.status == StepStatus.NEEDS_CONFIRMATION) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(step.id, color = if(step.status == StepStatus.PENDING) MaterialTheme.colorScheme.onSurfaceVariant else Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(step.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = step.tool,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                if (step.result != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "↳ ${step.result}", 
                        style = MaterialTheme.typography.bodySmall,
                        color = if (step.status == StepStatus.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(step: AgentStep, onApprove: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = { }, // Force user to choose
        icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        title = { Text("Konfirmasi Aksi") },
        text = {
            Column {
                Text("Agent ingin melakukan tindakan yang memerlukan izin Anda:")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aksi: ${step.title}\nTools: ${step.tool}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lanjutkan?", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(onClick = onApprove) {
                Text("Izinkan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDecline) {
                Text("Batalkan")
            }
        }
    )
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.SmartToy,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Berikan perintah pada Agent",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Agent akan memecah tugas kompleks menjadi beberapa langkah dan menjalankannya secara otonom.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
