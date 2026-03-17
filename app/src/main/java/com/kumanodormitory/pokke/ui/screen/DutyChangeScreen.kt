package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kumanodormitory.pokke.data.local.entity.RyoseiEntity
import com.kumanodormitory.pokke.ui.component.ThreeColumnSelector
import com.kumanodormitory.pokke.ui.viewmodel.DutyChangeUiState
import com.kumanodormitory.pokke.ui.viewmodel.DutyChangeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyChangeScreen(
    viewModel: DutyChangeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blocks by viewModel.blocks.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val ryoseiList by viewModel.ryoseiList.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()
    val selectedRoom by viewModel.selectedRoom.collectAsState()
    val selectedRyosei by viewModel.selectedRyosei.collectAsState()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is DutyChangeUiState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("事務当番交代") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        },
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
                isRyoseiEnabled = { ryosei -> !isRyoseiLeft(ryosei) }
            )

            if (uiState is DutyChangeUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    if (showConfirmDialog && selectedRyosei != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("事務当番交代") },
            text = { Text("${selectedRyosei!!.name} に事務当番を交代しますか？") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDutyChange() }) {
                    Text("交代する")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

private fun isRyoseiLeft(ryosei: RyoseiEntity): Boolean {
    val leavingDate = ryosei.leavingDate ?: return false
    return leavingDate < System.currentTimeMillis()
}
