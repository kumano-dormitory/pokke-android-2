package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kumanodormitory.pokke.ui.component.ThreeColumnSelector
import com.kumanodormitory.pokke.ui.viewmodel.ParcelRegisterUiState
import com.kumanodormitory.pokke.ui.viewmodel.ParcelRegisterViewModel

private data class ParcelTypeOption(
    val value: String,
    val label: String
)

private val parcelTypes = listOf(
    ParcelTypeOption("NORMAL", "普通"),
    ParcelTypeOption("REFRIGERATED", "冷蔵"),
    ParcelTypeOption("FROZEN", "冷凍"),
    ParcelTypeOption("LARGE", "大型"),
    ParcelTypeOption("ABSENCE_SLIP", "不在票"),
    ParcelTypeOption("OTHER", "その他")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelRegisterScreen(
    viewModel: ParcelRegisterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blocks by viewModel.blocks.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val ryoseiList by viewModel.ryoseiList.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRyosei by viewModel.selectedRyosei.collectAsState()
    val showTypeDialog by viewModel.showTypeDialog.collectAsState()
    val note by viewModel.note.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is ParcelRegisterUiState.Success) {
            snackbarHostState.showSnackbar("登録しました")
            viewModel.resetUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("荷物受取登録") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ThreeColumnSelector(
                blocks = blocks,
                rooms = rooms,
                ryoseiList = ryoseiList,
                onRyoseiSelected = { viewModel.selectRyosei(it) },
                selectedBlock = selectedBlock,
                onBlockSelected = { viewModel.selectBlock(it) },
                selectedRoom = selectedRoom,
                onRoomSelected = { viewModel.selectRoom(it) },
                showSearch = true,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) }
            )

            if (uiState is ParcelRegisterUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showTypeDialog && selectedRyosei != null) {
        AlertDialog(
            onDismissRequest = {
                selectedType = null
                viewModel.dismissTypeDialog()
            },
            title = { Text("荷物種別を選択") },
            text = {
                Column {
                    Text(
                        text = "${selectedRyosei!!.room} ${selectedRyosei!!.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn {
                        items(parcelTypes) { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedType = type.value }
                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedType == type.value)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                    if (selectedType == "OTHER") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { viewModel.updateNote(it) },
                            label = { Text("備考（200字以内）") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val type = selectedType ?: return@TextButton
                        viewModel.registerParcel(type, if (type == "OTHER") note else "")
                        selectedType = null
                    },
                    enabled = selectedType != null
                ) {
                    Text("登録")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedType = null
                    viewModel.dismissTypeDialog()
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}
