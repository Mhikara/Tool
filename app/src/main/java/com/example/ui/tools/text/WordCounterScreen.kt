package com.example.ui.tools.text

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WordCounterViewModel : ViewModel() {
    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun clearText() {
        _text.value = ""
    }

    fun getWordCount(): Int {
        val trimmed = _text.value.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split("\\s+".toRegex()).size
    }

    fun getCharCount(ignoreSpaces: Boolean = false): Int {
        return if (ignoreSpaces) {
            _text.value.replace("\\s+".toRegex(), "").length
        } else {
            _text.value.length
        }
    }
    
    fun getParagraphCount(): Int {
        val trimmed = _text.value.trim()
        if (trimmed.isEmpty()) return 0
        return trimmed.split("\n+").size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordCounterScreen(onBack: () -> Unit) {
    val viewModel: WordCounterViewModel = viewModel()
    val text by viewModel.text.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Word Counter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearText) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(title = "Words", value = viewModel.getWordCount().toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(title = "Chars", value = viewModel.getCharCount().toString(), modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                StatCard(title = "Paragraphs", value = viewModel.getParagraphCount().toString(), modifier = Modifier.weight(1f))
            }

            OutlinedTextField(
                value = text,
                onValueChange = viewModel::updateText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Enter text here") },
                placeholder = { Text("Type or paste your text...") }
            )
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
