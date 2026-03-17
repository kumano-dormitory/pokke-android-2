package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kumanodormitory.pokke.data.local.entity.ParcelEntity
import com.kumanodormitory.pokke.data.local.entity.RyoseiEntity
import com.kumanodormitory.pokke.ui.viewmodel.ParcelDeliveryViewModel

@Composable
fun ParcelDeliveryScreen(
    viewModel: ParcelDeliveryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "荷物の引き渡し",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            // Column 1: Blocks
            BlockColumn(
                blocks = uiState.blocks,
                selectedBlock = uiState.selectedBlock,
                onSelectBlock = viewModel::selectBlock,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            VerticalDivider()

            // Column 2: Rooms
            RoomColumn(
                rooms = uiState.rooms,
                selectedRoom = uiState.selectedRoom,
                onSelectRoom = viewModel::selectRoom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )

            VerticalDivider()

            // Column 3: Ryosei with parcels
            RyoseiColumn(
                ryoseiList = uiState.ryoseiWithParcels,
                selectedRyosei = uiState.selectedRyosei,
                onSelectRyosei = viewModel::selectRyosei,
                modifier = Modifier.weight(1.5f).fillMaxHeight()
            )
        }
    }

    if (uiState.showDeliveryDialog) {
        DeliveryDialog(
            ryosei = uiState.selectedRyosei,
            parcels = uiState.parcelsForRyosei,
            selectedIds = uiState.selectedParcelIds,
            isDelivering = uiState.isDelivering,
            onToggle = viewModel::toggleParcelSelection,
            onConfirm = { viewModel.deliverSelected(onNavigateBack) },
            onDismiss = viewModel::dismissDialog
        )
    }
}

@Composable
private fun BlockColumn(
    blocks: List<String>,
    selectedBlock: String?,
    onSelectBlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "ブロック",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
        HorizontalDivider()
        LazyColumn {
            items(blocks) { block ->
                val isSelected = block == selectedBlock
                Text(
                    text = block,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBlock(block) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun RoomColumn(
    rooms: List<String>,
    selectedRoom: String?,
    onSelectRoom: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "部屋",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
        HorizontalDivider()
        LazyColumn {
            items(rooms) { room ->
                val isSelected = room == selectedRoom
                Text(
                    text = room,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRoom(room) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun RyoseiColumn(
    ryoseiList: List<RyoseiEntity>,
    selectedRyosei: RyoseiEntity?,
    onSelectRyosei: (RyoseiEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "寮生",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
        HorizontalDivider()
        LazyColumn {
            items(ryoseiList) { ryosei ->
                val isSelected = ryosei.id == selectedRyosei?.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRyosei(ryosei) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = ryosei.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = ryosei.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryDialog(
    ryosei: RyoseiEntity?,
    parcels: List<ParcelEntity>,
    selectedIds: Set<String>,
    isDelivering: Boolean,
    onToggle: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDelivering) onDismiss() },
        title = {
            Text("${ryosei?.name ?: ""} さんの荷物")
        },
        text = {
            Column {
                Text(
                    text = "引き渡す荷物を選択してください",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    items(parcels) { parcel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isDelivering) { onToggle(parcel.id) }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = parcel.id in selectedIds,
                                onCheckedChange = { onToggle(parcel.id) },
                                enabled = !isDelivering
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = parcelTypeLabel(parcel.parcelType),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatTimestamp(parcel.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedIds.isNotEmpty() && !isDelivering
            ) {
                if (isDelivering) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp).width(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("引き渡し (${selectedIds.size}件)")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDelivering
            ) {
                Text("キャンセル")
            }
        }
    )
}

private fun parcelTypeLabel(type: String): String = when (type) {
    "NORMAL" -> "普通"
    "REFRIGERATED" -> "冷蔵"
    "FROZEN" -> "冷凍"
    "LARGE" -> "大型"
    "ABSENCE_SLIP" -> "不在票"
    "OTHER" -> "その他"
    else -> type
}

private fun formatTimestamp(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.JAPAN)
    return sdf.format(java.util.Date(millis))
}
