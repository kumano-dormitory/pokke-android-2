package com.kumanodormitory.pokke.ui.screen

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kumanodormitory.pokke.data.local.entity.OperationLogEntity
import com.kumanodormitory.pokke.ui.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val cancelTarget by viewModel.showCancelDialog.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        // Header: current duty person
        DutyPersonHeader(dutyPersonName = uiState.currentDutyPersonName)

        HorizontalDivider()

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Landscape layout: left = nav buttons, right = operation log
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Left panel: navigation buttons
                NavigationPanel(
                    onNavigate = onNavigate,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right panel: operation log
                OperationLogPanel(
                    logs = uiState.recentLogs,
                    isCancellable = { viewModel.isCancellable(it) },
                    onCancelRequest = { viewModel.requestCancel(it) },
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                )
            }
        }
    }

    // Cancel confirmation dialog
    cancelTarget?.let { log ->
        CancelConfirmDialog(
            log = log,
            onConfirm = { viewModel.confirmCancel() },
            onDismiss = { viewModel.dismissCancelDialog() }
        )
    }

    // Error dialog
    uiState.error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("エラー") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun DutyPersonHeader(dutyPersonName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "現在の事務当番:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dutyPersonName.ifEmpty { "未設定" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (dutyPersonName.isEmpty()) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun NavigationPanel(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "メニュー",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))

        NavButton(label = "事務当交代", onClick = { onNavigate("duty_change") })
        NavButton(
            label = "寮生の呼び出し",
            onClick = {},
            enabled = false
        )
        NavButton(label = "荷物の受け取り", onClick = { onNavigate("parcel_register") })
        NavButton(label = "荷物の引き渡し", onClick = { onNavigate("parcel_delivery") })
        NavButton(label = "泊まり事務当番", onClick = { onNavigate("night_duty") })
        NavButton(label = "旧型ノート", onClick = { onNavigate("old_notebook") })
        NavButton(label = "管理用画面", onClick = { onNavigate("admin") })
    }
}

@Composable
private fun NavButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = if (enabled) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun OperationLogPanel(
    logs: List<OperationLogEntity>,
    isCancellable: (OperationLogEntity) -> Boolean,
    onCancelRequest: (OperationLogEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "操作ログ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "操作ログはありません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    OperationLogItem(
                        log = log,
                        isCancellable = isCancellable(log),
                        onCancelRequest = { onCancelRequest(log) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationLogItem(
    log: OperationLogEntity,
    isCancellable: Boolean,
    onCancelRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(log.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OperationTypeBadge(operationType = log.operationType)
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (!log.metadata.isNullOrBlank()) {
                    Text(
                        text = log.metadata,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!log.operatedByName.isNullOrBlank()) {
                    Text(
                        text = "操作者: ${log.operatedByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isCancellable) {
                OutlinedButton(
                    onClick = onCancelRequest,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
private fun OperationTypeBadge(operationType: String) {
    val label = when (operationType) {
        "REGISTER" -> "登録"
        "DELIVER" -> "引渡"
        "CANCEL_REGISTER" -> "登録取消"
        "CANCEL_DELIVER" -> "引渡取消"
        "MARK_LOST" -> "紛失"
        "NIGHT_DUTY_CONFIRM" -> "泊まり確認"
        "DUTY_CHANGE" -> "当番交代"
        else -> operationType
    }
    val color = when (operationType) {
        "REGISTER" -> MaterialTheme.colorScheme.primary
        "DELIVER" -> MaterialTheme.colorScheme.tertiary
        "CANCEL_REGISTER", "CANCEL_DELIVER" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.JAPAN)
    return sdf.format(Date(millis))
}

@Composable
private fun CancelConfirmDialog(
    log: OperationLogEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val typeLabel = when (log.operationType) {
        "REGISTER" -> "登録"
        "DELIVER" -> "引渡"
        else -> log.operationType
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${typeLabel}の取消") },
        text = {
            Text("この操作を取り消しますか？\n\n日時: ${formatTimestamp(log.createdAt)}\n対象: ${log.metadata ?: "N/A"}")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("取消実行", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
