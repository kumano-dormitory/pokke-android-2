package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kumanodormitory.pokke.data.local.entity.ParcelEntity
import com.kumanodormitory.pokke.ui.viewmodel.NightDutyUiState
import com.kumanodormitory.pokke.ui.viewmodel.NightDutyViewModel

@Composable
fun NightDutyScreen(
    viewModel: NightDutyViewModel,
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
        NightDutyHeader(uiState = uiState)

        BuildingTabs(
            tabs = NightDutyViewModel.BUILDING_TABS,
            selectedTab = uiState.selectedTab,
            onSelectTab = viewModel::selectTab
        )

        ParcelList(
            parcels = uiState.parcelsByBuilding[uiState.selectedTab] ?: emptyList(),
            uiState = uiState,
            onToggleCheck = viewModel::toggleCheck,
            onToggleLost = viewModel::toggleLost,
            modifier = Modifier.weight(1f)
        )

        NightDutyFooter(
            uiState = uiState,
            onAdvance = viewModel::advanceToPhase2,
            onComplete = { viewModel.completeNightDuty(onNavigateBack) }
        )
    }
}

@Composable
private fun NightDutyHeader(uiState: NightDutyUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "泊まり事務当番",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        PhaseIndicator(phase = uiState.phase)
    }
}

@Composable
private fun PhaseIndicator(phase: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (phase == 1) "フェーズ1: 現物確認" else "フェーズ2: 荷物札確認",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BuildingTabs(
    tabs: List<String>,
    selectedTab: String,
    onSelectTab: (String) -> Unit
) {
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onSelectTab(tab) },
                text = { Text(tab) }
            )
        }
    }
}

@Composable
private fun ParcelList(
    parcels: List<ParcelEntity>,
    uiState: NightDutyUiState,
    onToggleCheck: (String) -> Unit,
    onToggleLost: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (parcels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "この棟には未引渡の荷物はありません",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(parcels) { parcel ->
            ParcelCheckCard(
                parcel = parcel,
                uiState = uiState,
                onToggleCheck = onToggleCheck,
                onToggleLost = onToggleLost
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun ParcelCheckCard(
    parcel: ParcelEntity,
    uiState: NightDutyUiState,
    onToggleCheck: (String) -> Unit,
    onToggleLost: (String) -> Unit
) {
    val isChecked = if (uiState.phase == 1) {
        parcel.id in uiState.checkedIdsPhase1
    } else {
        parcel.id in uiState.checkedIdsPhase2
    }
    val isLost = parcel.id in uiState.lostIds

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggleCheck(parcel.id) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = parcel.ownerRoomName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = parcel.ownerName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = parcelTypeLabel(parcel.parcelType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formatTimestamp(parcel.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.phase == 1) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "紛失",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLost) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(
                        checked = isLost,
                        onCheckedChange = { onToggleLost(parcel.id) }
                    )
                }
            } else {
                if (isLost) {
                    Text(
                        text = "紛失",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NightDutyFooter(
    uiState: NightDutyUiState,
    onAdvance: () -> Unit,
    onComplete: () -> Unit
) {
    HorizontalDivider()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val totalCount = uiState.parcelsByBuilding.values.flatten().size
        val checkedCount = if (uiState.phase == 1) {
            uiState.checkedIdsPhase1.size
        } else {
            uiState.checkedIdsPhase2.size
        }

        Text(
            text = "確認済み: $checkedCount / $totalCount",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (uiState.phase == 1) {
            Button(
                onClick = onAdvance,
                enabled = uiState.allCheckedPhase1
            ) {
                Text("フェーズ2へ進む")
            }
        } else {
            Button(
                onClick = onComplete,
                enabled = uiState.allCheckedPhase2 && !uiState.isCompleting
            ) {
                if (uiState.isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp).width(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("泊まり事務当番終了")
                }
            }
        }
    }
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
