package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumanodormitory.pokke.R
import com.kumanodormitory.pokke.data.local.entity.ParcelEntity
import com.kumanodormitory.pokke.data.local.entity.RyoseiEntity
import com.kumanodormitory.pokke.ui.component.ThreeColumnSelector
import com.kumanodormitory.pokke.ui.util.SoundManager
import com.kumanodormitory.pokke.ui.util.debounceClickable
import com.kumanodormitory.pokke.ui.util.formatParcelType
import com.kumanodormitory.pokke.ui.viewmodel.ParcelDeliveryViewModel

// 旧POKKEの引渡テーマカラー
private val ReleaseTheme = Color(0xFFDAA186)
private val ReleaseHeaderFont = Color.White

@Composable
fun ParcelDeliveryScreen(
    viewModel: ParcelDeliveryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header — 旧UIの include_header_release を再現
        ReleaseHeader(
            dutyPersonName = uiState.dutyPersonName,
            onBack = onNavigateBack
        )

        // Content — ThreeColumnSelector で統一
        ThreeColumnSelector(
            blocks = uiState.blocks,
            rooms = uiState.rooms,
            ryoseiList = uiState.ryoseiWithParcels,
            onRyoseiSelected = { ryosei ->
                SoundManager.play(context, R.raw.cursor2)
                viewModel.selectRyosei(ryosei)
            },
            selectedBlock = uiState.selectedBlock,
            onBlockSelected = { block ->
                SoundManager.play(context, R.raw.cursor)
                viewModel.selectBlock(block)
            },
            selectedRoom = uiState.selectedRoom,
            onRoomSelected = { room ->
                SoundManager.play(context, R.raw.cursor2)
                viewModel.selectRoom(room)
            },
            showSearch = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // Footer — 旧UIの include_footer_release を再現
        ReleaseFooter()
    }

    // 本人確認ダイアログ（旧POKKE再現）
    if (uiState.showIdentityDialog) {
        IdentityConfirmDialog(
            ryosei = uiState.selectedRyosei,
            onConfirm = {
                viewModel.confirmIdentity()
            },
            onDismiss = {
                viewModel.dismissIdentityDialog()
            }
        )
    }

    // 引渡ダイアログ
    if (uiState.showDeliveryDialog) {
        DeliveryDialog(
            ryosei = uiState.selectedRyosei,
            parcels = uiState.parcelsForRyosei,
            selectedIds = uiState.selectedParcelIds,
            isDelivering = uiState.isDelivering,
            onToggle = viewModel::toggleParcelSelection,
            onConfirm = {
                viewModel.deliverSelected {
                    SoundManager.play(context, R.raw.done)
                    onNavigateBack()
                }
            },
            onDismiss = viewModel::dismissDialog
        )
    }
}

// ─── Header ───────────────────────────────────────────────

@Composable
private fun ReleaseHeader(
    dutyPersonName: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(ReleaseTheme)
            .padding(horizontal = 70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "戻る",
                tint = ReleaseHeaderFont,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "引き渡し",
            color = ReleaseHeaderFont,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "現在の事務当：",
            color = ReleaseHeaderFont,
            fontSize = 20.sp
        )

        Text(
            text = dutyPersonName.ifEmpty { "設定されていません" },
            color = ReleaseHeaderFont,
            fontSize = 20.sp,
            modifier = Modifier.width(280.dp)
        )
    }
}

// ─── Footer ───────────────────────────────────────────────

@Composable
private fun ReleaseFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(ReleaseTheme)
            .padding(horizontal = 50.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "© Kumano Dormitory",
            color = ReleaseHeaderFont,
            fontSize = 12.sp
        )
    }
}

// ─── Delivery Dialog ──────────────────────────────────────
// 旧UI: ReleaseDialog.java を再現
// - タイトル: "部屋 名前 の荷物を引き渡します。"
// - MultiChoiceItems（全選択デフォルト）
// - 「引き渡し」「キャンセル」ボタン

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
            Text(
                text = "${ryosei?.room ?: ""} ${ryosei?.name ?: ""} の荷物を引き渡します。",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                items(parcels) { parcel ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isDelivering) { onToggle(parcel.id) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = parcel.id in selectedIds,
                            onCheckedChange = { onToggle(parcel.id) },
                            enabled = !isDelivering
                        )
                        // 種別
                        Text(
                            text = formatParcelType(parcel.parcelType),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(60.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // 時刻（HH:mm形式）
                        Text(
                            text = formatTime(parcel.createdAt),
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // 登録日付（yyyy/MM/dd形式）
                        Text(
                            text = formatDate(parcel.createdAt),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            val canConfirm = selectedIds.isNotEmpty() && !isDelivering
            Box(
                modifier = Modifier
                    .then(
                        if (canConfirm) {
                            Modifier.debounceClickable(1000L) { onConfirm() }
                        } else {
                            Modifier
                        }
                    )
            ) {
                Button(
                    onClick = {},
                    enabled = canConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE9EBF5) // 旧UI: data1D
                    )
                ) {
                    if (isDelivering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "引き渡し",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDelivering
            ) {
                Text(
                    text = "キャンセル",
                    fontSize = 20.sp
                )
            }
        }
    )
}

// ─── Identity Confirm Dialog ─────────────────────────────
// 旧POKKE: ReleaseActivity.java の本人確認を再現

@Composable
private fun IdentityConfirmDialog(
    ryosei: RyoseiEntity?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("本人確認", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column {
                Text(
                    text = "${ryosei?.room ?: ""} ${ryosei?.name ?: ""} さんの本人確認を行ってください。",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "以下のいずれかで本人確認をしてください：",
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("・学生証", fontSize = 16.sp)
                Text("・免許証", fontSize = 16.sp)
                Text("・橙食券", fontSize = 16.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReleaseTheme
                )
            ) {
                Text("確認済み", fontSize = 18.sp, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", fontSize = 18.sp)
            }
        }
    )
}

// ─── Helpers ─────────────────────────────────────────────

private val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.JAPAN)
private val dateFormat = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.JAPAN)

private fun formatTime(millis: Long): String = timeFormat.format(java.util.Date(millis))
private fun formatDate(millis: Long): String = dateFormat.format(java.util.Date(millis))
