package com.example.ui.dangerzones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DangerZone
import com.example.data.model.UiState
import com.example.data.repository.SafeGuardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DangerZonesViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val repository = SafeGuardRepository(application)

    val dangerZonesState: StateFlow<UiState<List<DangerZone>>> = repository.observeDangerZones()
        .map<List<DangerZone>, UiState<List<DangerZone>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load danger zones")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun reportZone(title: String, description: String, category: String) {
        viewModelScope.launch {
            repository.reportDangerZone(
                DangerZone(
                    title = title,
                    description = description,
                    category = category,
                    latitude = 0.0,
                    longitude = 0.0
                )
            )
        }
    }

    fun upvoteZone(zoneId: String, currentUpvotes: Long) {
        viewModelScope.launch {
            repository.upvoteDangerZone(zoneId, currentUpvotes)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DangerZonesScreen(
    viewModel: DangerZonesViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.dangerZonesState.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("HARASSMENT") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crowdsourced Danger Zones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.testTag("report_zone_fab")
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = "Report Danger Zone")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(text = s.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    val zones = s.data
                    if (zones.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "No reported danger zones in your area.", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Community alerts keep everyone safe.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(zones) { zone ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("danger_zone_card"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = zone.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                                Text(text = zone.category, modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onError)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = zone.description, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Community Reports: ${zone.upvotes}", style = MaterialTheme.typography.bodySmall)
                                            OutlinedButton(onClick = { viewModel.upvoteZone(zone.zoneId, zone.upvotes) }) {
                                                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Confirm (${zone.upvotes})")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Report Unsafe Area") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Location Title (e.g. Dark Alley)") },
                                modifier = Modifier.fillMaxWidth().testTag("zone_title_input")
                            )
                            OutlinedTextField(
                                value = descInput,
                                onValueChange = { descInput = it },
                                label = { Text("Description / Reason") },
                                modifier = Modifier.fillMaxWidth().testTag("zone_desc_input")
                            )
                            OutlinedTextField(
                                value = categoryInput,
                                onValueChange = { categoryInput = it },
                                label = { Text("Category (HARASSMENT, POOR_LIGHTING, ISOLATED)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank()) {
                                    viewModel.reportZone(titleInput, descInput, categoryInput)
                                    titleInput = ""
                                    descInput = ""
                                    showDialog = false
                                }
                            },
                            modifier = Modifier.testTag("submit_zone_button")
                        ) {
                            Text("Submit Report")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
